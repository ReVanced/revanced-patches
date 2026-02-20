package app.revanced.extension.gamehub.filemanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("unused")
public class MTDataFilesProvider extends DocumentsProvider {
    public static final String COLUMN_MT_EXTRAS = "mt_extras";
    public static final String COLUMN_MT_PATH = "mt_path";
    public static final String METHOD_SET_LAST_MODIFIED = "mt:setLastModified";
    public static final String METHOD_SET_PERMISSIONS = "mt:setPermissions";
    public static final String METHOD_CREATE_SYMLINK = "mt:createSymlink";

    private static final String MT_METHOD_PREFIX = "mt:";
    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_TIME = "time";
    private static final String EXTRA_PERMISSIONS = "permissions";
    private static final String EXTRA_PATH = "path";
    private static final String RESULT_KEY = "result";
    private static final String MESSAGE_KEY = "message";

    private static final String DIR_DATA = "data";
    private static final String DIR_ANDROID_DATA = "android_data";
    private static final String DIR_ANDROID_OBB = "android_obb";
    private static final String DIR_USER_DE_DATA = "user_de_data";

    private static final String DATA_USER_PREFIX = "/data/user/";
    private static final String DATA_USER_DE_PREFIX = "/data/user_de/";

    private static final String MIME_ALL = "*/*";
    private static final String MIME_OCTET_STREAM = "application/octet-stream";

    /**
     * POSIX file type mask (S_IFMT).
     */
    @SuppressWarnings("OctalInteger")
    private static final int S_IFMT = 0170000;

    /**
     * POSIX symbolic link type (S_IFLNK).
     */
    @SuppressWarnings("OctalInteger")
    private static final int S_IFLNK = 0120000;

    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID,
    };

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE,
            COLUMN_MT_EXTRAS,
    };

    private String packageName;
    private File dataDir;
    private File userDeDataDir;
    private File androidDataDir;
    private File androidObbDir;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        packageName = context.getPackageName();
        dataDir = context.getFilesDir().getParentFile();
        String dataDirPath = dataDir.getPath();
        if (dataDirPath.startsWith(DATA_USER_PREFIX)) {
            userDeDataDir = new File(DATA_USER_DE_PREFIX
                    + dataDirPath.substring(DATA_USER_PREFIX.length()));
        }
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            androidDataDir = externalFilesDir.getParentFile();
        }
        androidObbDir = context.getObbDir();
    }

    /**
     * Returns null when the docId refers to the root directory.
     */
    private File getFileForDocId(String docId) throws FileNotFoundException {
        return getFileForDocId(docId, true);
    }

    /**
     * Returns null when the docId refers to the root directory.
     */
    private File getFileForDocId(String docId, boolean checkExists) throws FileNotFoundException {
        String filename = docId;
        if (filename.startsWith(packageName)) {
            filename = filename.substring(packageName.length());
        } else {
            throw new FileNotFoundException(docId + " not found");
        }
        if (filename.startsWith("/")) {
            filename = filename.substring(1);
        }
        if (filename.isEmpty()) {
            return null;
        }
        String type;
        String subPath;
        int i = filename.indexOf('/');
        if (i == -1) {
            type = filename;
            subPath = "";
        } else {
            type = filename.substring(0, i);
            subPath = filename.substring(i + 1);
        }
        File f = null;
        if (type.equalsIgnoreCase(DIR_DATA)) {
            f = new File(dataDir, subPath);
        } else if (type.equalsIgnoreCase(DIR_ANDROID_DATA) && androidDataDir != null) {
            f = new File(androidDataDir, subPath);
        } else if (type.equalsIgnoreCase(DIR_ANDROID_OBB) && androidObbDir != null) {
            f = new File(androidObbDir, subPath);
        } else if (type.equalsIgnoreCase(DIR_USER_DE_DATA) && userDeDataDir != null) {
            f = new File(userDeDataDir, subPath);
        }
        if (f == null) {
            throw new FileNotFoundException(docId + " not found");
        }
        if (checkExists) {
            try {
                Os.lstat(f.getPath());
            } catch (Exception e) {
                throw new FileNotFoundException(docId + " not found");
            }
        }
        return f;
    }

    @Override
    public Cursor queryRoots(String[] projection) {
        final ApplicationInfo applicationInfo = getContext().getApplicationInfo();
        final String applicationName = applicationInfo.loadLabel(
                getContext().getPackageManager()).toString();
        final MatrixCursor result = new MatrixCursor(
                projection != null ? projection : DEFAULT_ROOT_PROJECTION);
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, packageName);
        row.add(Root.COLUMN_DOCUMENT_ID, packageName);
        row.add(Root.COLUMN_SUMMARY, packageName);
        row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE | Root.FLAG_SUPPORTS_IS_CHILD);
        row.add(Root.COLUMN_TITLE, applicationName);
        row.add(Root.COLUMN_MIME_TYPES, MIME_ALL);
        row.add(Root.COLUMN_ICON, applicationInfo.icon);
        return result;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection)
            throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(
                projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        includeFile(result, documentId, null);
        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection,
            String sortOrder) throws FileNotFoundException {
        if (parentDocumentId.endsWith("/")) {
            parentDocumentId = parentDocumentId.substring(0, parentDocumentId.length() - 1);
        }
        final MatrixCursor result = new MatrixCursor(
                projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        final File parent = getFileForDocId(parentDocumentId);
        if (parent == null) {
            includeFile(result, parentDocumentId + "/" + DIR_DATA, dataDir);
            if (androidDataDir != null && androidDataDir.exists()) {
                includeFile(result, parentDocumentId + "/" + DIR_ANDROID_DATA, androidDataDir);
            }
            if (androidObbDir != null && androidObbDir.exists()) {
                includeFile(result, parentDocumentId + "/" + DIR_ANDROID_OBB, androidObbDir);
            }
            if (userDeDataDir != null && userDeDataDir.exists()) {
                includeFile(result, parentDocumentId + "/" + DIR_USER_DE_DATA, userDeDataDir);
            }
        } else {
            File[] files = parent.listFiles();
            if (files != null) {
                for (File file : files) {
                    includeFile(result, parentDocumentId + "/" + file.getName(), file);
                }
            }
        }
        return result;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode,
            CancellationSignal signal) throws FileNotFoundException {
        final File file = getFileForDocId(documentId, false);
        if (file == null) {
            throw new FileNotFoundException(documentId + " not found");
        }
        final int fileMode = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(file, fileMode);
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName)
            throws FileNotFoundException {
        File parent = getFileForDocId(parentDocumentId);
        if (parent != null) {
            File newFile = new File(parent, displayName);
            int noConflictId = 2;
            while (newFile.exists()) {
                newFile = new File(parent, displayName + " (" + noConflictId++ + ")");
            }
            try {
                boolean succeeded;
                if (Document.MIME_TYPE_DIR.equals(mimeType)) {
                    succeeded = newFile.mkdir();
                } else {
                    succeeded = newFile.createNewFile();
                }
                if (succeeded) {
                    return parentDocumentId.endsWith("/")
                            ? parentDocumentId + newFile.getName()
                            : parentDocumentId + "/" + newFile.getName();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new FileNotFoundException(
                "Failed to create document in " + parentDocumentId + " with name " + displayName);
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        File file = getFileForDocId(documentId);
        if (file == null || !deleteFile(file)) {
            throw new FileNotFoundException("Failed to delete document " + documentId);
        }
    }

    @Override
    public void removeDocument(String documentId, String parentDocumentId)
            throws FileNotFoundException {
        deleteDocument(documentId);
    }

    @Override
    public String renameDocument(String documentId, String displayName)
            throws FileNotFoundException {
        File file = getFileForDocId(documentId);
        if (file != null) {
            File target = new File(file.getParentFile(), displayName);
            if (file.renameTo(target)) {
                int i = documentId.lastIndexOf('/', documentId.length() - 2);
                return documentId.substring(0, i) + "/" + displayName;
            }
        }
        throw new FileNotFoundException(
                "Failed to rename document " + documentId + " to " + displayName);
    }

    @Override
    public String moveDocument(String sourceDocumentId, String sourceParentDocumentId,
            String targetParentDocumentId) throws FileNotFoundException {
        File sourceFile = getFileForDocId(sourceDocumentId);
        File targetDir = getFileForDocId(targetParentDocumentId);
        if (sourceFile != null && targetDir != null) {
            File targetFile = new File(targetDir, sourceFile.getName());
            if (!targetFile.exists() && sourceFile.renameTo(targetFile)) {
                return targetParentDocumentId.endsWith("/")
                        ? targetParentDocumentId + targetFile.getName()
                        : targetParentDocumentId + "/" + targetFile.getName();
            }
        }
        throw new FileNotFoundException(
                "Failed to move document " + sourceDocumentId + " to " + targetParentDocumentId);
    }

    @Override
    public String getDocumentType(String documentId) throws FileNotFoundException {
        File file = getFileForDocId(documentId);
        return file == null ? Document.MIME_TYPE_DIR : getMimeType(file);
    }

    @Override
    public boolean isChildDocument(String parentDocumentId, String documentId) {
        return documentId.startsWith(parentDocumentId);
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Bundle result = super.call(method, arg, extras);
        if (result != null) {
            return result;
        }
        if (!method.startsWith(MT_METHOD_PREFIX)) {
            return null;
        }
        Bundle out = new Bundle();
        try {
            Uri uri = extras.getParcelable(EXTRA_URI);
            List<String> pathSegments = uri.getPathSegments();
            String documentId = pathSegments.size() >= 4
                    ? pathSegments.get(3) : pathSegments.get(1);
            switch (method) {
                case METHOD_SET_LAST_MODIFIED: {
                    File file = getFileForDocId(documentId);
                    if (file == null) {
                        out.putBoolean(RESULT_KEY, false);
                    } else {
                        long time = extras.getLong(EXTRA_TIME);
                        out.putBoolean(RESULT_KEY, file.setLastModified(time));
                    }
                    break;
                }
                case METHOD_SET_PERMISSIONS: {
                    File file = getFileForDocId(documentId);
                    if (file == null) {
                        out.putBoolean(RESULT_KEY, false);
                    } else {
                        int permissions = extras.getInt(EXTRA_PERMISSIONS);
                        try {
                            Os.chmod(file.getPath(), permissions);
                            out.putBoolean(RESULT_KEY, true);
                        } catch (ErrnoException e) {
                            out.putBoolean(RESULT_KEY, false);
                            out.putString(MESSAGE_KEY, e.getMessage());
                        }
                    }
                    break;
                }
                case METHOD_CREATE_SYMLINK: {
                    File file = getFileForDocId(documentId, false);
                    if (file == null) {
                        out.putBoolean(RESULT_KEY, false);
                    } else {
                        String path = extras.getString(EXTRA_PATH);
                        try {
                            Os.symlink(path, file.getPath());
                            out.putBoolean(RESULT_KEY, true);
                        } catch (ErrnoException e) {
                            out.putBoolean(RESULT_KEY, false);
                            out.putString(MESSAGE_KEY, e.getMessage());
                        }
                    }
                    break;
                }
                default:
                    out.putBoolean(RESULT_KEY, false);
                    out.putString(MESSAGE_KEY, "Unsupported method: " + method);
                    break;
            }
        } catch (Exception e) {
            out.putBoolean(RESULT_KEY, false);
            out.putString(MESSAGE_KEY, e.toString());
        }
        return out;
    }

    private void includeFile(MatrixCursor result, String docId, File file)
            throws FileNotFoundException {
        if (file == null) {
            file = getFileForDocId(docId);
        }
        if (file == null) {
            // Root directory.
            final MatrixCursor.RowBuilder row = result.newRow();
            row.add(Document.COLUMN_DOCUMENT_ID, packageName);
            row.add(Document.COLUMN_DISPLAY_NAME, packageName);
            row.add(Document.COLUMN_SIZE, 0L);
            row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
            row.add(Document.COLUMN_LAST_MODIFIED, 0);
            row.add(Document.COLUMN_FLAGS, 0);
            return;
        }

        int flags = 0;
        if (file.isDirectory()) {
            if (file.canWrite()) {
                flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
            }
        } else if (file.canWrite()) {
            flags |= Document.FLAG_SUPPORTS_WRITE;
        }
        //noinspection ConstantConditions
        if (file.getParentFile().canWrite()) {
            flags |= Document.FLAG_SUPPORTS_DELETE;
            flags |= Document.FLAG_SUPPORTS_RENAME;
        }

        String path = file.getPath();
        final String displayName;
        boolean addExtras = false;
        if (path.equals(dataDir.getPath())) {
            displayName = DIR_DATA;
        } else if (androidDataDir != null && path.equals(androidDataDir.getPath())) {
            displayName = DIR_ANDROID_DATA;
        } else if (androidObbDir != null && path.equals(androidObbDir.getPath())) {
            displayName = DIR_ANDROID_OBB;
        } else if (userDeDataDir != null && path.equals(userDeDataDir.getPath())) {
            displayName = DIR_USER_DE_DATA;
        } else {
            displayName = file.getName();
            addExtras = true;
        }

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, displayName);
        row.add(Document.COLUMN_SIZE, file.length());
        row.add(Document.COLUMN_MIME_TYPE, getMimeType(file));
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
        row.add(Document.COLUMN_FLAGS, flags);
        row.add(COLUMN_MT_PATH, file.getAbsolutePath());
        if (addExtras) {
            try {
                StringBuilder sb = new StringBuilder();
                StructStat stat = Os.lstat(path);
                sb.append(stat.st_mode)
                        .append("|").append(stat.st_uid)
                        .append("|").append(stat.st_gid);
                if ((stat.st_mode & S_IFMT) == S_IFLNK) {
                    sb.append("|").append(Os.readlink(path));
                }
                row.add(COLUMN_MT_EXTRAS, sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getMimeType(File file) {
        if (file.isDirectory()) {
            return Document.MIME_TYPE_DIR;
        }
        final String name = file.getName();
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1).toLowerCase();
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) return mime;
        }
        return MIME_OCTET_STREAM;
    }

    private static boolean deleteFile(File file) {
        if (file.isDirectory() && !isSymbolicLink(file)) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteFile(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    private static boolean isSymbolicLink(File file) {
        try {
            StructStat stat = Os.lstat(file.getPath());
            return (stat.st_mode & S_IFMT) == S_IFLNK;
        } catch (ErrnoException e) {
            e.printStackTrace();
            return false;
        }
    }
}

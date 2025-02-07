package app.revanced.extension.all.misc.directory.documentsprovider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

/**
 * A DocumentsProvider that allows access to the app's internal data directory.
 */
@SuppressLint("LongLogTag")
public class InternalDataDocumentsProvider extends DocumentsProvider {
    private static final String[] rootColumns =
            {"root_id", "mime_types", "flags", "icon", "title", "summary", "document_id"};
    private static final String[] directoryColumns =
            {"document_id", "mime_type", "_display_name", "last_modified", "flags",
                    "_size", "full_path", "lstat_info"};
    private static final int S_IFLNK = 0x8000;

    private String packageName;
    private File dataDirectory;

    /**
     * Recursively delete a file or directory and all its children.
     *
     * @param root The file or directory to delete.
     * @return True if the file or directory and all its children were successfully deleted.
     */
    private static boolean deleteRecursively(File root) {
        // If root is a directory, delete all children first
        if (root.isDirectory()) {
            try {
                // Only delete recursively if the directory is not a symlink
                if ((Os.lstat(root.getPath()).st_mode & S_IFLNK) != S_IFLNK) {
                    File[] files = root.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (!deleteRecursively(file)) {
                                return false;
                            }
                        }
                    }
                }
            } catch (ErrnoException e) {
                Log.e("InternalDocumentsProvider", "Failed to lstat " + root.getPath(), e);
            }
        }

        // Delete file or empty directory
        return root.delete();
    }

    /**
     * Resolve the MIME type of a file based on its extension.
     *
     * @param file The file to resolve the MIME type for.
     * @return The MIME type of the file.
     */
    private static String resolveMimeType(File file) {
        if (file.isDirectory()) {
            return DocumentsContract.Document.MIME_TYPE_DIR;
        }

        String name = file.getName();
        int indexOfExtDot = name.lastIndexOf('.');
        if (indexOfExtDot < 0) {
            // No extension
            return "application/octet-stream";
        }

        String extension = name.substring(indexOfExtDot + 1).toLowerCase();
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    @Override
    public final boolean onCreate() {
        return true;
    }

    @Override
    public final void attachInfo(Context context, ProviderInfo providerInfo) {
        super.attachInfo(context, providerInfo);

        this.packageName = context.getPackageName();
        this.dataDirectory = context.getFilesDir().getParentFile();
    }

    @Override
    public final String createDocument(String parentDocumentId, String mimeType, String displayName) throws FileNotFoundException {
        File directory = resolveDocumentId(parentDocumentId);
        File file = new File(directory, displayName);

        // If file already exists, append a number to the name
        int i = 2;
        while (file.exists()) {
            file = new File(directory, displayName + " (" + i + ")");
            i++;
        }

        try {
            // Create the file or directory
            if (mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR) ? file.mkdir() : file.createNewFile()) {
                // Return the document ID of the new entity
                if (!parentDocumentId.endsWith("/")) {
                    parentDocumentId = parentDocumentId + "/";
                }
                return parentDocumentId + file.getName();
            }
        } catch (IOException e) {
            // Do nothing. We are throwing a FileNotFoundException later if the file could not be created.
        }
        throw new FileNotFoundException("Failed to create document in " + parentDocumentId + " with name " + displayName);
    }

    @Override
    public final void deleteDocument(String documentId) throws FileNotFoundException {
        File file = resolveDocumentId(documentId);
        if (!deleteRecursively(file)) {
            throw new FileNotFoundException("Failed to delete document " + documentId);
        }
    }

    @Override
    public final String getDocumentType(String documentId) throws FileNotFoundException {
        return resolveMimeType(resolveDocumentId(documentId));
    }

    @Override
    public final boolean isChildDocument(String parentDocumentId, String documentId) {
        return documentId.startsWith(parentDocumentId);
    }

    @Override
    public final String moveDocument(String sourceDocumentId, String sourceParentDocumentId, String targetParentDocumentId) throws FileNotFoundException {
        File source = resolveDocumentId(sourceDocumentId);
        File dest = resolveDocumentId(targetParentDocumentId);

        File file = new File(dest, source.getName());
        if (!file.exists() && source.renameTo(file)) {
            // Return the new document ID
            if (targetParentDocumentId.endsWith("/")) {
                return targetParentDocumentId + file.getName();
            }
            return targetParentDocumentId + "/" + file.getName();
        }

        throw new FileNotFoundException("Failed to move document from " + sourceDocumentId + " to " + targetParentDocumentId);
    }

    @Override
    public final ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        File file = resolveDocumentId(documentId);
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode));
    }

    @Override
    public final Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        if (parentDocumentId.endsWith("/")) {
            parentDocumentId = parentDocumentId.substring(0, parentDocumentId.length() - 1);
        }

        if (projection == null) {
            projection = directoryColumns;
        }

        MatrixCursor cursor = new MatrixCursor(projection);
        File children = resolveDocumentId(parentDocumentId);

        // Collect all children
        File[] files = children.listFiles();
        if (files != null) {
            for (File file : files) {
                addRowForDocument(cursor, parentDocumentId + "/" + file.getName(), file);
            }
        }
        return cursor;
    }

    @Override
    public final Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        if (projection == null) {
            projection = directoryColumns;
        }

        MatrixCursor cursor = new MatrixCursor(projection);
        addRowForDocument(cursor, documentId, null);
        return cursor;
    }

    @Override
    public final Cursor queryRoots(String[] projection) {
        ApplicationInfo info = Objects.requireNonNull(getContext()).getApplicationInfo();
        String appName = info.loadLabel(getContext().getPackageManager()).toString();

        if (projection == null) {
            projection = rootColumns;
        }

        MatrixCursor cursor = new MatrixCursor(projection);
        MatrixCursor.RowBuilder row = cursor.newRow();
        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, this.packageName);
        row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, this.packageName);
        row.add(DocumentsContract.Root.COLUMN_SUMMARY, this.packageName);
        row.add(DocumentsContract.Root.COLUMN_FLAGS,
                DocumentsContract.Root.FLAG_LOCAL_ONLY |
                        DocumentsContract.Root.FLAG_SUPPORTS_IS_CHILD);
        row.add(DocumentsContract.Root.COLUMN_TITLE, appName);
        row.add(DocumentsContract.Root.COLUMN_MIME_TYPES, "*/*");
        row.add(DocumentsContract.Root.COLUMN_ICON, info.icon);
        return cursor;
    }

    @Override
    public final void removeDocument(String documentId, String parentDocumentId) throws FileNotFoundException {
        deleteDocument(documentId);
    }

    @Override
    public final String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        File file = resolveDocumentId(documentId);
        if (!file.renameTo(new File(file.getParentFile(), displayName))) {
            throw new FileNotFoundException("Failed to rename document from " + documentId + " to " + displayName);
        }

        // Return the new document ID
        return documentId.substring(0, documentId.lastIndexOf('/', documentId.length() - 2)) + "/" + displayName;
    }

    /**
     * Resolve a file instance for a given document ID.
     *
     * @param fullContentPath The document ID to resolve.
     * @return File object for the given document ID.
     * @throws FileNotFoundException If the document ID is invalid or the file does not exist.
     */
    private File resolveDocumentId(String fullContentPath) throws FileNotFoundException {
        if (!fullContentPath.startsWith(this.packageName)) {
            throw new FileNotFoundException(fullContentPath + " not found");
        }
        String path = fullContentPath.substring(this.packageName.length());

        // Resolve the relative path within /data/data/{PKG}
        File file;
        if (path.equals("/") || path.isEmpty()) {
            file = this.dataDirectory;
        } else {
            // Remove leading slash
            String relativePath = path.substring(1);
            file = new File(this.dataDirectory, relativePath);
        }

        if (!file.exists()) {
            throw new FileNotFoundException(fullContentPath + " not found");
        }
        return file;
    }

    /**
     * Add a row containing all file properties to a MatrixCursor for a given document ID.
     *
     * @param cursor     The cursor to add the row to.
     * @param documentId The document ID to add the row for.
     * @param file       The file to add the row for. If null, the file will be resolved from the document ID.
     * @throws FileNotFoundException If the file does not exist.
     */
    private void addRowForDocument(MatrixCursor cursor, String documentId, File file) throws FileNotFoundException {
        if (file == null) {
            file = resolveDocumentId(documentId);
        }

        int flags = 0;
        if (file.isDirectory()) {
            // Prefer list view for directories
            flags = flags | DocumentsContract.Document.FLAG_DIR_PREFERS_LAST_MODIFIED;
        }

        if (file.canWrite()) {
            if (file.isDirectory()) {
                flags = flags | DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE;
            }

            flags = flags | DocumentsContract.Document.FLAG_SUPPORTS_WRITE |
                    DocumentsContract.Document.FLAG_SUPPORTS_DELETE |
                    DocumentsContract.Document.FLAG_SUPPORTS_RENAME |
                    DocumentsContract.Document.FLAG_SUPPORTS_MOVE;
        }

        MatrixCursor.RowBuilder row = cursor.newRow();
        row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, documentId);
        row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, file.getName());
        row.add(DocumentsContract.Document.COLUMN_SIZE, file.length());
        row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, resolveMimeType(file));
        row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, file.lastModified());
        row.add(DocumentsContract.Document.COLUMN_FLAGS, flags);

        // Custom columns
        row.add("full_path", file.getAbsolutePath());

        // Add lstat column
        String path = file.getPath();
        try {
            StringBuilder sb = new StringBuilder();
            StructStat lstat = Os.lstat(path);
            sb.append(lstat.st_mode);
            sb.append(";");
            sb.append(lstat.st_uid);
            sb.append(";");
            sb.append(lstat.st_gid);
            // Append symlink target if it is a symlink
            if ((lstat.st_mode & S_IFLNK) == S_IFLNK) {
                sb.append(";");
                sb.append(Os.readlink(path));
            }
            row.add("lstat_info", sb.toString());
        } catch (Exception ex) {
            Log.e("InternalDocumentsProvider", "Failed to get lstat info for " + path, ex);
        }
    }
}

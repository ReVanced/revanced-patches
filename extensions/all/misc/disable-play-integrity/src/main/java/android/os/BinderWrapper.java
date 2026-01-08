package android.os;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;

/** @hide */
public class BinderWrapper implements IBinder {
    protected final IBinder base;

    public BinderWrapper(IBinder base) {
        this.base = base;
    }

    @Override
    public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        return base.transact(code, data, reply, flags);
    }

    @Nullable
    @Override
    public IInterface queryLocalInterface(@NonNull String descriptor) {
        return base.queryLocalInterface(descriptor);
    }

    @Nullable
    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return base.getInterfaceDescriptor();
    }

    @Override
    public boolean pingBinder() {
        return base.pingBinder();
    }

    @Override
    public boolean isBinderAlive() {
        return base.isBinderAlive();
    }

    @Override
    public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        base.dump(fd, args);
    }

    @Override
    public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        base.dumpAsync(fd, args);
    }

    @Override
    public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {
        base.linkToDeath(recipient, flags);
    }

    @Override
    public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
        return base.unlinkToDeath(recipient, flags);
    }
}

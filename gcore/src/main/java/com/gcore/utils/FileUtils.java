package com.gcore.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.system.Os;
import android.text.TextUtils;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/* loaded from: classes.jar:com/gcore/utils/FileUtils.class */
public class FileUtils {

    /* loaded from: classes.jar:com/gcore/utils/FileUtils$FileLock.class */
    public static class FileLock {
        private static FileLock singleton;
        private final Map<String, FileLockCount> mRefCountMap = new ConcurrentHashMap();

        /* JADX INFO: Access modifiers changed from: private */
        /* loaded from: classes.jar:com/gcore/utils/FileUtils$FileLock$FileLockCount.class */
        public static class FileLockCount {
            FileChannel fChannel;
            RandomAccessFile fOs;
            java.nio.channels.FileLock mFileLock;
            int mRefCount;

            FileLockCount(java.nio.channels.FileLock fileLock, int i, RandomAccessFile randomAccessFile, FileChannel fileChannel) {
                this.mFileLock = fileLock;
                this.mRefCount = i;
                this.fOs = randomAccessFile;
                this.fChannel = fileChannel;
            }
        }

        private int RefCntDec(String str) {
            int i;
            if (this.mRefCountMap.containsKey(str)) {
                FileLockCount fileLockCount = this.mRefCountMap.get(str);
                int i2 = fileLockCount.mRefCount - 1;
                fileLockCount.mRefCount = i2;
                i = i2;
                if (i2 <= 0) {
                    this.mRefCountMap.remove(str);
                    i = i2;
                }
            } else {
                i = 0;
            }
            return i;
        }

        private void RefCntInc(String str, java.nio.channels.FileLock fileLock, RandomAccessFile randomAccessFile, FileChannel fileChannel) {
            if (!this.mRefCountMap.containsKey(str)) {
                this.mRefCountMap.put(str, new FileLockCount(fileLock, 1, randomAccessFile, fileChannel));
                return;
            }
            this.mRefCountMap.get(str).mRefCount++;
        }

        public static FileLock getInstance() {
            if (singleton == null) {
                singleton = new FileLock();
            }
            return singleton;
        }

        public boolean LockExclusive(File file) {
            if (file == null) {
                return false;
            }
            try {
                File file2 = new File(file.getParentFile().getAbsolutePath().concat("/lock"));
                if (!file2.exists()) {
                    file2.createNewFile();
                }
                RandomAccessFile randomAccessFile = new RandomAccessFile(file2.getAbsolutePath(), "rw");
                FileChannel channel = randomAccessFile.getChannel();
                java.nio.channels.FileLock lock = channel.lock();
                if (lock.isValid()) {
                    RefCntInc(file2.getAbsolutePath(), lock, randomAccessFile, channel);
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }

        public void unLock(File file) {
            FileLockCount fileLockCount;
            File file2 = new File(file.getParentFile().getAbsolutePath().concat("/lock"));
            if (file2.exists() && this.mRefCountMap.containsKey(file2.getAbsolutePath()) && (fileLockCount = this.mRefCountMap.get(file2.getAbsolutePath())) != null) {
                java.nio.channels.FileLock fileLock = fileLockCount.mFileLock;
                RandomAccessFile randomAccessFile = fileLockCount.fOs;
                FileChannel fileChannel = fileLockCount.fChannel;
                try {
                    if (RefCntDec(file2.getAbsolutePath()) <= 0) {
                        if (fileLock != null && fileLock.isValid()) {
                            fileLock.release();
                        }
                        if (randomAccessFile != null) {
                            randomAccessFile.close();
                        }
                        if (fileChannel != null) {
                            fileChannel.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* loaded from: classes.jar:com/gcore/utils/FileUtils$FileMode.class */
    public interface FileMode {
        public static final int MODE_755 = 493;
        public static final int MODE_IRGRP = 32;
        public static final int MODE_IROTH = 4;
        public static final int MODE_IRUSR = 256;
        public static final int MODE_ISGID = 1024;
        public static final int MODE_ISUID = 2048;
        public static final int MODE_ISVTX = 512;
        public static final int MODE_IWGRP = 16;
        public static final int MODE_IWOTH = 2;
        public static final int MODE_IWUSR = 128;
        public static final int MODE_IXGRP = 8;
        public static final int MODE_IXOTH = 1;
        public static final int MODE_IXUSR = 64;
    }

    public static String buildValidExtFilename(String str) {
        if (TextUtils.isEmpty(str) || ".".equals(str) || "..".equals(str)) {
            return "(invalid)";
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (isValidExtFilenameChar(charAt)) {
                sb.append(charAt);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    public static boolean canRead(String str) {
        return new File(str).canRead();
    }

    public static File changeExt(File file, String str) {
        int lastIndexOf;
        String str2;
        String absolutePath = file.getAbsolutePath();
        if (getFilenameExt(absolutePath).equals(str)) {
            return file;
        }
        if (absolutePath.lastIndexOf(".") > 0) {
            str2 = absolutePath.substring(0, lastIndexOf + 1) + str;
        } else {
            str2 = absolutePath + "." + str;
        }
        return new File(str2);
    }

    public static void chmod(String str, int i) throws Exception {
        try {
            Os.chmod(str, i);
        } catch (Exception e) {
            String str2 = new File(str).isDirectory() ? "chmod  -R " : "chmod ";
            String format = String.format("%o", Integer.valueOf(i));
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(str2 + format + " " + str).waitFor();
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }

    public static void copyFile(File file, File file2) throws IOException {
        FileOutputStream fileOutputStream;
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            try {
                FileOutputStream fileOutputStream2 = new FileOutputStream(file2);
                try {
                    FileChannel channel = fileInputStream.getChannel();
                    FileChannel channel2 = fileOutputStream2.getChannel();
                    ByteBuffer allocate = ByteBuffer.allocate(1024);
                    while (true) {
                        allocate.clear();
                        if (channel.read(allocate) == -1) {
                            closeQuietly(fileInputStream);
                            closeQuietly(fileOutputStream2);
                            return;
                        }
                        allocate.limit(allocate.position());
                        allocate.position(0);
                        channel2.write(allocate);
                    }
                } catch (Throwable th) {
                    th = th;
                    fileOutputStream = fileOutputStream2;
                    closeQuietly(fileInputStream);
                    closeQuietly(fileOutputStream);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = null;
            }
        } catch (Throwable th3) {
            th = th3;
            fileOutputStream = null;
            fileInputStream = null;
        }
    }

    public static void copyFile(InputStream inputStream, File file) {
        FileOutputStream fileOutputStream;
        FileOutputStream fileOutputStream2;
        try {
            fileOutputStream2 = new FileOutputStream(file);
        } catch (Throwable th) {
            fileOutputStream = null;
        }
        try {
            byte[] bArr = new byte[4096];
            while (true) {
                int read = inputStream.read(bArr);
                if (read == -1) {
                    fileOutputStream2.flush();
                    closeQuietly(inputStream);
                    closeQuietly(fileOutputStream2);
                    return;
                }
                fileOutputStream2.write(bArr, 0, read);
            }
        } catch (Throwable th2) {
            fileOutputStream = fileOutputStream2;
            closeQuietly(inputStream);
            closeQuietly(fileOutputStream);
        }
    }

    public static int count(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                return 1;
            }
            int i = 0;
            if (file.isDirectory()) {
                String[] list = file.list();
                i = list == null ? 0 : list.length;
            }
            return i;
        }
        return -1;
    }

    public static void createSymlink(String str, String str2) throws Exception {
        try {
            Os.link(str, str2);
        } catch (Throwable th) {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("ln -s " + str + " " + str2).waitFor();
        }
    }

    public static int deleteDir(File file) {
        boolean z;
        int i = 0;
        if (file.isDirectory()) {
            try {
                z = isSymlink(file);
            } catch (Exception e) {
                z = false;
            }
            i = 0;
            if (!z) {
                i = 0;
                for (String str : file.list()) {
                    i += deleteDir(new File(file, str));
                }
            }
        }
        int i2 = i;
        if (file.delete()) {
            i2 = i + 1;
        }
        return i2;
    }

    public static int deleteDir(String str) {
        return deleteDir(new File(str));
    }

    private static String getDataColumn(Context context, Uri uri, String str, String[] strArr) {
        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"_data"}, str, strArr, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        String string = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                        if (cursor != null) {
                            cursor.close();
                        }
                        return string;
                    }
                } catch (Throwable th) {
                    th = th;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
                return null;
            }
            return null;
        } catch (Throwable th2) {
            th = th2;
            cursor = null;
        }
    }

    public static String getFilenameExt(String str) {
        int lastIndexOf = str.lastIndexOf(46);
        return lastIndexOf == -1 ? "" : str.substring(lastIndexOf + 1);
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isExist(String str) {
        return new File(str).exists();
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isSymlink(File file) throws IOException {
        if (file != null) {
            if (file.getParent() != null) {
                file = new File(file.getParentFile().getCanonicalFile(), file.getName());
            }
            return !file.getCanonicalFile().equals(file.getAbsoluteFile());
        }
        throw new NullPointerException("File must not be null");
    }

    public static boolean isValidExtFilename(String str) {
        return str != null && str.equals(buildValidExtFilename(str));
    }

    private static boolean isValidExtFilenameChar(char c) {
        return (c == 0 || c == '/') ? false : true;
    }

    public static void mkdirs(File file) {
        if (file.exists()) {
            return;
        }
        file.mkdirs();
    }

    public static void mkdirs(String str) {
        mkdirs(new File(str));
    }

    public static int peekInt(byte[] bArr, int i, ByteOrder byteOrder) {
        int i2;
        int i3;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            int i4 = ((bArr[i + 1] & 255) << 16) | ((bArr[i] & 255) << 24) | ((bArr[i + 2] & 255) << 8);
            i2 = bArr[i + 3] & 255;
            i3 = i4;
        } else {
            int i5 = ((bArr[i + 1] & 255) << 8) | (bArr[i] & 255) | ((bArr[i + 2] & 255) << 16);
            i2 = (bArr[i + 3] & 255) << 24;
            i3 = i5;
        }
        return i2 | i3;
    }

    public static Parcel readToParcel(File file) throws IOException {
        Parcel obtain = Parcel.obtain();
        byte[] byteArray = toByteArray(file);
        obtain.unmarshall(byteArray, 0, byteArray.length);
        obtain.setDataPosition(0);
        return obtain;
    }

    public static String readToString(String str) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(str);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int read = fileInputStream.read();
            if (read == -1) {
                return byteArrayOutputStream.toString();
            }
            byteArrayOutputStream.write(read);
        }
    }

    public static boolean renameTo(File file, File file2) {
        return file.renameTo(file2);
    }

    public static byte[] toByteArray(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            return toByteArray(fileInputStream);
        } finally {
            closeQuietly(fileInputStream);
        }
    }

    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[100];
        while (true) {
            int read = inputStream.read(bArr, 0, 100);
            if (read <= 0) {
                return byteArrayOutputStream.toByteArray();
            }
            byteArrayOutputStream.write(bArr, 0, read);
        }
    }

    public static void writeParcelToFile(Parcel parcel, File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(parcel.marshall());
        fileOutputStream.close();
    }

    public static void writeParcelToOutput(Parcel parcel, FileOutputStream fileOutputStream) throws IOException {
        fileOutputStream.write(parcel.marshall());
    }

    public static void writeToFile(InputStream inputStream, File file) throws IOException {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        byte[] bArr = new byte[1024];
        while (true) {
            int read = inputStream.read(bArr, 0, 1024);
            if (read == -1) {
                bufferedOutputStream.close();
                return;
            }
            bufferedOutputStream.write(bArr, 0, read);
        }
    }

    public static void writeToFile(byte[] bArr, File file) throws IOException {
        FileChannel fileChannel;
        ReadableByteChannel readableByteChannel;
        FileOutputStream fileOutputStream;
        FileOutputStream fileOutputStream2 = null;
        try {
            readableByteChannel = Channels.newChannel(new ByteArrayInputStream(bArr));
            try {
                fileOutputStream = new FileOutputStream(file);
                fileChannel = null;
            } catch (Throwable th) {
                th = th;
                fileChannel = null;
            }
        } catch (Throwable th2) {
            th = th2;
            fileChannel = null;
            readableByteChannel = null;
        }
        try {
            FileChannel channel = fileOutputStream.getChannel();
            fileChannel = channel;
            channel.transferFrom(readableByteChannel, 0L, bArr.length);
            fileOutputStream.close();
            if (readableByteChannel != null) {
                readableByteChannel.close();
            }
            if (channel != null) {
                channel.close();
            }
        } catch (Throwable th3) {
            th = th3;
            fileOutputStream2 = fileOutputStream;
            if (fileOutputStream2 != null) {
                fileOutputStream2.close();
            }
            if (readableByteChannel != null) {
                readableByteChannel.close();
            }
            if (fileChannel != null) {
                fileChannel.close();
            }
            throw th;
        }
    }
}


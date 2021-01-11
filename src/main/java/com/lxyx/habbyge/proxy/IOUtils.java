package com.lxyx.habbyge.proxy;

import org.objectweb.asm.Type;

import java.io.*;
import java.lang.reflect.Method;

public final class IOUtils {
    private IOUtils() {
    }

    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static Class<?> loadClass(ClassLoader classLoader, byte[] bytes) {
        try {
            Class<?> clClass = Class.forName("java.lang.ClassLoader");
            // Android中的这个方法在Android中是直接throws一个Exception的，所以不能使用，Android中只能生成dex了
            Method method = clClass.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
            method.setAccessible(true);
            Class<?> interfaceClass = (Class<?>) method.invoke(classLoader, bytes, 0, bytes.length);

            System.out.println("_LXYX_ genInterface, interfaceClass: "
                    + interfaceClass.getName() + ", "
                    + interfaceClass.isInterface());

            return interfaceClass;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return 让被代理Class实现interface，并返回新的class的字节码
     */
    public static InputStream getStream(Class<?> _class) {
        String className = Type.getInternalName(_class) + ".class";
        System.out.println("_LXYX_ _getStream: className: " + className);

        InputStream stream;
        try {
            // 取巧的地方，Android的ClassLoader中也存在这个API
            stream = _class.getClassLoader().getResourceAsStream(className);
            if (stream == null) {
                System.err.println("_LXYX_ _getStream: stream == null");
                return null;
            }
            System.out.println("_LXYX_ _getStream: stream: " + stream.getClass().getName());
            return stream;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean _write2Disk(String classFilePath, byte[] bytes, boolean isNew) {
        File file = new File(classFilePath);

        if (!isNew) {
            if (!file.exists()) {
                System.err.println("_LXYX_ _write2Disk, classFilePath: NOT EXISTS!");
                return false;
            }
            boolean readable = file.setReadable(true);
            boolean writable = file.setWritable(true);
            if (!file.canWrite()) {
                System.err.println("_LXYX_ classFilePath: NO Write Access: " + readable + ", " + writable);
                return false;
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}

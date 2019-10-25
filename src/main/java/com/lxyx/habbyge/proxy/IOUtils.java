package com.lxyx.habbyge.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
}

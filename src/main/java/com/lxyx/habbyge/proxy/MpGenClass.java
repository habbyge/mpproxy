package com.lxyx.habbyge.proxy;

import javafx.util.Pair;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Created by habbyge on 2019/10/12.
 * 利用jdk自带的Proxy技术来实现通用对象的动态代理
 *
 * --执行jar包中测试例子：
 * cd mpproxy/build/distributions/mpproxy-1.0-SNAPSHOT/bin/
 * ./mpproxy
 */
public class MpGenClass {

    private MpGenClass() {
    }

    /**
     * @param _class 要被动态代理的class
     * @param methodInfos class中要被动态代理劫持的方法
     * @param handler 劫持方法的处理器
     * @return 返回 interface名、代理对象
     */
    public static ProxyInfo getProxy(Class<?> _class,
                                     List<Pair<String, String>> methodInfos,
                                     InvocationHandler handler) {

        MpClassLoader mpClassLoader = new MpClassLoader();

        ProxyInfo proxyInfo = _genProxyInfo(mpClassLoader, _class, methodInfos);
        if (proxyInfo == null) {
            System.err.println("_LXYX_ TestGenClass, getProxy: proxyInfo NULL !!");
            return null;
        }

        Object proxy = Proxy.newProxyInstance(proxyInfo.classLoader,
                new Class<?>[]{proxyInfo._interface}, handler);
        System.out.println("_LXYX_ TestGenClass, getProxy, proxy: " + proxy.getClass().getName());

        proxyInfo.proxy = proxy;

        return proxyInfo;
    }

    /**
     * @return 返回<interface, 旧的原始对象>
     */
    private static ProxyInfo _genProxyInfo(MpClassLoader classLoader, Class<?> _class,
                                           List<Pair<String, String>> methodInfos) {

        // 生成interface，并写入磁盘
        byte[] bytesOfInterface = _genInterface(_class, methodInfos);
        Class<?> _interface = IOUtils.loadClass(classLoader, bytesOfInterface);
        if (_interface == null) {
            return null;
        }
        System.out.println("_LXYX_ _interface: " + _interface.getName());

        // 实现(继承)interface，并生成新的class，写入磁盘
        InputStream stream = _getStream(_class); // 获取实现(继承)接口后的class的流
        if (stream == null) {
            return null;
        }
        try {
            byte[] bytes = IOUtils.toByteArray(stream);
            if (bytes == null) {
                return null;
            }
            System.out.println("_LXYX_ newClass, bytes: " + bytes.length);

            String interfaceName = Type.getInternalName(_interface); // 获取interface的路径
            System.out.println("_LXYX_ interfaceName: " + interfaceName);

            byte[] newBytes = _injectInterface2Class(bytes, interfaceName);
            System.out.println("_LXYX_ newBytes: " + newBytes.length);

            // 这里加载旧的原始对象(相对于Proxy来说)，But ！！！JVM中的Classloader中已经有了该Class，
            // 如何重复加载 ？自定义一个ClassLoader: MpClassLoader !!
            // 这里不需要存入磁盘，直接通过ClassLoader加载到内存中，使用即可，进程重启后，依旧会在运行时，
            // 重新生成对应的interface、实现该interface后的class，并被ClassLoader加载
            Class<?> oldObjectClass;
            try {
                System.out.println("_LXYX_ myClassLoader: class !!");
                classLoader.setBytes(newBytes);
                oldObjectClass = classLoader.findClass("com.lxyx.habbyge.TestProxy");
                System.out.println("_LXYX_ oldObjectClass: " + oldObjectClass.getName());
                for (Class<?> _interface_ : oldObjectClass.getInterfaces()) {
                    System.out.println("_LXYX_ oldObjectClass, _interface_: " + _interface_.getName());
                }

                ProxyInfo info = new ProxyInfo();
                info.oldObject = oldObjectClass.newInstance();
                info.classLoader = classLoader;

                info._interface = _interface;
                return info;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            // Crash Caused by: java.lang.LinkageError:
            // loader (instance of  sun/misc/Launcher$AppClassLoader):
            // attempted duplicate(重读定义) class definition for name:
            // "com/tencent/habbyge/asm/TestProxy"
            // 因为已经存在了一个该被代理类(TestProxy)，这里使用ClassLoader再次加载，当然是重复了，不合理 !!
            /*return IOUtils.loadClass(classLoader, newBytes);*/
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }

        return null;
    }

    private static byte[] _genInterface(Class<?> _class, List<Pair<String, String>> methodInfos) {
        String className = Type.getInternalName(_class);
        String interfaceName = className + "_" + "IHellProxy";
        System.out.println("_LXYX_ interfaceName: " + interfaceName);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_7,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE, // interface访问flag必须这样
                interfaceName, null, "java/lang/Object", null);

        for (Pair<String, String> methodInfo : methodInfos) {
            cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
                    methodInfo.getKey(), methodInfo.getValue(), null, null); // todo 暂时不支持exception
        }

        cw.visitEnd();
        byte[] byteCodes = cw.toByteArray();
        System.out.println("_LXYX_ genInterfaceBytes, byteCodes: " + byteCodes.length);
        return byteCodes;
    }

    private static byte[] _injectInterface2Class(byte[] bytes, String _interface) {
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new MpProxyClassVisitor(cw, _interface);
        cr.accept(cv, 0);

        byte[] codeBytes = cw.toByteArray();
        System.out.println("_LXYX_ _injectInterface2Class, codeBytes: " + codeBytes.length);
        return codeBytes;
    }

    /**
     * @return 让被代理Class实现interface，并返回新的class的字节码
     */
    private static InputStream _getStream(Class<?> _class) {
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

    private static boolean _write2Disk(String classFilePath, byte[] bytes, boolean isNew) {
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

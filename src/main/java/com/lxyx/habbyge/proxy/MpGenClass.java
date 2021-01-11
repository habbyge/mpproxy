package com.lxyx.habbyge.proxy;

import org.gradle.internal.Pair;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Created by habbyge on 2019/10/12.
 * 利用jdk自带的Proxy技术来实现通用对象的动态代理
 * <p>
 * --执行jar包中测试例子：
 * cd mpproxy/build/distributions/mpproxy-1.0-SNAPSHOT/bin/
 * ./mpproxy
 */
final class MpGenClass {

    private MpGenClass() {
    }

    /**
     * @param _class      要被动态代理的class
     * @param methodInfos class中要被动态代理劫持的方法
     * @param handler     劫持方法的处理器
     * @return 返回 interface名、代理对象
     */
    static ProxyInfo getProxy(Class<?> _class,
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
        InputStream stream = IOUtils.getStream(_class); // 获取实现(继承)接口后的class的流
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
                classLoader.setBytes(newBytes);
                oldObjectClass = classLoader.findClass(_class.getCanonicalName());
                classLoader.setBytes(null); // 清理掉当前加载的字节码，避免影响其他的class加载

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
                    methodInfo.getLeft(), methodInfo.getRight(),
                    null, null); // todo 暂时不支持exception
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
}

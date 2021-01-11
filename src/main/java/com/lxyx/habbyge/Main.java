package com.lxyx.habbyge;

import com.lxyx.habbyge.proxy.MpGenClass;
import com.lxyx.habbyge.proxy.ProxyInfo;
import com.lxyx.habbyge.test.TestProxy;
import com.lxyx.habbyge.test.TestProxy2;
import org.gradle.internal.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("_LXYX_ Hello, lxyx !!");

        Main main = new Main();
        ProxyInfo proxyInfo = main.getProxyOfTestProxy();
        main.test4Proxy1(proxyInfo);
        System.out.println("_LXYX_ Shameless dividing line ~~~~~~~~~~~~~~~~~~~~~~~~~~");
        main.test4Proxy2(proxyInfo);
    }

    private Object mOldObject = null;

    private ProxyInfo getProxyOfTestProxy() {
        /*TestGenClass.genClass();*/

        // 先被ClassLoader加载了，应该放在生成新的该被代理类，并覆盖之后再加载
        /*try {
            Class<?> clazz = Class.forName("com.lxyx.habbyge.Main");
            Field field = clazz.getDeclaredField("testProxy");
            field.setAccessible(true);
            oldObject = field.get(null);
            System.out.println("HABBYGE-MALI, oldObject: " + oldObject.getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }*/

        //【测试用例】测试通过asm生成动态代理对象
        List<Pair<String, String>> methodInfos = new ArrayList<Pair<String, String>>();
        methodInfos.add(Pair.of("foo", "()V"));
        methodInfos.add(Pair.of("foo1", "(Ljava/lang/String;)Ljava/lang/String;"));
        methodInfos.add(Pair.of("foo2", "(I)Ljava/lang/Integer;"));

        return MpGenClass.getProxy(TestProxy.class, methodInfos, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("_LXYX_ TestGenClass, invoke: " + method.getName());

                //  这里问题的关键是：需要让ClassLoader重新加载TestProxy这个类，因为此时在JVM中的的
                //  TestProxy类是旧的，并没有实现asm生成的interface.
                //  jvm中的该TestProxy类信息已经脏了，需要更新为新的，才可以，所以需要从新加载替换.
                method.setAccessible(true);
                return method.invoke(mOldObject, args);
            }
        });
    }

    private void test4Proxy1(ProxyInfo proxyInfo) {
        if (proxyInfo == null) {
            return;
        }

        mOldObject = proxyInfo.oldObject; // 保存旧的原始对象

        int interfaceCount = mOldObject.getClass().getInterfaces().length;
        System.out.println("_LXYX_ interfaceCount: " + interfaceCount);

        // 动态代理调用
        try {
            Class<?> intefaceClass = mOldObject.getClass().getInterfaces()[0];
            System.out.println("_LXYX_ interfazz: " + intefaceClass.getName());

            System.out.println("_LXYX_ foo.invoke......");
            Method fooMethod = intefaceClass.getDeclaredMethod("foo");
            fooMethod.setAccessible(true);
            fooMethod.invoke(proxyInfo.proxy);

            System.out.println("_LXYX_ foo1.invoke......");
            Method fooMethod1 = intefaceClass.getDeclaredMethod("foo1", String.class);
            fooMethod1.setAccessible(true);
            String fooMethod1RetVal = (String) fooMethod1.invoke(proxyInfo.proxy, "I love Mali");
            System.out.println("_LXYX_ fooMethod1RetVal: " + fooMethod1RetVal);

            System.out.println("_LXYX_ foo2.invoke......");
            Method fooMethod2 = proxyInfo._interface.getDeclaredMethod("foo2", int.class);
            fooMethod2.setAccessible(true);
            int fooMethod2RetVal = (int) fooMethod2.invoke(proxyInfo.proxy, 17);
            System.out.println("_LXYX_ fooMethod2RetVal: " + fooMethod2RetVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TestProxy2 testProxy2;
    private void test4Proxy2(ProxyInfo proxyInfo) {
        TestProxy testProxy = new TestProxy();
        testProxy2 = new TestProxy2(testProxy);

        // 旧的
        testProxy2.test1();
        testProxy2.test2("I love _LXYX_");
        testProxy2.test3(10000);

        // 新的代理
        if (proxyInfo == null) {
            return;
        }
        try {
            Field field = testProxy2.getClass().getDeclaredField("testProxy");
            field.setAccessible(true);
            field.set(testProxy2, proxyInfo.proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
        testProxy2.test1();
        testProxy2.test2("I love _LXYX_");
        testProxy2.test3(10000);
    }
}

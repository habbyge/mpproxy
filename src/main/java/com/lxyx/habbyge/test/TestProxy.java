package com.lxyx.habbyge.test;

/**
 * 测试用例1
 */
// fixme: 这里的例子是：final，一样能够对其进行动态代理劫持
public final class TestProxy {

    public void foo() {
        System.out.println("_LXYX_ TestProxy, foo()");
    }

    // fixme: 这里的例子是：final，一样能够对其进行动态代理劫持
    public final String foo1(String name) {
        System.out.println("_LXYX_ TestProxy, foo1(): " + name);
        return name;
    }

    public Integer foo2(int age) {
        System.out.println("_LXYX_ TestProxy, foo2(): " + age);
        return age;
    }
}

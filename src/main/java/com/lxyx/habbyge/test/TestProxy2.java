package com.lxyx.habbyge.test;

/**
 * 测试用例2
 */
public class TestProxy2 {

    private TestProxy testProxy;

    public TestProxy2(TestProxy testProxy) {
        this.testProxy = testProxy;
    }

    public void test1() {
        System.out.println("_LXYX_ TestProxy2, test1()");
        testProxy.foo();
    }

    public void test2(String name) {
        System.out.println("_LXYX_ TestProxy2, test2()");
        testProxy.foo1(name);
    }

    public void test3(int age) {
        System.out.println("_LXYX_ TestProxy2, test3()");
        testProxy.foo2(age);
    }
}

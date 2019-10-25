package com.lxyx.habbyge;

public class TestProxy {

    public void foo() {
        System.out.println("_LXYX_ TestProxy, foo()");
    }

    public String foo1(String name) {
        System.out.println("_LXYX_ TestProxy, foo1(): " + name);
        return name;
    }

    public Integer foo2(int age) {
        System.out.println("_LXYX_ TestProxy, foo2(): " + age);
        return age;
    }
}

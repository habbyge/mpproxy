package com.lxyx.habbyge.proxy;

/**
 * Created by habbyge on 2019/10/12.
 */
public final class ProxyInfo {
    public Class<?> _interface = null;   // 接口
    public Object proxy = null;         // 代理对象

    public Object oldObject = null; // 旧的原始对象
    // 为了能够重新加载旧的原始类，如果要使用这个class中的成员，必须使用该classLoader来加载
    public ClassLoader classLoader;
}

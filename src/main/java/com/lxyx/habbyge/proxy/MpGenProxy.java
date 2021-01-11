package com.lxyx.habbyge.proxy;

import org.gradle.internal.Pair;

import java.lang.reflect.InvocationHandler;
import java.util.List;

/**
 * Created by habbyge on 2019/10/29.
 * todo
 *  屏蔽MapGenClass中的ProxyInfo细节，提供：
 *  （1）设置代理API
 *  （2）恢复旧对象API
 */
public final class MpGenProxy {
    private MpGenProxy() {
    }

    /**
     * 设置动态代理类
     */
    public void setProxy(Class<?> _class, List<Pair<String, String>> methodInfos, InvocationHandler handler) {
        ProxyInfo proxyInfo = MpGenClass.getProxy(_class, methodInfos, handler);
        if (proxyInfo == null) {
            return;
        }
    }

    /**
     * 恢复回旧的原始对象
     */
    public void restoreProxy() {
        // TODO: 2019/10/29
    }
}

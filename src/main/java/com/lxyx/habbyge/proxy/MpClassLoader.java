package com.lxyx.habbyge.proxy;

/**
 * Created by habbyge on 2019/10/12.
 * 芒果皮蛋ClassLoader
 * 自定义ClassLoader，用于运行期使用动态代理类的实例，以及旧实例
 */
public class MpClassLoader extends ClassLoader {
    private byte[] bytes;

    MpClassLoader() {
        super(null); // 阻止双亲机制
    }

    void setBytes(byte[] classBytes) {
        bytes = classBytes;
    }

    // 重写 findClass方法，加载指定文件，这个部分你可以自由发挥
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.defineClass(name, bytes, 0, bytes.length);
    }
}

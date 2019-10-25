# mpproxy
全新角度实现对任意Class的动态代理技术方案：动态生成被代理类的interface并实现(继承)之，再利用自定义ClassLoader，以及java中自带的Proxy机制，生成动态代理对象。

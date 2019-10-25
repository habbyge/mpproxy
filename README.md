# mpproxy
全新角度实现对任意Class的动态代理技术方案：动态生成被代理类的interface并实现(继承)之，再利用自定义ClassLoader，以及java中自带的Proxy机制，生成动态代理对象。    
# 与业界方案对比优点             
1、java自带的Proxy只能用于那些实现了interface的class，能力上是受到限制的，不够灵活，这个方法即是对其的扩展，更加通用，几乎无限制使用动态代理能力；     
2、cglib库中提供的动态代理技术，是通过生成被代理class的子类方式来实现，而本文的方案是反其道而行之，通过生成被代理class的interface的方式来实现，本方案提供的jar更加小巧，对外提供的API也更加简洁友好：只需要传入被代理类的class对象、以及该class中需要被代理的方法及其签名即可。          
# ***接下来会补充如何使用，以及如何运行demo***     

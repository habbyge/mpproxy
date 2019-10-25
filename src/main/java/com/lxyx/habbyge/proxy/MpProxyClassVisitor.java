package com.lxyx.habbyge.proxy;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Created by habbyge on 2019/10/12.
 */
final class MpProxyClassVisitor extends ClassVisitor {
    private String mInterface;

    MpProxyClassVisitor(ClassWriter cw, String _interface) {
        super(Opcodes.ASM5, cw);
        mInterface = _interface;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {

        if (mInterface != null) {
            boolean hadIntferfaceYet = false;
            for (String _interface : interfaces) {
                if (mInterface.equals(_interface)) {
                    hadIntferfaceYet = true;
                    break;
                }
            }
            if (hadIntferfaceYet) { // 避免重复 ！！！
                System.out.println("_LXYX_ TestProxyClassVisitor, visit, hadIntferfaceYet, YES: " + mInterface);
                super.visit(version, access, name, signature, superName, interfaces);
                return;
            }
            System.out.println("_LXYX_ TestProxyClassVisitor, visit, hadIntferfaceYet, NO: " + mInterface);

            final String[] newInterfaces = new String[interfaces.length + 1];
            newInterfaces[interfaces.length] = mInterface; // 代理接口名
            super.visit(version, access, name, signature, superName, newInterfaces);
        } else {
            super.visit(version, access, name, signature, superName, interfaces);
        }
    }
}

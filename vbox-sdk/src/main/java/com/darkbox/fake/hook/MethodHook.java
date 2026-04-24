package com.darkbox.fake.hook;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import com.darkbox.VBoxCore;

public abstract class MethodHook {
    
    private static final Map<Long, HookRecord> sHookRecords = new ConcurrentHashMap<>();
    
    protected String getMethodName() {
        return null;
    }

    protected Object afterHook(Object result) throws Throwable {
        return result;
    }

    protected Object beforeHook(Object who, Method method, Object[] args) throws Throwable {
        return null;
    }

    protected abstract Object hook(Object who, Method method, Object[] args) throws Throwable;

    protected boolean isEnable() {
        return VBoxCore.get().isBlackProcess();
    }
    
    private static HookHandler sHookHandler = new HookHandler() {
        @Override
        public MethodHook.Unhook handleHook(HookRecord hookRecord, MethodHook hook, int modifiers,
                                boolean newMethod, boolean canInitDeclaringClass) {
            if (newMethod)
                hookNewMethod(hookRecord, modifiers, canInitDeclaringClass);

            if (hook == null) {
                return null;
            }
            hookRecord.addCallback(hook);
            return hook.new Unhook(hookRecord);
        }

        @Override 
        public void handleUnhook(HookRecord hookRecord, MethodHook hook) {
            hookRecord.removeCallback(hook);
        }
        
        private void hookNewMethod(HookRecord hookRecord, int modifiers, boolean canInitDeclaringClass) {
            try {
                Member target = hookRecord.target;
                Class<?> declaringClass = target.getDeclaringClass();
                
                if (canInitDeclaringClass) {
                    try {
                        Class.forName(declaringClass.getName(), true, declaringClass.getClassLoader());
                    } catch (ClassNotFoundException ignored) {}
                }
                
                if (target instanceof Method) {
                    Method original = (Method) target;
                    hookRecord.backup = original;
                    hookRecord.isStatic = Modifier.isStatic(modifiers);
                    hookRecord.paramNumber = original.getParameterCount();
                    hookRecord.paramTypes = original.getParameterTypes();
                    
                    // Replace method implementation using reflection
                    Field field = Method.class.getDeclaredField("methodAccessor");
                    field.setAccessible(true);
                    field.set(original, new MethodAccessorProxy(original, hookRecord));
                }
            } catch (Throwable e) {
                throw new RuntimeException("Failed to hook method", e);
            }
        }
    };
    
    private static class MethodAccessorProxy implements InvocationHandler {
        private final Method original;
        private final HookRecord hookRecord;
        
        public MethodAccessorProxy(Method original, HookRecord hookRecord) {
            this.original = original;
            this.hookRecord = hookRecord;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object who = null;
            Object[] actualArgs = args;
            
            if (!hookRecord.isStatic) {
                who = args[0];
                actualArgs = new Object[args.length - 1];
                System.arraycopy(args, 1, actualArgs, 0, actualArgs.length);
            }
            
            // Call before hooks
            for (MethodHook callback : hookRecord.getCallbacks()) {
                Object result = callback.beforeHook(who, original, actualArgs);
                if (result != null) {
                    return callback.afterHook(result);
                }
            }
            
            // Invoke original method
            Object result;
            try {
                result = original.invoke(who, actualArgs);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
            
            // Call after hooks
            for (MethodHook callback : hookRecord.getCallbacks()) {
                result = callback.afterHook(result);
            }
            
            return result;
        }
    }
    
    public interface HookHandler {
        MethodHook.Unhook handleHook(HookRecord hookRecord, MethodHook hook, int modifiers,
                                   boolean newMethod, boolean canInitDeclaringClass);
        void handleUnhook(HookRecord hookRecord, MethodHook hook);
    }
    
    public class Unhook {
        private final HookRecord hookRecord;

        public Unhook(HookRecord hookRecord) {
            this.hookRecord = hookRecord;
        }

        public Member getTarget() {
            return hookRecord.target;
        }

        public MethodHook getCallback() {
            return MethodHook.this;
        }

        public void unhook() {
            getHookHandler().handleUnhook(hookRecord, MethodHook.this);
        }
    }
    
    public static HookHandler getHookHandler() {
        return sHookHandler;
    }
    
    public static final class HookRecord {
        public final Member target;
        public final long artMethod;
        public Method backup;
        public boolean isStatic;
        public int paramNumber;
        public Class<?>[] paramTypes;
        private Set<MethodHook> callbacks = new HashSet<>();

        public HookRecord(Member target, long artMethod) {
            this.target = target;
            this.artMethod = artMethod;
        }

        public synchronized void addCallback(MethodHook callback) {
            callbacks.add(callback);
        }

        public synchronized void removeCallback(MethodHook callback) {
            callbacks.remove(callback);
        }

        public synchronized boolean emptyCallbacks() {
            return callbacks.isEmpty();
        }

        public synchronized MethodHook[] getCallbacks() {
            return callbacks.toArray(new MethodHook[callbacks.size()]);
        }

        public boolean isPending() {
            return backup == null;
        }
    }
    
    public static HookRecord getHookRecord(long artMethod) {
        HookRecord result = sHookRecords.get(artMethod);
        if (result == null) {
            throw new AssertionError("No HookRecord found for ArtMethod pointer 0x" + Long.toHexString(artMethod));
        }
        return result;
    }
    
    public static void addHookRecord(long artMethod, HookRecord record) {
        sHookRecords.put(artMethod, record);
    }
    
    public static void removeHookRecord(long artMethod) {
        sHookRecords.remove(artMethod);
    }
    
    public static boolean hasHookRecord(long artMethod) {
        return sHookRecords.containsKey(artMethod);
    }
}
/*
 * Copyright 2023 JMount contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package snw.jmount.impl.jdk.handle;

import snw.jmount.Mount;
import snw.jmount.common.util.UncheckedFunction;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static snw.jmount.common.util.MountUtils.*;
import static snw.jmount.common.util.ReflectUtils.perform;

/**
 * The {@link InvocationHandler} implementation for handling Mount Point interface method calls.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public class MountInvocationHandler implements InvocationHandler {
    protected final Mount mount;
    protected final Object origin;
    protected final Map<Method, UncheckedFunction<Object[], Object>> invokerMap = new ConcurrentHashMap<>();
    protected final WeakHashMap<Object, Object> resultCache = new WeakHashMap<>();
    protected final Lock cacheLock = new ReentrantLock();

    public MountInvocationHandler(Mount mount, Object origin) {
        this.mount = mount;
        this.origin = origin;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final UncheckedFunction<Object[], Object> invoker = invokerMap.computeIfAbsent(method, this::generate);
        return invoker.apply(args);
    }

    public Object getOrigin() {
        return origin;
    }

    private UncheckedFunction<Object[], Object> generate(Method method) {
        final Method underlyingMethod = convertMethod(method, mount);
        underlyingMethod.setAccessible(true);
        final Class<?>[] paramTypes = method.getParameterTypes();
        final Class<?> returnType = method.getReturnType();
        final MethodHandle handle = perform(() -> MethodHandles.lookup().unreflect(underlyingMethod)).bindTo(origin);
        return args -> {
            Object o = handle.invoke(convert(mount, paramTypes, args));
            if (isMP(returnType)) {
                cacheLock.lock();
                try {
                    if (resultCache.containsKey(o)) {
                        return resultCache.get(o);
                    } else {
                        Object mounted = mount.mount(returnType, o);
                        resultCache.put(o, mounted);
                        return mounted;
                    }
                } finally {
                    cacheLock.unlock();
                }
            }
            return o;
        };
    }
}

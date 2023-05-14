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

package snw.jmount.common.handle;

import org.jetbrains.annotations.Nullable;
import snw.jmount.Mount;
import snw.jmount.common.util.UncheckedFunction;
import snw.jmount.handle.FieldAccessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static snw.jmount.common.util.MountUtils.*;
import static snw.jmount.common.util.MountUtils.isMP;
import static snw.jmount.common.util.ReflectUtils.lookUpField;
import static snw.jmount.common.util.ReflectUtils.perform;

/**
 * The invoker for invoking methods in the underlying classes.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public class Invoker {
    protected final Mount mount;
    protected final AtomicReference<Object> underlyingRef;
    protected final Map<Method, UncheckedFunction<Object[], Object>> invokerMap = new ConcurrentHashMap<>();
    protected final WeakHashMap<Object, Object> resultCache = new WeakHashMap<>();
    protected final Lock cacheLock = new ReentrantLock();

    protected Invoker(Mount mount, AtomicReference<Object> underlyingRef) {
        this.mount = mount;
        this.underlyingRef = underlyingRef;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final UncheckedFunction<Object[], Object> invoker = invokerMap.computeIfAbsent(method, i -> generate(method));
        return invoker.apply(args);
    }

    public Object getOrigin() {
        return underlyingRef.get();
    }

    private UncheckedFunction<Object[], Object> generate(Method method) {
        final Object origin = underlyingRef.get();
        if (isFieldAccessor(method)) {
            return generateFieldAccessor(method);
        }
        final Method underlyingMethod = convertMethod(method, mount);
        underlyingMethod.setAccessible(true);
        final Class<?>[] paramTypes = method.getParameterTypes();
        final Class<?> returnType = method.getReturnType();
        final MethodHandle handle = perform(() -> MethodHandles.lookup().unreflect(underlyingMethod)).bindTo(origin);
        return args -> {
            // args is null if the Mount Point instance is mounting an enum constant.
            Object o = args == null ? invoke(handle, null) : invoke(handle, convert(mount, paramTypes, args));
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


    protected UncheckedFunction<Object[], Object> generateFieldAccessor(Method method) {
        final Object origin = underlyingRef.get();
        final Field field = lookUpField(
                mount.findOriginClass(method.getDeclaringClass()),
                getTargetFieldNameWithPattern(method),
                mount
        );
        final Type genericType = method.getGenericReturnType();
        if (genericType instanceof ParameterizedType) { // require FieldAccessor
            if (((ParameterizedType) genericType).getRawType() == FieldAccessor.class) {
                Class<?> type = ((Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0]);
                final MethodHandleBasedFieldAccessor<?> accessor = new MethodHandleBasedFieldAccessor<>(mount, origin, type, field);
                return args -> accessor;
            }
        }
        if (method.getReturnType() == void.class) { // setter
            final MethodHandleBasedFieldAccessor<?> accessor = new MethodHandleBasedFieldAccessor<>(mount, origin, null, field);
            return args -> {
                accessor.set(args[0]);
                return null;
            };
        }

        Class<?> type = method.getReturnType();
        final MethodHandleBasedFieldAccessor<?> accessor = new MethodHandleBasedFieldAccessor<>(mount, origin, type, field);
        if (isMP(method.getReturnType())) {
            return args -> accessor.getMounted();
        }
        return args -> accessor.get();
    }

    protected Object invoke(MethodHandle boundHandle, @Nullable Object[] convertedArgs) throws Throwable {
        return convertedArgs == null ? boundHandle.invoke() : boundHandle.invoke(convertedArgs);
    }
}

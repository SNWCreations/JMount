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

package snw.jmount.impl.bytebuddy.handle;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import snw.jmount.common.handle.Invoker;
import snw.jmount.impl.bytebuddy.ByteBuddyMount;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The {@link Invoker} implementation with ByteBuddy support.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public class ByteBuddyInterceptor extends Invoker {

    public ByteBuddyInterceptor(ByteBuddyMount mount, AtomicReference<Object> underlyingRef) {
        super(mount, underlyingRef);
    }

    @RuntimeType
    public Object intercept(@This Object proxyItself, @Origin Method method, @AllArguments Object[] args)
            throws Throwable {
        underlyingRef.updateAndGet(i -> i != null ? i : mount.unmount(proxyItself));
        return invoke(proxyItself, method, args);
    }

}

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

package snw.jmount.impl.jdk;

import snw.jmount.common.AbstractMount;
import snw.jmount.common.AbstractMountBuilder;
import snw.jmount.impl.jdk.handle.MountInvocationHandler;

import java.lang.reflect.Proxy;

/**
 * The {@link snw.jmount.Mount} implementation based on pure JDK things.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public class JDKMount extends AbstractMount {

    public JDKMount(AbstractMountBuilder builder) {
        super(builder);
    }

    @Override
    public <T> T mount(Class<T> mpType, Object origin) throws IllegalArgumentException {
        if (!findOriginClass(mpType).isAssignableFrom(origin.getClass())) {
            throw new IllegalArgumentException("The target Mount Point type is not compatible with the provided object");
        }
        verify(mpType);
        // noinspection unchecked
        return (T) Proxy.newProxyInstance(
                classLoader(), new Class[]{mpType}, new MountInvocationHandler(this, origin)
        );
    }

    @Override
    public Object unmount(Object mp) throws IllegalArgumentException {
        return ((MountInvocationHandler) Proxy.getInvocationHandler(mp)).getOrigin();
    }

}

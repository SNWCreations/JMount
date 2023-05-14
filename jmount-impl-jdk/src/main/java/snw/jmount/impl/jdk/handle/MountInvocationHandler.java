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
import snw.jmount.common.handle.Invoker;

import java.lang.reflect.InvocationHandler;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The {@link InvocationHandler} implementation for handling Mount Point interface method calls.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public class MountInvocationHandler extends Invoker implements InvocationHandler {

    public MountInvocationHandler(Mount mount, Object origin) {
        super(mount, new AtomicReference<>(origin));
    }

}

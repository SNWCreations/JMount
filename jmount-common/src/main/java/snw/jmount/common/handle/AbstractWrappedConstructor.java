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

import snw.jmount.handle.WrappedConstructor;

import java.lang.reflect.Constructor;

/**
 * A shared implementation of {@link WrappedConstructor} with partly completed feature.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public abstract class AbstractWrappedConstructor implements WrappedConstructor {
    protected final Constructor<?> underlying;

    protected AbstractWrappedConstructor(Constructor<?> underlying) {
        this.underlying = underlying;
        this.underlying.setAccessible(true);
    }

    @Override
    public Constructor<?> getUnderlyingConstructor() {
        return underlying;
    }
}

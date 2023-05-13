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

package snw.jmount.common;

import snw.jmount.MountBuilder;
import snw.jmount.NameTransformer;

import java.util.Objects;

/**
 * A shared {@link snw.jmount.MountBuilder} implementation with partly completed feature.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public abstract class AbstractMountBuilder implements MountBuilder {
    protected NameTransformer nameTransformer;
    protected ClassLoader classLoader;

    @Override
    public MountBuilder classLoader(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "ClassLoader cannot be null");
        return this;
    }

    @Override
    public MountBuilder nameTransformer(NameTransformer nameTransformer) {
        this.nameTransformer = Objects.requireNonNull(nameTransformer, "Name transformer cannot be null");
        return this;
    }

}

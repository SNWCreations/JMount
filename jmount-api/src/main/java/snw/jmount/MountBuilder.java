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

package snw.jmount;

import org.jetbrains.annotations.Contract;
import snw.jmount.spi.MountBuilderProvider;

/**
 * The {@link Mount} object builder.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public interface MountBuilder {

    /**
     * Set the class loader of this builder.
     *
     * @param classLoader The class loader
     * @return This builder
     */
    @Contract("null -> fail; _ -> this")
    MountBuilder classLoader(ClassLoader classLoader);

    /**
     * Set the name transformer of this builder.
     *
     * @param nameTransformer The name transformer
     * @return This builder
     */
    @Contract("null -> fail; _ -> this")
    MountBuilder nameTransformer(NameTransformer nameTransformer);

    /**
     * Build a new {@link Mount} object.
     *
     * @return The new {@link Mount} object
     */
    @Contract("-> new")
    Mount build();

    /**
     * Create a new builder instance.
     *
     * @return The new builder instance
     */
    @Contract("-> new")
    static MountBuilder create() {
        return MountBuilderProvider.getInstance().create();
    }

}

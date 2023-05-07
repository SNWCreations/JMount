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

package snw.jmount.spi;

import snw.jmount.MountBuilder;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The SPI interface for providing {@link snw.jmount.MountBuilder} objects.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public abstract class MountBuilderProvider {
    private static final AtomicReference<MountBuilderProvider> INSTANCE = new AtomicReference<>();

    /**
     * @see MountBuilder#create()
     */
    public abstract MountBuilder create();

    public static MountBuilderProvider getInstance() {
        return INSTANCE.updateAndGet(i -> {
            if (i == null) {
                final Iterator<MountBuilderProvider> iterator =
                        ServiceLoader.load(MountBuilderProvider.class).iterator();
                if (iterator.hasNext()) {
                    return iterator.next();
                } else {
                    throw new NoSuchElementException("No MountBuilder provider available!");
                }
            }
            return i;
        });
    }
}

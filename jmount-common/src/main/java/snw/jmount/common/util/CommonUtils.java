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

package snw.jmount.common.util;

import org.jetbrains.annotations.Nullable;

/**
 * A set of utility methods, but they are common, can not only used in this library, but also everywhere!
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public final class CommonUtils {

    private CommonUtils() {
    }

    /**
     * Convert the provided primitive wrapper type to the real primitive type.
     *
     * @param wrapper The wrapper type
     * @return The primitive type, or null if not a wrapper type
     */
    public static @Nullable Class<?> wrapperToPrimitive(Class<?> wrapper) {
        if (wrapper == Boolean.class) {
            return boolean.class;
        } else if (wrapper == Character.class) {
            return char.class;
        } else if (wrapper == Byte.class) {
            return byte.class;
        } else if (wrapper == Short.class) {
            return short.class;
        } else if (wrapper == Integer.class) {
            return int.class;
        } else if (wrapper == Long.class) {
            return long.class;
        } else if (wrapper == Double.class) {
            return double.class;
        } else if (wrapper == Void.class) {
            return void.class;
        }
        return null; // Unrecognized
    }
}

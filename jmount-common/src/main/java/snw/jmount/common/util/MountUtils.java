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

import snw.jmount.Mount;
import snw.jmount.annotation.MountPoint;

import static snw.jmount.common.util.ReflectUtils.perform;

/**
 * A set of utility methods related to {@link Mount} logics.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public final class MountUtils {
    private MountUtils() {
    }

    /**
     * Check if the provided class is a Mount Point. (won't check if it is a valid Mount Point)
     *
     * @param clazz The class to be checked
     * @return True if the class is a Mount Point.
     */
    public static boolean isMP(Class<?> clazz) {
        return clazz.isAnnotationPresent(MountPoint.class);
    }

    /**
     * Check the provided class if it is a Mount Point and do nothing, otherwise this method fails.
     *
     * @param clazz The class to be checked
     */
    public static void checkIfIsMP(Class<?> clazz) throws IllegalArgumentException {
        if (!isMP(clazz)) {
            throw new IllegalArgumentException("Provided class " + clazz + " is not a Mount Point interface");
        }
    }

    /**
     * Convert the provided class into the underlying type if needed.
     *
     * @param maybeMP The class will be converted
     * @param mount The mount object used for looking up the underlying class and replacing patterns
     * @return The underlying class or {@code maybeMP} itself
     */
    public static Class<?> convertOrReturn(Class<?> maybeMP, Mount mount) {
        if (!isMP(maybeMP)) {
            return maybeMP;
        }
        return convertToUnderlyingClass(maybeMP, mount);
    }

    /**
     * Convert the provided class into the underlying type, or fail if not a Mount Point.
     *
     * @param mp The Mount Point class
     * @param mount The mount object used for looking up the underlying class and replacing patterns
     * @return The underlying class
     */
    public static Class<?> convertToUnderlyingClass(Class<?> mp, Mount mount) {
        checkIfIsMP(mp);
        final MountPoint annotation = mp.getAnnotation(MountPoint.class);
        final String pattern = annotation.value();
        final String transformedName = mount.nameTransformer().transformClassName(pattern);
        return perform(() -> Class.forName(transformedName, false, mount.classLoader()));
    }
}

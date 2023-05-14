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
import snw.jmount.annotation.AccessField;
import snw.jmount.annotation.MountPoint;
import snw.jmount.annotation.Redirect;
import snw.jmount.handle.FieldAccessor;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static snw.jmount.common.util.ReflectUtils.*;

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
     * Check if the method is a field accessor.
     *
     * @param method The method to be checked
     * @return True if it is a field accessor
     */
    public static boolean isFieldAccessor(Method method) {
        return method.isAnnotationPresent(AccessField.class);
    }

    /**
     * Get the target field name of the provided method, but not converted yet. (known it is field accessor)
     *
     * @param m The field accessor method
     * @return The field name with pattern
     */
    public static String getTargetFieldNameWithPattern(Method m) {
        if (!isFieldAccessor(m)) {
            throw new IllegalArgumentException(m + " is not a field accessor");
        }
        String fieldName = m.getAnnotation(AccessField.class).value();
        if (fieldName == null || fieldName.isEmpty()) {
            fieldName = m.getName();
        }
        return fieldName;
    }

    /**
     * Check the provided method if it is a valid field accessor and do nothing, otherwise this method fails.
     *
     * @param m The method to be checked
     * @param mount The mount object used for looking up the underlying class
     */
    public static void checkIfIsFieldAccessor(Method m, Mount mount) {
        if (!isFieldAccessor(m)) {
            throw new IllegalArgumentException(m + " is not a field accessor");
        }
        final Class<?> underlyingClass = mount.findOriginClass(m.getDeclaringClass());

        final String fieldNameWithPattern = getTargetFieldNameWithPattern(m);

        final Field underlyingField = lookUpField(underlyingClass, fieldNameWithPattern, mount);

        final Type returnType = m.getGenericReturnType();
        if (returnType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) returnType;
            if (parameterizedType.getRawType() == FieldAccessor.class) {
                final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length != 1) {
                    invalidFieldAccessor(m, "impossible: FieldAccessor detected, but argument length is not equals to 1");
                }
                final Type actualTypeArgument = actualTypeArguments[0];
                if (actualTypeArgument instanceof Class) {
                    if (!convertOrReturn(((Class<?>) actualTypeArgument), mount).isAssignableFrom(underlyingField.getType())) {
                        invalidFieldAccessor(m, "field accessors only accepts Mount Point types, primitive types or ? as its type variable");
                    }
                } else if (actualTypeArgument instanceof WildcardType) {
                    final WildcardType wildcardType = (WildcardType) actualTypeArgument;
                    if (!(
                            wildcardType.getLowerBounds().length == 0 &&
                                    wildcardType.getUpperBounds().length == 1 &&
                                    wildcardType.getUpperBounds()[0] == Object.class
                            )) {
                        invalidFieldAccessor(m, "field accessors only accepts Mount Point types, primitive types or ? as its type variable");
                    }
                }
                return; // valid FieldAccessor, prevent return type check
            }
        } else if (returnType == void.class) {
            if (m.getParameterCount() == 0) {
                invalidFieldAccessor(m, "it is impossible to declare a field with void type");
            } else if (m.getParameterCount() != 1) {
                invalidFieldAccessor(m, "not a valid setter, too many arguments");
            } else if (!underlyingField.getType().isAssignableFrom(convertOrReturn(m.getParameterTypes()[0], mount))) {
                invalidFieldAccessor(m, "not a valid setter, the provided type is not compatible with underlying type");
            }
            return;
        }

        final Class<?> ourReturnType = convertOrReturn(m.getReturnType(), mount);
        final Class<?> realReturnType = underlyingField.getType();
        if (!ourReturnType.isAssignableFrom(realReturnType)) {
            invalidFieldAccessor(
                    m, "underlying field type " + realReturnType
                            + " is not compatible with the declared type (" + ourReturnType
                            + ") of the accessor"
            );
        }
    }

    /*
       Only for internal use: always throw an IllegalArgumentException, used by checkIfIsFieldAccessor method.
     */
    private static void invalidFieldAccessor(Method method, String msg) {
        throw new IllegalArgumentException(
                method + " is not a valid field accessor: " + msg
        );
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
     * Convert the Mount Point object into the underlying object if needed.
     *
     * @param maybeMP An object
     * @param mount The mount object used for looking up the underlying class
     * @return The converted object if the provided object is a Mount Point instance, or the {@code maybeMP} itself
     */
    public static Object convertOrReturn(Object maybeMP, Mount mount) {
        if (!mount.isMount(maybeMP)) {
            return maybeMP;
        }
        return mount.unmount(maybeMP);
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

    /**
     * Convert all classes in the provided array into origin classes.
     *
     * @param mount The mount object used for looking up the underlying class
     * @param classes The classes to be converted
     * @return The converted classes
     */
    public static Class<?>[] convert(Mount mount, Class<?>... classes) {
        List<Class<?>> list = new ArrayList<>(classes.length);
        for (Class<?> clazz : classes) {
            list.add(convertOrReturn(clazz, mount));
        }
        return list.toArray(new Class[0]);
    }

    /**
     * Convert all objects in the provided array into origin ones.
     *
     * @param mount The mount object used for looking up the underlying class
     * @param argTypes The argument type for checking
     * @param objects The objects to be converted
     * @return The converted objects
     */
    public static Object[] convert(Mount mount, Class<?>[] argTypes, Object[] objects) {
        List<Object> list = new ArrayList<>(objects.length);
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            Object adding = null;
            if (object != null) {
                if (isMP(object.getClass())) {
                    if (argTypes[i].isAssignableFrom(mount.findOriginClass(object.getClass()))){
                        adding = mount.unmount(object);
                    } else {
                        throw new IllegalArgumentException(
                                "Provided object is not compatible with the provided argument types"
                        );
                    }
                } else if (!argTypes[i].isAssignableFrom(object.getClass())) {
                    throw new IllegalArgumentException(
                            "Provided object is not compatible with the provided argument types"
                    );
                } else {
                    adding = object;
                }
            }
            list.add(adding);
        }
        return list.toArray();
    }

    /**
     * Look up a method matches the underlying class.
     *
     * @param method The method from Mount Point
     * @param mount The mount object used for looking up the underlying class
     * @return The method matches the requirement of the provided method
     * @throws NoSuchElementException See {@link ReflectUtils#matchMethod}
     */
    public static Method convertMethod(Method method, Mount mount) throws NoSuchElementException {
        Class<?> underlyingClazz = mount.findOriginClass(method.getDeclaringClass());
        String methodName;
        if (method.isAnnotationPresent(Redirect.class)) {
            methodName = method.getAnnotation(Redirect.class).value();
        } else {
            methodName = method.getName();
        }
        methodName = mount.nameTransformer().transformMethodName(
                underlyingClazz.getName(), methodName,
                convert(mount, method.getParameterTypes())
        );
        Parameter[] parameterTypes = method.getParameters();
        Class<?> returnType = method.getReturnType();
        return matchMethod(
                underlyingClazz, methodName, parameterTypes, returnType, mount
        );
    }
}

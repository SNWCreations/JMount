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
import snw.jmount.Mount;
import snw.jmount.annotation.RuntimeType;
import snw.jmount.common.exceptions.ReflectOperationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static snw.jmount.common.util.MountUtils.convertOrReturn;

/**
 * A set of utility methods for calling Java reflect API.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public final class ReflectUtils {

    // You should NOT call constructor.
    private ReflectUtils() {
    }

    /**
     * Try to look for an method in the specified underlying class. <br>
     * Provided argument types and return type will be converted into underlying type if needed.
     *
     * @param underlyingClass The specified underlying class
     * @param methodName The method name
     * @param argTypes The array of argument type
     * @param returnType The expected return type of the resulting method
     * @param mount The mount object used for looking up the underlying classes
     * @return The method
     * @throws NoSuchElementException Thrown if no method in the specified class matches the requirement
     */
    public static Method matchMethod(
            Class<?> underlyingClass,
            String methodName,
            Parameter[] argTypes,
            Class<?> returnType,
            Mount mount
    ) throws NoSuchElementException {
        final Method[] methods = getAllMethodsWithExactName(underlyingClass, methodName);

        for (Method method : methods) {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (argTypes.length != parameterTypes.length) {
                continue;
            }
            if (!isCompatible(parameterTypes, argTypes, mount)) {
                continue;
            }
            if (!method.isAnnotationPresent(RuntimeType.class)) {
                final Class<?> methodReturnType = method.getReturnType();
                if (!convertOrReturn(methodReturnType, mount).isAssignableFrom(methodReturnType)) {
                    continue;
                }
            }
            return method;
        }

        throw new NoSuchElementException(
                "No method in " + underlyingClass + " matches "
                        + toMethodSignature(methodName, argTypes, returnType)
        );
    }

    /**
     * Find constructor with the specified argument types,
     *  will convert argument types into underlying types if needed.
     *
     * @param originClazz The underlying class
     * @param argTypes The argument types
     * @param mount The {@link Mount} objects
     * @return The constructor matches the requirement specified by the caller
     * @throws NoSuchElementException Thrown if no constructor matches the requirement
     */
    public static Constructor<?> lookUpConstructor(
            Class<?> originClazz,
            Class<?>[] argTypes,
            Mount mount
    ) throws NoSuchElementException {
        final Constructor<?>[] constructors = originClazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (isCompatible(parameterTypes, argTypes, mount)) {
                return constructor;
            }
        }
        throw new NoSuchElementException(
                "No constructor in " + originClazz + " matches " + toParamSignature(argTypes)
        );
    }

    /**
     * Check if the classes in {@code classes} array is compatible with the classes in {@code anotherClassArray}.
     *
     * @param classes The classes as the comparison source
     * @param paramArray The classes to be checked, it can contain Mount Points
     * @param mount The {@link Mount} for looking up the underlying class
     * @return True if compatible
     * @throws IllegalArgumentException Thrown if the length of two arrays is not equal
     */
    public static boolean isCompatible(Class<?>[] classes, Parameter[] paramArray, Mount mount)
            throws IllegalArgumentException
    {
        if (classes.length != paramArray.length) {
            throw new IllegalArgumentException("Length is not equal");
        }
        for (int i = 0; i < classes.length; i++) {
            final Parameter paramTypeFromArgs = paramArray[i];
            if (paramTypeFromArgs.isAnnotationPresent(RuntimeType.class)) {
                continue;
            }
            final Class<?> paramType = classes[i];
            if (!paramType.isAssignableFrom(convertOrReturn(paramTypeFromArgs.getType(), mount))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the classes in {@code classes} array is compatible with the classes in {@code anotherClassArray}.
     *
     * @param classes The classes as the comparison source
     * @param anotherClassArray The classes to be checked, it can contain Mount Points
     * @param mount The {@link Mount} for looking up the underlying class
     * @return True if compatible
     * @throws IllegalArgumentException Thrown if the length of two arrays is not equal
     */
    public static boolean isCompatible(Class<?>[] classes, Class<?>[] anotherClassArray, Mount mount)
            throws IllegalArgumentException
    {
        if (classes.length != anotherClassArray.length) {
            throw new IllegalArgumentException("Length is not equal");
        }
        for (int i = 0; i < classes.length; i++) {
            final Class<?> paramType = classes[i];
            final Class<?> paramTypeFromArgs = anotherClassArray[i];
            if (!paramType.isAssignableFrom(convertOrReturn(paramTypeFromArgs, mount))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get all methods with the specified name in the specified class.
     *
     * @param clazz The class object
     * @param methodName The method name as the condition
     * @return An array of methods matching the requirement
     */
    public static Method[] getAllMethodsWithExactName(Class<?> clazz, String methodName) {
        return getAllMethods(clazz, m -> m.getName().equals(methodName));
    }

    /**
     * Look for all methods in the specified class, including methods in superclasses.
     *
     * @param clazz The specified class
     * @param condition The condition for check the method if it should be added to the result array
     * @return The methods
     */
    public static Method[] getAllMethods(Class<?> clazz, @Nullable Predicate<Method> condition) {
        Collection<Method> methods = new LinkedList<>();
        while (clazz != Object.class) { // do not look for methods in Object
            for (Method m : clazz.getDeclaredMethods()) {
                if (condition == null || condition.test(m)) {
                    methods.add(m);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methods.toArray(new Method[0]);
    }

    /**
     * Perform the provided action, return its return value. <br>
     * Any exception will be wrapped into {@link ReflectOperationException}.
     *
     * @param callable The callable
     * @return The return value of the provided callable
     * @param <T> The return type of the provided callable
     * @throws ReflectOperationException Thrown if any exception occurred during the execution of the provided callable
     */
    public static <T> T perform(UncheckedCallable<T> callable) throws ReflectOperationException {
        try {
            return callable.call();
        } catch (Throwable e) {
            throw new ReflectOperationException("Cannot perform action", e);
        }
    }

    /**
     * Create a method signature string (without modifiers).
     *
     * @param methodName The method name
     * @param argTypes The argument types
     * @param returnType The return type
     * @return The method signature string
     */
    public static String toMethodSignature(String methodName, Parameter[] argTypes, Class<?> returnType) {
        return String.valueOf(returnType) +
                ' ' +
                methodName +
                '(' +
                toParamSignature(argTypes) +
                ')';
    }

    /**
     * Joining the provided argument type into a string.
     *
     * @param argTypes An array of the {@link Class} objects
     * @return The string
     */
    public static String toParamSignature(Class<?>... argTypes) {
        return Arrays.stream(argTypes).map(Class::getTypeName).collect(Collectors.joining(", "));
    }

    /**
     * Joining the provided argument type into a string.
     *
     * @param parameters An array of the {@link Parameter} objects
     * @return The string
     */
    public static String toParamSignature(Parameter... parameters) {
        final StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(
                Arrays.stream(parameters).map(i -> {
                    if (i.isAnnotationPresent(RuntimeType.class)) {
                        return "@RuntimeType " + i;
                    }
                    return i.getType().getName();
                }).collect(Collectors.joining(", "))
        );
        sb.append(')');
        return sb.toString();
    }

    /**
     * Look up field in the underlying class.
     *
     * @param underlyingClass The underlying class
     * @param fieldNameWithPattern The field name with pattern
     * @param mount The {@link Mount} for replacing patterns
     * @return The field
     */
    public static Field lookUpField(Class<?> underlyingClass, String fieldNameWithPattern, Mount mount) {
        final String fieldName =
                mount.nameTransformer().transformFieldName(underlyingClass.getName(), fieldNameWithPattern);
        return perform(() -> underlyingClass.getDeclaredField(fieldName));
    }
}

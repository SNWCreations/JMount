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

/**
 * The name transformer.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public interface NameTransformer {

    /**
     * Transform the class name into the exact class name.
     *
     * @param classNameWithPattern The class name with patterns need to be replaced
     * @return The exact class name
     */
    String transformClassName(String classNameWithPattern);

    /**
     * Transform the field name into the exact field name.
     *
     * @param originClassName      The exact origin class name
     * @param fieldNameWithPattern The field name with patterns need to be replaced
     * @return The exact field name
     */
    default String transformFieldName(String originClassName, String fieldNameWithPattern) {
        return fieldNameWithPattern;
    }

    /**
     * Transform the method name into the exact method name.
     *
     * @param originClassName       The exact origin class name
     * @param methodNameWithPattern The method name with patterns need to be replaced
     * @param convertedArgTypes     The argument types, already converted into the origin classes
     * @return The exact method name
     */
    default String transformMethodName(String originClassName, String methodNameWithPattern, Class<?>[] convertedArgTypes) {
        return methodNameWithPattern;
    }

}

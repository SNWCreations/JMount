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

import org.junit.jupiter.api.Test;
import snw.jmount.common.exceptions.ReflectOperationException;
import snw.jmount.common.testobjects.Something;
import snw.jmount.common.testobjects.SomethingMP;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static snw.jmount.common.util.ReflectUtils.*;

public class ReflectUtilsTest {
    @Test
    void getAllMethodsWithExactNameTest() {
        assertEquals(1, getAllMethodsWithExactName(SomethingMP.class, "doSomething").length);
    }

    @Test
    void getAllMethodsTest() {
        assertEquals(3, getAllMethods(SomethingMP.class, null).length);
    }

    @Test
    void performTest() {
        assertThrows(ReflectOperationException.class, () -> perform(() -> {
            throw new RuntimeException();
        }));
        assertDoesNotThrow(() -> perform(() -> null));
    }

    @Test
    void forceSetTest() {
        int expectedNewValue = 9;

        final Something something = new Something();
        final Field var0 = perform(() -> Something.class.getDeclaredField("var0"));
        assertDoesNotThrow(() -> forceSet(something, var0, expectedNewValue));
        assertEquals(expectedNewValue, perform(() -> var0.get(something)));;
    }
}

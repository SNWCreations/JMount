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
import snw.jmount.testobjects.Something;
import snw.jmount.testobjects.SomethingMP;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static snw.jmount.common.util.MountUtils.*;
import static snw.jmount.common.util.ReflectUtils.perform;

public class MountUtilsTest {
    @Test
    void isMountTest() {
        assertTrue(isMP(SomethingMP.class));
        assertFalse(isMP(Something.class));
        assertThrows(IllegalArgumentException.class, () -> checkIfIsMP(Something.class));
        assertDoesNotThrow(() -> checkIfIsMP(SomethingMP.class));
    }

    @Test
    void isFieldAccessorTest() {
        final Method getVar0 = perform(() -> SomethingMP.class.getDeclaredMethod("getVar0"));
        assertTrue(isFieldAccessor(getVar0));
        final Method doSomething = perform(() -> SomethingMP.class.getDeclaredMethod("doSomething"));
        assertFalse(isFieldAccessor(doSomething));
    }
}

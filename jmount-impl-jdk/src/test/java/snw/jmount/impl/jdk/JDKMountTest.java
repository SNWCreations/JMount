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

package snw.jmount.impl.jdk;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import snw.jmount.Mount;
import snw.jmount.MountBuilder;
import snw.jmount.handle.ConstructorMP;
import snw.jmount.handle.WrappedConstructor;
import snw.jmount.impl.jdk.testobjects.*;
import snw.jmount.impl.jdk.testobjects.mp.AnEnumMP;
import snw.jmount.impl.jdk.testobjects.mp.AnotherThingMP;
import snw.jmount.impl.jdk.testobjects.mp.SomethingMP;
import snw.jmount.impl.jdk.testobjects.origin.AnEnum;
import snw.jmount.impl.jdk.testobjects.origin.AnotherThing;
import snw.jmount.impl.jdk.testobjects.origin.Something;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;
import static snw.jmount.common.util.ReflectUtils.perform;

public class JDKMountTest {
    protected static Mount mount;

    @BeforeAll
    static void beforeAll() {
        mount = MountBuilder.create()
                .classLoader(Thread.currentThread().getContextClassLoader())
                .nameTransformer(new DummyNameTransformer())
                .build();
    }

    @Test
    void accessTest() {
        final Something underlying = new Something();
        SomethingMP mp = mount.mount(SomethingMP.class, underlying);
        assertEquals(underlying.object, mp.object().get());
        assertDoesNotThrow(() -> mp.object().set(new Object()));
        assertEquals(0, mp.getVar0());
        assertEquals(0, mp.anotherThing().getMounted().getA());

        assertFalse(underlying.did);
        mp.doSomething();
        assertTrue(underlying.did);
        mp.anotherRefToDoSomething();
        assertFalse(underlying.did);
    }

    @Test
    void constructorTest() {
        final Constructor<?> c = perform(() -> Something.class.getDeclaredConstructor(AnotherThing.class));
        final Constructor<?> cFromMount = mount.findConstructor(mount.findOriginClass(SomethingMP.class), AnotherThingMP.class);
        assertEquals(c, cFromMount);

        final WrappedConstructor wc = mount.findConstructorAndWrap(mount.findOriginClass(SomethingMP.class), AnotherThingMP.class);
        assertEquals(c, wc.getUnderlyingConstructor());

        final ConstructorMP<SomethingMP> mpc = mount.findConstructorAndWrapAsMP(SomethingMP.class, AnotherThingMP.class);
        assertEquals(wc.getUnderlyingConstructor(), mpc.getUnderlyingConstructor().getUnderlyingConstructor());

        final ConstructorMP<AnotherThingMP> atmpc = mount.findConstructorAndWrapAsMP(AnotherThingMP.class);
        final AnotherThingMP anotherThingMP = atmpc.newInstance();
        assertEquals(0, anotherThingMP.getA());
        final SomethingMP somethingMP = mpc.newInstance(anotherThingMP);

        /*
         * Why unmount?
         * The arguments passed to constructor will be converted to underlying object,
         * and the getMounted method will re-generate a new mount object instead of finding an existing one.
         * So we should compare the underlying object instead of comparing the Mount Point objects directly.
         */
        assertEquals(mount.unmount(anotherThingMP), somethingMP.anotherThing().get());

        int a = 6;
        final ConstructorMP<AnotherThingMP> atmpcWithInt = mount.findConstructorAndWrapAsMP(AnotherThingMP.class, int.class);
        final AnotherThingMP anotherThingMP1 = atmpcWithInt.newInstance(a);
        assertEquals(a, anotherThingMP1.getA());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void fillEnumTest() {
        assertNull(AnEnumMP.A);
        assertNull(AnEnumMP.B);

        mount.fillEnum(AnEnumMP.class);

        assertNotNull(AnEnumMP.A);
        assertNotNull(AnEnumMP.B);

        assertEquals(AnEnum.A.getStr(), AnEnumMP.A.getStr());
        assertEquals(AnEnum.B.getStr(), AnEnumMP.B.getStr());
    }
}

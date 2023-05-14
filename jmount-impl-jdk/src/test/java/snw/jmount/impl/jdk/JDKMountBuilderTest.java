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

import org.junit.jupiter.api.Test;
import snw.jmount.Mount;
import snw.jmount.MountBuilder;
import snw.jmount.impl.jdk.testobjects.DummyNameTransformer;

import static org.junit.jupiter.api.Assertions.*;

public class JDKMountBuilderTest {

    @Test
    void isJDKBuilderTest() {
        assertTrue(MountBuilder.create() instanceof JDKMountBuilder);
    }

    @Test
    void buildTest() {
        assertThrows(NullPointerException.class, () -> MountBuilder.create().build());
        assertDoesNotThrow(this::produce);
        assertTrue(() -> produce() instanceof JDKMount);
    }

    private Mount produce() {
        return MountBuilder.create()
                .classLoader(Thread.currentThread().getContextClassLoader())
                .nameTransformer(new DummyNameTransformer())
                .build();
    }

}

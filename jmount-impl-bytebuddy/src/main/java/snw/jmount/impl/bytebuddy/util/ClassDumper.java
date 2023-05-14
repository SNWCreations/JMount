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

package snw.jmount.impl.bytebuddy.util;

import net.bytebuddy.dynamic.DynamicType;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * The dumper for the classes generated by ByteBuddy. Only for debug use. <br>
 * To enable it, specify a JVM option (key value is at {@link #PROPERTY}) with a folder path as its value.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public final class ClassDumper {
    public static final Consumer<DynamicType.Unloaded<?>> DUMPER;
    private static final String PROPERTY = "snw.jmount.impl.bytebuddy.DUMP_LOCATION";
    private static final File FOLDER;

    static {
        final String folder = System.getProperty(PROPERTY, "");
        if (folder.isEmpty()) {
            DUMPER = b -> {}; // equals to /dev/null ?
            FOLDER = null;
        } else {
            final File theFolder = new File(folder).getAbsoluteFile();
            if (!theFolder.isDirectory()) {
                //noinspection ResultOfMethodCallIgnored
                theFolder.mkdirs();
            }
            DUMPER = ClassDumper::dump;
            FOLDER = theFolder;
        }
    }

    private ClassDumper() {
    }

    private static void dump(DynamicType.Unloaded<?> unloaded) {
        try {
            unloaded.saveIn(FOLDER);
        } catch (IOException ignored) { // do nothing
        }
    }

}

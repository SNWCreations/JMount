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

package snw.jmount.test.testobjects.mp;

import snw.jmount.annotation.MountPoint;

@MountPoint("snw.jmount.test.testobjects.origin.AnEnum")
public interface AnEnumMP {
    AnEnumMP A = null;
    AnEnumMP B = null;

    String getStr();
}

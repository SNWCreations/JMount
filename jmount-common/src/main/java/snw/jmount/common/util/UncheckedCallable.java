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

/**
 * A copy of {@link java.util.concurrent.Callable}, but {@link #call()} can throw {@link Throwable}.
 *
 * @param <V> The type of the result
 * @author SNWCreations
 * @since 0.1.0
 */
public interface UncheckedCallable<V> {

    /**
     * Call this callable, then return the result.
     *
     * @return The result
     * @throws Throwable Thrown if an exception occurred during the execution of this callable
     */
    V call() throws Throwable;
}

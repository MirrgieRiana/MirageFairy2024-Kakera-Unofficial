/*
 * Copyright 2022 MirrgieRiana
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

@file:Suppress("unused", "SpellCheckingInspection")

package mirrg.kotlin.hydrogen

inline fun unit(block: () -> Unit) = block()
inline fun <reified O : Any> Any?.castOr(default: () -> O) = this as? O ?: default()
inline fun <reified O : Any> Any?.castOrThrow() = this as O
inline fun <reified O : Any> Any?.castOrNull() = this as? O
inline fun <S> S?.or(default: () -> S) = this ?: default()

fun Boolean.toUnitOrNull() = if (this) Unit else null

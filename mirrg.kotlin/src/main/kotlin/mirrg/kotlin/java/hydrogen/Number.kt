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

package mirrg.kotlin.java.hydrogen

import java.math.BigDecimal
import java.math.BigInteger

// floor div/mod
infix fun Int.floorDiv(other: Int) = Math.floorDiv(this, other)
infix fun Long.floorDiv(other: Int) = Math.floorDiv(this, other)
infix fun Long.floorDiv(other: Long) = Math.floorDiv(this, other)
infix fun Int.floorMod(other: Int) = Math.floorMod(this, other)
infix fun Long.floorMod(other: Int) = Math.floorMod(this, other)
infix fun Long.floorMod(other: Long) = Math.floorMod(this, other)

// BigDecimal, BigInteger
infix fun BigDecimal.isSameAs(other: BigDecimal) = this.compareTo(other) == 0
infix fun BigDecimal.isNotSameAs(other: BigDecimal) = this.compareTo(other) != 0
infix fun BigInteger.isSameAs(other: BigInteger) = this == other
infix fun BigInteger.isNotSameAs(other: BigInteger) = this != other

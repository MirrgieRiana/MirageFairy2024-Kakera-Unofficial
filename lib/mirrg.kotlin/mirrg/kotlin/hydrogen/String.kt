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


val String.notEmptyOrNull get() = ifEmpty { null }
val String.notBlankOrNull get() = ifBlank { null }

fun String.indent(indent: String) = indent + this.replace("""\r\n|\n|\r""".toRegex()) { it.value + indent }

fun String.escapeRegex() = this.replace("""[.\[^$()*+?{|\\]""".toRegex()) { "\\" + it.groups[0]?.value!! }


// toStringを呼び出さないjoin

fun <T : CharSequence> Iterable<T>.join(separator: CharSequence = ", ") = joinToString(separator)
fun <T : CharSequence> Array<T>.join(separator: CharSequence = ", ") = joinToString(separator)
fun <T : CharSequence> Sequence<T>.join(separator: CharSequence = ", ") = joinToString(separator)

fun <T> Iterable<T>.join(separator: CharSequence = ", ", transform: (T) -> CharSequence) = joinToString(separator, transform = transform)
fun <T> Array<T>.join(separator: CharSequence = ", ", transform: (T) -> CharSequence) = joinToString(separator, transform = transform)
fun <T> Sequence<T>.join(separator: CharSequence = ", ", transform: (T) -> CharSequence) = joinToString(separator, transform = transform)


/** 先頭の文字のみを大文字にします。 */
@Suppress("DEPRECATION")
fun String.toUpperCaseHead() = if (isEmpty()) this else take(1).toUpperCase() + drop(1)

/** 先頭の文字のみを小文字にします。 */
@Suppress("DEPRECATION")
fun String.toLowerCaseHead() = if (isEmpty()) this else take(1).toLowerCase() + drop(1)

/** @receiver スネークケースの文字列 */
fun String.toUpperCamelCase(beforeDelimiter: String = "_", afterDelimiter: String = "") = split(beforeDelimiter).map { it.toUpperCaseHead() }.join(afterDelimiter)

/** @receiver スネークケースの文字列 */
fun String.toLowerCamelCase(beforeDelimiter: String = "_", afterDelimiter: String = "") = split(beforeDelimiter).mapIndexed { i, it -> if (i == 0) it else it.toUpperCaseHead() }.join(afterDelimiter)


// 中置format

infix fun Byte.formatAs(format: String) = String.format(format, this)
infix fun Short.formatAs(format: String) = String.format(format, this)
infix fun Int.formatAs(format: String) = String.format(format, this)
infix fun Long.formatAs(format: String) = String.format(format, this)
infix fun Float.formatAs(format: String) = String.format(format, this)
infix fun Double.formatAs(format: String) = String.format(format, this)

@Deprecated("removing", ReplaceWith("this formatAs \"%\${formatSpecifier}d\""))
infix fun Byte.formatBy(formatSpecifier: String) = this formatAs "%${formatSpecifier}d"

@Deprecated("removing", ReplaceWith("this formatAs \"%\${formatSpecifier}d\""))
infix fun Short.formatBy(formatSpecifier: String) = this formatAs "%${formatSpecifier}d"

@Deprecated("removing", ReplaceWith("this formatAs \"%\${formatSpecifier}d\""))
infix fun Int.formatBy(formatSpecifier: String) = this formatAs "%${formatSpecifier}d"

@Deprecated("removing", ReplaceWith("this formatAs \"%\${formatSpecifier}d\""))
infix fun Long.formatBy(formatSpecifier: String) = this formatAs "%${formatSpecifier}d"

@Deprecated("removing", ReplaceWith("this formatAs \"%\${formatSpecifier}f\""))
infix fun Float.formatBy(formatSpecifier: String) = this formatAs "%${formatSpecifier}f"

@Deprecated("removing", ReplaceWith("this formatAs \"%\${formatSpecifier}f\""))
infix fun Double.formatBy(formatSpecifier: String) = this formatAs "%${formatSpecifier}f"


// Regex

fun CharSequence.match(regex: Regex) = regex.matchEntire(this)

operator fun MatchResult.get(index: Int) = this.groups[index]?.value

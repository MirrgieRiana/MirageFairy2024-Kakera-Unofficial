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

package mirrg.kotlin.gson.hydrogen

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.math.BigDecimal
import java.math.BigInteger

class JsonTypeMismatchException(message: String) : IllegalStateException(message)

class JsonWrapper(val jsonElement: JsonElement?, val path: String = "$") {

    // 型チェック
    // 厳密に一致する場合のみtrueを返す
    val isUndefined get() = jsonElement == null
    val isArray get() = jsonElement is JsonArray
    val isObject get() = jsonElement is JsonObject
    val isPrimitive get() = jsonElement is JsonPrimitive
    val isNumber get() = jsonElement is JsonPrimitive && jsonElement.isNumber
    val isString get() = jsonElement is JsonPrimitive && jsonElement.isString
    val isBoolean get() = jsonElement is JsonPrimitive && jsonElement.isBoolean
    val isNull get() = jsonElement is JsonNull

    val type
        get() = when {
            isUndefined -> "Undefined"
            isArray -> "Array"
            isObject -> "Object"
            isNumber -> "Number"
            isString -> "String"
            isBoolean -> "Boolean"
            isNull -> "Null"
            else -> throw IllegalStateException()
        }


    /** @return undefinedもしくはJsonNullの場合、null。 */
    val orNull get() = if (isUndefined || isNull) null else this


    private fun typeMismatch(expectedType: String): Nothing = throw JsonTypeMismatchException("Expected $expectedType, but is $type: $path")

    // キャスト（厳密に同じ型でない場合は例外）
    fun asJsonUndefined() = if (isUndefined) jsonElement as Nothing? else typeMismatch("Undefined")
    fun asJsonArray() = if (isArray) jsonElement as JsonArray else typeMismatch("Array")
    fun asJsonObject() = if (isObject) jsonElement as JsonObject else typeMismatch("Object")
    fun asJsonPrimitive() = if (isPrimitive) jsonElement as JsonPrimitive else typeMismatch("Primitive")
    fun asJsonNull() = if (isNull) jsonElement as JsonNull else typeMismatch("Null")

    // プリミティブのキャスト
    fun asNumber(): Number = if (isNumber) (jsonElement as JsonPrimitive).asNumber else typeMismatch("Number")
    fun asBigInteger(): BigInteger = if (isNumber) (jsonElement as JsonPrimitive).asBigInteger else typeMismatch("Number")
    fun asBigDecimal(): BigDecimal = if (isNumber) (jsonElement as JsonPrimitive).asBigDecimal else typeMismatch("Number")
    fun asString(): String = if (isString) (jsonElement as JsonPrimitive).asString else typeMismatch("String")
    fun asBoolean() = if (isBoolean) (jsonElement as JsonPrimitive).asBoolean else typeMismatch("Boolean")

    // 加工
    fun asList() = asJsonArray().mapIndexed { i, it -> JsonWrapper(it, "$path[$i]") }
    fun asMap() = asJsonObject().entrySet().associate { (key, value) -> key!! to JsonWrapper(value, "$path.$key") }
    fun asByte() = asNumber().toByte()
    fun asShort() = asNumber().toShort()
    fun asInt() = asNumber().toInt()
    fun asLong() = asNumber().toLong()
    fun asFloat() = asNumber().toFloat()
    fun asDouble() = asNumber().toDouble()


    operator fun get(index: Int) = asJsonArray().let { JsonWrapper(if (index >= 0 && index < it.size()) it[index] else null, "$path[$index]") }
    operator fun get(key: String) = JsonWrapper(asJsonObject().get(key), "$path.$key")


    private fun getString(jsonElement: JsonElement?): String = when (jsonElement) {
        null -> "undefined"
        is JsonArray -> "[${jsonElement.joinToString(",") { getString(it) }}]"
        is JsonObject -> "{${jsonElement.entrySet().joinToString(",") { """${it.key}:${getString(it.value)}""" }}}"
        is JsonPrimitive -> when {
            jsonElement.isNumber -> jsonElement.asNumber.toString()
            jsonElement.isString -> jsonElement.asString
            jsonElement.isBoolean -> jsonElement.asBoolean.toString()
            else -> throw IllegalStateException()
        }

        is JsonNull -> "null"
        else -> throw IllegalStateException()
    }

    override fun toString() = getString(jsonElement)

}

fun JsonElement?.toJsonWrapper() = JsonWrapper(this)

val JsonWrapper.isDefined get() = !this.isUndefined
val JsonWrapper.isNonnullValue get() = !this.isUndefined && !this.isNull

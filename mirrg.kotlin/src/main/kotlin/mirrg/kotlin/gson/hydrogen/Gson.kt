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

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive


// Conversion
fun String.toJsonElement(configurator: GsonBuilder.() -> Unit = {}) = if (this.isBlank()) null else GsonBuilder().also { configurator(it) }.create().fromJson(this, JsonElement::class.java)
fun JsonElement?.toJson(configurator: GsonBuilder.() -> Unit = {}): String = if (this == null) "" else GsonBuilder().also { configurator(it) }.create().toJson(this)


// to JsonElement

// List

private operator fun JsonArray.plusAssign(item: JsonElement) = this.add(item)

val Iterable<JsonElement>.jsonArray: JsonArray
    get() {
        val jsonArray = JsonArray()
        this.forEach { item ->
            jsonArray += item
        }
        return jsonArray
    }

val Iterable<JsonElement?>.jsonArrayNotNull: JsonArray
    get() {
        val jsonArray = JsonArray()
        this.forEach { item ->
            if (item != null) {
                jsonArray += item
            }
        }
        return jsonArray
    }

fun jsonArray(vararg items: JsonElement) = items.asIterable().jsonArray
fun jsonArrayNotNull(vararg items: JsonElement?) = items.asIterable().jsonArrayNotNull

// Map

private operator fun JsonObject.set(key: String, value: JsonElement) = this.add(key, value)

val Iterable<Pair<String, JsonElement>>.jsonObject: JsonObject
    get() {
        val jsonObject = JsonObject()
        this.forEach { entry ->
            jsonObject[entry.first] = entry.second
        }
        return jsonObject
    }

val Iterable<Pair<String, JsonElement?>?>.jsonObjectNotNull: JsonObject
    get() {
        val jsonObject = JsonObject()
        this.forEach { entry ->
            if (entry != null) {
                val value = entry.second
                if (value != null) {
                    jsonObject[entry.first] = value
                }
            }
        }
        return jsonObject
    }

fun jsonObject(vararg entries: Pair<String, JsonElement>) = entries.asIterable().jsonObject
fun jsonObjectNotNull(vararg entries: Pair<String, JsonElement?>?) = entries.asIterable().jsonObjectNotNull

val Map<String, JsonElement>.jsonObject: JsonObject
    get() {
        val jsonObject = JsonObject()
        this.forEach { entry ->
            jsonObject[entry.key] = entry.value
        }
        return jsonObject
    }

val Map<String, JsonElement?>.jsonObjectNotNull: JsonObject
    get() {
        val jsonObject = JsonObject()
        this.forEach { entry ->
            val value = entry.value
            if (value != null) {
                jsonObject[entry.key] = value
            }
        }
        return jsonObject
    }

// Primitives

val Number.jsonElement get() = JsonPrimitive(this)
val String.jsonElement get() = JsonPrimitive(this)
val Boolean.jsonElement get() = JsonPrimitive(this)
val Number?.jsonElementOrJsonNull get() = this?.jsonElement ?: jsonNull
val String?.jsonElementOrJsonNull get() = this?.jsonElement ?: jsonNull
val Boolean?.jsonElementOrJsonNull get() = this?.jsonElement ?: jsonNull
val jsonNull: JsonNull get() = JsonNull.INSTANCE

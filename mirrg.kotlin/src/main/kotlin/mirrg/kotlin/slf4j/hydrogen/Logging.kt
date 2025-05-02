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

package mirrg.kotlin.slf4j.hydrogen

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <T : Any> T.getLogger() = getLogger(javaClass)
fun getLogger(name: String): Logger = LoggerFactory.getLogger(name)
fun getLogger(clazz: Class<*>): Logger = LoggerFactory.getLogger(clazz)
fun getFileLogger(topLevelAnonymousObject: Any) = getLogger(topLevelAnonymousObject::class.java.enclosingClass!!)

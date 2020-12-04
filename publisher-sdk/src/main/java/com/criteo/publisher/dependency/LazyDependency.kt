/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.dependency

/**
 * Lazily provide and cache the dependency that can be built from the given supplier.
 *
 * If a dependency is particularly expensive to produce, then work can be deferred with a lazy dependency. This is
 * especially useful when the dependency if not always needed.
 *
 * In case of circular dependency, a [StackOverflowError] is thrown unless the cycle is broken via a lazy dependency.
 *
 * For instance in:
 * ```
 * class Foo(val bar: Bar)
 *
 * class Bar(val foo: Foo)
 * ```
 *
 * Both `Foo` and `Bar` need each other and produce a circular dependency. This can be fixed by deferring the creation
 * of `Foo` (or `Bar`) like:
 * ```
 * class Foo(val bar: Bar)
 *
 * class Bar(val lazyFoo: LazyDependency<Foo>) {
 *   fun foobar() = lazyFoo.get().foobar() // Foo is only instantiated here
 * }
 * ```
 */
class LazyDependency<T>(private val name: String? = null, supplier: () -> T) {

  private val value: T by lazy(supplier)

  fun get(): T = value

  override fun toString(): String {
    return name?.let { "LazyDependency($it)" } ?: super.toString()
  }
}

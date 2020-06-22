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

package com.criteo.publisher.privacy.gdpr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.util.SafeSharedPreferences;

public class TcfStrategyResolver {
  private final SafeSharedPreferences safeSharedPreferences;

  public TcfStrategyResolver(@NonNull SafeSharedPreferences safeSharedPreferences) {
    this.safeSharedPreferences = safeSharedPreferences;
  }

  @Nullable
  TcfGdprStrategy resolveTcfStrategy() {
    Tcf2GdprStrategy tcf2GdprStrategy = new Tcf2GdprStrategy(safeSharedPreferences);

    if (tcf2GdprStrategy.isProvided()) {
      return tcf2GdprStrategy;
    }

    Tcf1GdprStrategy tcf1GdprStrategy = new Tcf1GdprStrategy(safeSharedPreferences);

    if (tcf1GdprStrategy.isProvided()) {
      return tcf1GdprStrategy;

    }

    return null;
  }
}

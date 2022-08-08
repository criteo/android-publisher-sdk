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

public class GdprDataFetcher {

  @NonNull
  private final TcfStrategyResolver tcfStrategyResolver;

  public GdprDataFetcher(@NonNull TcfStrategyResolver tcfStrategyResolver) {
    this.tcfStrategyResolver = tcfStrategyResolver;
  }

  @Nullable
  public GdprData fetch() {
    TcfGdprStrategy tcfStrategy = tcfStrategyResolver.resolveTcfStrategy();

    if (tcfStrategy == null) {
      return null;
    }

    String subjectToGdpr = tcfStrategy.getSubjectToGdpr();
    String consentString = tcfStrategy.getConsentString();

    return new GdprData(
        consentString,
        subjectToGdpr.isEmpty() ? null : "1".equals(subjectToGdpr),
        tcfStrategy.getVersion()
    );
  }
}

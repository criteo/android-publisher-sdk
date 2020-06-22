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

package com.criteo.publisher.csm;

import android.content.Context;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.criteo.publisher.DependencyProvider.Factory;
import com.criteo.publisher.util.BuildConfigWrapper;

@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR1)
public class MetricRepositoryFactory implements Factory<MetricRepository> {

  @NonNull
  private final Context context;

  @NonNull
  private final MetricParser metricParser;

  @NonNull
  private final BuildConfigWrapper buildConfigWrapper;

  public MetricRepositoryFactory(
      @NonNull Context context,
      @NonNull MetricParser metricParser,
      @NonNull BuildConfigWrapper buildConfigWrapper
  ) {
    this.context = context;
    this.metricParser = metricParser;
    this.buildConfigWrapper = buildConfigWrapper;
  }

  @NonNull
  @Override
  public MetricRepository create() {
    MetricDirectory directory = new MetricDirectory(context, buildConfigWrapper, metricParser);
    MetricRepository fileMetricRepository = new FileMetricRepository(directory);
    return new BoundedMetricRepository(fileMetricRepository, buildConfigWrapper);
  }
}

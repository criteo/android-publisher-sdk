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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.JsonSerializer;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MetricRepositoryFactoryTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  @Inject
  private JsonSerializer jsonSerializer;

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  private MetricRepositoryFactory factory;

  @Before
  public void setUp() throws Exception {
    when(buildConfigWrapper.getCsmDirectoryName()).thenReturn("directory");

    factory = new MetricRepositoryFactory(
        context,
        jsonSerializer,
        buildConfigWrapper
    );
  }

  @After
  public void tearDown() throws Exception {
    MetricDirectoryHelper.clear(new MetricDirectory(context, buildConfigWrapper, jsonSerializer));
  }

  @Test
  public void create_GivenNewDirectory_RepositoryIsWorking() throws Exception {
    MetricRepository repository = factory.create();

    repository.addOrUpdateById("id", builder -> {});
    assertTrue(repository.contains("id"));
    repository.moveById("id", metric -> false);
    assertTrue(repository.contains("id"));
    repository.moveById("id", metric -> true);
    assertFalse(repository.contains("id"));
  }

  @Test
  public void addOrUpdateById_GivenTonsOfMetrics_SizeStaysAroundMemoryLimit() throws Exception {
    int smallSizeEstimationPerMetrics = 150;
    int maxSize = buildConfigWrapper.getMaxSizeOfCsmMetricsFolder();
    int requiredMetricsForOverflow = maxSize / smallSizeEstimationPerMetrics;
    int requiredMetricsForOverflowWithMargin = (int) (requiredMetricsForOverflow * 1.20);

    MetricRepository repository = factory.create();

    for (int i = 0; i < requiredMetricsForOverflowWithMargin; i++) {
      repository.addOrUpdateById("id" + i, builder -> builder
          .setCdbCallStartTimestamp(0L)
          .setCdbCallEndTimestamp(1L)
          .setElapsedTimestamp(2L));
    }

    // The last element can overflow the limit, so we are lenient (up to 1%) on the below condition.
    assertTrue(repository.getTotalSize() * 0.99 <= maxSize);
  }
}

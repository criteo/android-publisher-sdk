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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.squareup.tape.ObjectQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MetricObjectQueueFactoryTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private File queueFile;

  @Inject
  private Context context;

  @Inject
  private MetricParser metricParser;

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  private MetricObjectQueueFactory factory;

  @Before
  public void setUp() throws Exception {
    when(buildConfigWrapper.getCsmQueueFilename()).thenReturn("queueFile");

    queueFile = new File(context.getFilesDir(), "queueFile");

    factory = spy(new MetricObjectQueueFactory(
        context,
        metricParser,
        buildConfigWrapper
    ));

    doReturn(queueFile).when(factory).getQueueFile();
  }

  @After
  public void tearDown() throws Exception {
    queueFile.delete();
  }

  @Test
  public void sanityCheck_GivenNotExistingFileAndDoNotCreateQueue_QueueFileDoesNotExist() throws Exception {
    queueFile.createNewFile();
    queueFile.delete();

    // no factory.create()

    assertFalse(queueFile.exists());
  }

  @Test
  public void create_GivenNotExistingQueueFile_CreateFileAndQueueIsWorking() throws Exception {
    queueFile.delete();

    ObjectQueue<Metric> queue = factory.create();

    assertTrue(queueFile.exists());
    assertNotNull(queue);
  }


  @Test
  public void create_GivenStaleQueueFile_RecreateFileAndQueueIsWorking() throws Exception {
    givenDeactivatedPreconditionUtils();

    byte[] garbage = new byte[]{42, 13, 37};
    try (FileOutputStream fos = new FileOutputStream(queueFile)) {
      fos.write(garbage);
    }

    ObjectQueue<Metric> metricObjectQueue = factory.create();

    byte[] fileContent = Files.readAllBytes(queueFile.toPath());
    assertTrue(queueFile.exists());
    assertFalse(Arrays.equals(fileContent, garbage));
    assertNotNull(metricObjectQueue);
  }

  @Test
  public void create_GivenQueueFileIsADirectory_RecreateFileAndQueueIsWorking() throws Exception {
    givenDeactivatedPreconditionUtils();

    queueFile.mkdirs();

    ObjectQueue<Metric> metricObjectQueue = factory.create();

    assertTrue(queueFile.exists());
    assertFalse(queueFile.isDirectory());
    assertNotNull(metricObjectQueue);
  }

  @Test
  public void create_GivenQueueFileIsADirectoryWithContent_RecreateFileAndQueueIsWorking() throws Exception {
    givenDeactivatedPreconditionUtils();

    queueFile.mkdirs();
    new File(queueFile, "dummyContent").createNewFile();

    ObjectQueue<Metric> metricObjectQueue = factory.create();

    assertTrue(queueFile.exists());
    assertFalse(queueFile.isDirectory());
    assertNotNull(metricObjectQueue);
  }

  @Test
  public void offer_GivenATonsOfMetrics_AcceptAllOfThemButEvictOlderOnesToStayAroundMemoryLimit() throws Exception {
    int smallSizeEstimationPerMetrics = 150;
    int maxSize = buildConfigWrapper.getMaxSizeOfCsmMetricSendingQueue();
    int requiredMetricsForOverflow = maxSize / smallSizeEstimationPerMetrics;
    int requiredMetricsForOverflowWithMargin = (int) (requiredMetricsForOverflow * 1.20);

    MetricSendingQueue tapeQueue = new TapeMetricSendingQueue(factory);
    BoundedMetricSendingQueue boundedMetricSendingQueue = new BoundedMetricSendingQueue(tapeQueue,
        buildConfigWrapper);

    for (int i = 0; i < requiredMetricsForOverflowWithMargin; i++) {
      Metric metric = Metric.builder("id" + i)
          .setCdbCallStartTimestamp(0L)
          .setCdbCallEndTimestamp(1L)
          .setElapsedTimestamp(2L)
          .build();

      boundedMetricSendingQueue.offer(metric);
    }

    // The last element can overflow the limit, so we are lenient (up to 1%) on the below condition.
    assertTrue(boundedMetricSendingQueue.getTotalSize() * 0.99 <= maxSize);

    // The queue file grows in power of 2. So it can be, at most, twice larger than expected.
    // To not waste this memory, the max size should be near a power of 2. We are lenient (up to
    // 10%) on this condition.
    assertTrue(queueFile.length() <= maxSize * 1.10);
  }

  private void givenDeactivatedPreconditionUtils() {
    when(buildConfigWrapper.preconditionThrowsOnException()).thenReturn(false);
  }
}

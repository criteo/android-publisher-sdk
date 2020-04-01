package com.criteo.publisher.csm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.Util.BuildConfigWrapper;
import com.criteo.publisher.mock.MockBean;
import com.criteo.publisher.mock.MockedDependenciesRule;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MetricSendingQueueFactoryTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private Context context;

  private File queueFile;

  private BuildConfigWrapper buildConfigWrapper;

  private MetricSendingQueueFactory factory;

  @Before
  public void setUp() throws Exception {
    context = InstrumentationRegistry.getContext().getApplicationContext();

    buildConfigWrapper = spy(mockedDependenciesRule.getDependencyProvider().provideBuildConfigWrapper());
    doReturn(buildConfigWrapper).when(mockedDependenciesRule.getDependencyProvider()).provideBuildConfigWrapper();
    when(buildConfigWrapper.getCsmQueueFilename()).thenReturn("queueFile");

    queueFile = new File(context.getFilesDir(), "queueFile");

    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();

    factory = new MetricSendingQueueFactory(
        context,
        dependencyProvider.provideMetricParser(),
        dependencyProvider.provideBuildConfigWrapper()
    );
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

    MetricSendingQueue queue = factory.create();

    assertTrue(queueFile.exists());
    assertQueueIsWorking(queue);
  }

  @Test
  public void create_GivenAlreadyExistingQueueFile_ReuseFileAndQueueIsWorking() throws Exception {
    Metric metric1 = Metric.builder("id1").build();
    Metric metric2 = Metric.builder("id2").build();

    queueFile.delete();

    MetricSendingQueue previousQueue = factory.create();
    previousQueue.offer(metric1);

    MetricSendingQueue queue = factory.create();
    queue.offer(metric2);
    List<Metric> metrics = queue.poll(2);

    assertTrue(queueFile.exists());
    assertEquals(Arrays.asList(metric1, metric2), metrics);
  }

  @Test
  public void create_GivenStaleQueueFile_RecreateFileAndQueueIsWorking() throws Exception {
    givenDeactivatedPreconditionUtils();

    byte[] garbage = new byte[]{42, 13, 37};
    try (FileOutputStream fos = new FileOutputStream(queueFile)) {
      fos.write(garbage);
    }

    MetricSendingQueue queue = factory.create();
    byte[] fileContent = Files.readAllBytes(queueFile.toPath());

    assertTrue(queueFile.exists());
    assertFalse(Arrays.equals(fileContent, garbage));
    assertQueueIsWorking(queue);
  }

  @Test
  public void create_GivenQueueFileIsADirectory_RecreateFileAndQueueIsWorking() throws Exception {
    givenDeactivatedPreconditionUtils();

    queueFile.mkdirs();

    MetricSendingQueue queue = factory.create();

    assertTrue(queueFile.exists());
    assertFalse(queueFile.isDirectory());
    assertQueueIsWorking(queue);
  }

  @Test
  public void create_GivenQueueFileIsADirectoryWithContent_RecreateFileAndQueueIsWorking() throws Exception {
    givenDeactivatedPreconditionUtils();

    queueFile.mkdirs();
    new File(queueFile, "dummyContent").createNewFile();

    MetricSendingQueue queue = factory.create();

    assertTrue(queueFile.exists());
    assertFalse(queueFile.isDirectory());
    assertQueueIsWorking(queue);
  }

  @Test
  public void offer_GivenATonsOfMetrics_AcceptAllOfThemButEvictOlderOnesToStayAroundMemoryLimit() throws Exception {
    int smallSizeEstimationPerMetrics = 150;
    int maxSize = buildConfigWrapper.getMaxSizeOfCsmMetricSendingQueue();
    int requiredMetricsForOverflow = maxSize / smallSizeEstimationPerMetrics;
    int requiredMetricsForOverflowWithMargin = (int) (requiredMetricsForOverflow * 1.20);

    MetricSendingQueue queue = factory.create();

    for (int i = 0; i < requiredMetricsForOverflowWithMargin; i++) {
      Metric metric = Metric.builder("id" + i)
          .setCdbCallStartTimestamp(0L)
          .setCdbCallEndTimestamp(1L)
          .setElapsedTimestamp(2L)
          .build();

      assertTrue(queue.offer(metric));
    }

    // The last element can overflow the limit, so we are lenient (up to 1%) on the below condition.
    assertTrue(queue.getTotalSize() * 0.99 <= maxSize);

    // The queue file grows in power of 2. So it can be, at most, twice larger than expected.
    // To not waste this memory, the max size should be near a power of 2. We are lenient (up to
    // 10%) on this condition.
    assertTrue(queueFile.length() <= maxSize * 1.10);
  }

  private void assertQueueIsWorking(MetricSendingQueue queue) {
    Metric savedMetric = Metric.builder("id").build();
    queue.offer(savedMetric);
    List<Metric> metrics = queue.poll(2);

    assertEquals(1, metrics.size());
    assertTrue(metrics.contains(savedMetric));
  }

  private void givenDeactivatedPreconditionUtils() {
    when(buildConfigWrapper.isDebug()).thenReturn(false);
  }

}
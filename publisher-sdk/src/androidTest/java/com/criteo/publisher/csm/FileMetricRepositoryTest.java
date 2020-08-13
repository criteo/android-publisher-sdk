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

import static com.criteo.publisher.csm.MetricDirectoryHelper.clear;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.criteo.publisher.csm.MetricRepository.MetricUpdater;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.util.BuildConfigWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class FileMetricRepositoryTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  private MetricDirectory directory;

  @Inject
  private BuildConfigWrapper buildConfigWrapper;

  @Inject
  private MetricParser parser;

  private FileMetricRepository repository;

  @Before
  public void setUp() throws Exception {
    givenNewRepository();
  }

  @After
  public void tearDown() throws Exception {
    clear(directory);
  }

  @Test
  public void getTotalSize_GivenEmptyRepository_ReturnZero() throws Exception {
    int size = repository.getTotalSize();

    assertEquals(0, size);
  }

  @Test
  public void getTotalSize_GivenSomeOperations_ReturnSizeGreaterThanEstimation() throws Exception {
    repository.addOrUpdateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(42L)
          .setCdbCallEndTimestamp(1337L)
          .setElapsedTimestamp(1024L)
          .setReadyToSend(true);
    });

    repository.addOrUpdateById("id2", builder -> {});

    givenNewRepository();

    repository.addOrUpdateById("id3", builder -> {
      builder.setCdbCallStartTimestamp(42L)
          .setCdbCallEndTimestamp(1337L)
          .setElapsedTimestamp(1024L)
          .setReadyToSend(true);

    });

    repository.moveById("id1", metric -> true);
    repository.moveById("id2", metric -> true);
    repository.moveById("id3", metric -> true);

    for (int i = 0; i < 100; i++) {
      repository.addOrUpdateById("id" + i, builder -> {
        builder.setCdbCallStartTimestamp(42L)
            .setCdbCallEndTimestamp(1337L)
            .setElapsedTimestamp(1024L)
            .setReadyToSend(true);
      });
    }

    givenNewRepository();

    int estimatedSizePerMetric = 164;
    int size = repository.getTotalSize();

    // Metric DTO will tend to grow
    assertTrue(size >= 100 * estimatedSizePerMetric);
  }

  @Test
  public void contains_GivenEmptyRepository_ReturnFalse() throws Exception {
    boolean contained = repository.contains("id");

    assertFalse(contained);
  }

  @Test
  public void contains_AfterAnUpdate_ReturnTrue() throws Exception {
    repository.addOrUpdateById("id1", builder -> {});
    givenNewRepository();
    repository.addOrUpdateById("id2", builder -> {});

    assertTrue(repository.contains("id1"));
    assertTrue(repository.contains("id2"));
  }

  @Test
  public void contains_AfterAMove_ReturnFalse() throws Exception {
    repository.addOrUpdateById("id1", builder -> {});
    repository.addOrUpdateById("id2", builder -> {});

    repository.moveById("id1", builder -> true);
    givenNewRepository();
    repository.moveById("id2", builder -> true);

    assertFalse(repository.contains("id1"));
    assertFalse(repository.contains("id2"));
  }

  @Test
  public void getAllStoredMetrics_GivenNoOperations_ReturnEmpty() throws Exception {
    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertTrue(metrics.isEmpty());
  }

  @Test
  public void getAllStoredMetrics_GivenUpdatesAndThenNewRepository_ReturnPreviousResult() throws Exception {
    repository.addOrUpdateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(42L);
      builder.setCdbCallTimeout(true);
    });

    repository.addOrUpdateById("id2", builder -> {
      builder.setCdbCallEndTimestamp(1337L);
    });

    Collection<Metric> metrics1 = repository.getAllStoredMetrics();
    givenNewRepository(); // Simulate a restart of the application
    Collection<Metric> metrics2 = repository.getAllStoredMetrics();

    assertEquals(metrics1, metrics2);
  }

  @Test
  public void updateById_GivenNoOpUpdateOperation_RepositoryContainOneNewMetric() throws Exception {
    Metric expected = Metric.builder("id")
        .build();

    repository.addOrUpdateById("id", builder -> {
      /* no op */
    });

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertEquals(expected, metrics.iterator().next());
  }

  @Test
  public void updateById_GivenOneUpdateOperation_RepositoryContainOneUpdatedMetric() throws Exception {
    Metric expected = Metric.builder("id")
        .setCdbCallStartTimestamp(42L)
        .build();

    repository.addOrUpdateById("id", builder -> {
      builder.setCdbCallStartTimestamp(42L);
    });

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertEquals(expected, metrics.iterator().next());
  }

  @Test
  public void updateById_GivenManyUpdateOperations_RepositoryContainMetricWithAllUpdates() throws Exception {
    Metric expected = Metric.builder("impId")
        .setCdbCallStartTimestamp(42L)
        .setCdbCallEndTimestamp(1337L)
        .setCdbCallTimeout(true)
        .build();

    repository.addOrUpdateById("impId", builder -> {
      builder.setCdbCallStartTimestamp(42L);
    });

    repository.addOrUpdateById("impId", builder -> {
      builder.setCdbCallEndTimestamp(1337L);
    });

    repository.addOrUpdateById("impId", builder -> {
      builder.setCdbCallTimeout(true);
    });

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertEquals(expected, metrics.iterator().next());
  }

  @Test
  public void updateById_GivenManyUpdateOperationsWithNewRepository_RepositoryContainMetricWithAllUpdates() throws Exception {
    Metric expected = Metric.builder("impId")
        .setCdbCallStartTimestamp(42L)
        .setCdbCallEndTimestamp(1337L)
        .setCdbCallTimeout(true)
        .build();

    repository.addOrUpdateById("impId", builder -> {
      builder.setCdbCallStartTimestamp(42L);
    });

    givenNewRepository();

    repository.addOrUpdateById("impId", builder -> {
      builder.setCdbCallEndTimestamp(1337L);
    });

    givenNewRepository();

    repository.addOrUpdateById("impId", builder -> {
      builder.setCdbCallTimeout(true);
    });

    givenNewRepository();

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertEquals(expected, metrics.iterator().next());
  }

  @Test
  public void updateById_GivenManyIdsUpdated_RepositoryContainsMultipleMetrics() throws Exception {
    Metric expected1 = Metric.builder("id1")
        .setCdbCallStartTimestamp(42L)
        .setCdbCallTimeout(true)
        .build();

    Metric expected2 = Metric.builder("id2")
        .setCdbCallEndTimestamp(1337L)
        .build();

    repository.addOrUpdateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(42L);
      builder.setCdbCallTimeout(true);
    });

    repository.addOrUpdateById("id2", builder -> {
      builder.setCdbCallEndTimestamp(1337L);
    });

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(2, metrics.size());
    assertTrue(metrics.contains(expected1));
    assertTrue(metrics.contains(expected2));
  }

  @Test
  public void updateById_GivenReadDuringAnUpdate_ReadOccurAfterUpdateIndependentlyOfOtherIds() throws Exception {
    Metric expected = Metric.builder("id1")
            .setCdbCallStartTimestamp(1337L)
            .build();

    ExecutorService executor = Executors.newFixedThreadPool(3);
    CountDownLatch isInUpdate = new CountDownLatch(2);
    CountDownLatch isReadFinished = new CountDownLatch(1);

    repository.addOrUpdateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(0L);
    });

    Future<?> updateTask = executor.submit(() -> {
      repository.addOrUpdateById("id1", builder -> {
        builder.setCdbCallStartTimestamp(42L);
        isInUpdate.countDown();
        builder.setCdbCallStartTimestamp(1337L);
      });
    });

    Future<?> independentUpdateTask = executor.submit(() -> {
      repository.addOrUpdateById("id2", builder -> {
        builder.setCdbCallStartTimestamp(1L);
        isInUpdate.countDown();

        // Block this update to show that it doesn't block the read
        awaitShortly(isReadFinished);

        builder.setCdbCallStartTimestamp(2L);
      });
    });

    Future<Collection<Metric>> readTask = executor.submit(() -> {
      awaitShortly(isInUpdate);
      return repository.getAllStoredMetrics();
    });

    Collection<Metric> metrics = readTask.get();
    isReadFinished.countDown();
    updateTask.get();
    independentUpdateTask.get();

    // id1 already exists before the read started, so it is included in read, but only its committed
    // version is read. id2 was committed after the read start, hence it is not included.
    assertEquals(1, metrics.size());
    assertTrue(metrics.contains(expected));
  }

  @Test
  public void updateById_GivenSeveralUpdateAndReadingInParallel_DoNotThrowAndIsNotInInconsistentState() throws Exception {
    int parties = 30;
    CyclicBarrier barrier = new CyclicBarrier(parties);
    ExecutorService executor = Executors.newFixedThreadPool(parties);
    ArrayList<Future<?>> futures = new ArrayList<>();

    for (int i = 0; i < parties * 10; i++) {
      long finalI = i + 1;
      futures.add(executor.submit(() -> {
        barrier.await();
        repository.addOrUpdateById("id", builder -> {
          builder.setCdbCallStartTimestamp(finalI);
        });
        return null;
      }));

      futures.add(executor.submit(() -> {
        barrier.await();
        return repository.getAllStoredMetrics();
      }));
    }

    for (Future<?> future : futures) {
      future.get();
    }

    repository.addOrUpdateById("id", builder -> {
      builder.setCdbCallStartTimestamp(0L);
    });

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertTrue(metrics.contains(Metric.builder("id")
        .setCdbCallStartTimestamp(0L)
        .build()));
  }

  @Test
  public void updateById_GivenExceptionDuringUpdateOfNewMetric_MetricIsNotCreated() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(1);
    RuntimeException expectedException = new RuntimeException();

    try {
      executor.submit(() -> {
        repository.addOrUpdateById("id", builder -> {
          throw expectedException;
        });
      }).get();
      fail("Expected exception");
    } catch (ExecutionException e) {
      assertEquals(expectedException, e.getCause());
    }

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertTrue(metrics.isEmpty());
  }

  @Test
  public void updateById_GivenExceptionDuringUpdateOfOldMetric_DoesNotCommitUpdate() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(1);
    RuntimeException expectedException = new RuntimeException();

    Metric expectedMetric = Metric.builder("id")
        .setCdbCallStartTimestamp(42L)
        .build();

    repository.addOrUpdateById("id", builder -> {
      builder.setCdbCallStartTimestamp(42L);
    });

    try {
      executor.submit(() -> {
        repository.addOrUpdateById("id", builder -> {
          builder.setCdbCallStartTimestamp(1337L);
          throw expectedException;
        });
      }).get();
      fail("Expected exception");
    } catch (ExecutionException e) {
      assertEquals(expectedException, e.getCause());
    }

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertTrue(metrics.contains(expectedMetric));
  }

  @Test
  public void getAllStoredMetrics_GivenAnIoExceptionDuringOneRead_IgnoreIt() throws Exception {
    parser = spy(parser);

    repository.addOrUpdateById("id1", builder -> { });
    repository.addOrUpdateById("id2", builder -> { });

    givenNewRepository();

    doThrow(IOException.class)
        .doCallRealMethod()
        .when(parser)
        .read(any());

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
  }

  @Test
  public void updateById_GivenIoExceptionDuringReadOfExistingMetric_DoNotUpdateMetric() throws Exception {
    parser = spy(parser);

    repository.addOrUpdateById("id1", builder -> { });

    givenNewRepository();

    doThrow(IOException.class).when(parser).read(any());

    MetricUpdater updater = mock(MetricUpdater.class);
    repository.addOrUpdateById("id1", updater);

    verify(updater, never()).update(any());
  }

  @Test
  public void updateById_GivenIoExceptionDuringWriteOfNewMetric_DoNotCreateMetric() throws Exception {
    parser = spy(parser);
    givenNewRepository();

    doThrow(IOException.class).when(parser).write(any(), any());

    repository.addOrUpdateById("id1", builder -> { });

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertTrue(metrics.isEmpty());
  }

  @Test
  public void updateById_GivenIoExceptionDuringFirstWriteOfNewMetricThenWriteNormally_CreateNewMetric() throws Exception {
    Metric expectedMetric = Metric.builder("id1")
        .setCdbCallStartTimestamp(42L)
        .build();

    parser = spy(parser);
    givenNewRepository();

    doThrow(IOException.class)
        .doCallRealMethod()
        .when(parser)
        .write(any(), any());

    repository.addOrUpdateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(1337L);
    });

    repository.addOrUpdateById("id1", builder -> {
      assertEquals(Metric.builder("id1").build(), builder.build());
      builder.setCdbCallStartTimestamp(42L);
    });

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertTrue(metrics.contains(expectedMetric));
  }

  @Test
  public void updateById_GivenIoExceptionDuringWriteOfOldMetric_DoNotUpdateMetric() throws Exception {
    Metric expectedMetric = Metric.builder("id1").build();

    parser = spy(parser);
    givenNewRepository();

    repository.addOrUpdateById("id1", builder -> { });

    doThrow(IOException.class).when(parser).write(any(), any());

    repository.addOrUpdateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(42L);
    });

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertTrue(metrics.contains(expectedMetric));
  }

  @Test
  public void updateById_GivenIoExceptionDuringFirstWriteOfOldMetricThenWriteNormally_UpdateMetric() throws Exception {
    Metric expectedMetric = Metric.builder("id1")
        .setCdbCallStartTimestamp(42L)
        .build();

    parser = spy(parser);
    givenNewRepository();

    repository.addOrUpdateById("id1", builder -> { });

    doThrow(IOException.class)
        .doCallRealMethod()
        .when(parser)
        .write(any(), any());

    repository.addOrUpdateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(1337L);
    });

    repository.addOrUpdateById("id1", builder -> {
      assertEquals(Metric.builder("id1").build(), builder.build());
      builder.setCdbCallStartTimestamp(42L);
    });

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertTrue(metrics.contains(expectedMetric));
  }

  @Test
  public void updateById_AfterAMovedMetric_GetANewMetricToUpdate() throws Exception {
    MetricMover mover = mock(MetricMover.class);
    when(mover.offerToDestination(any())).thenReturn(true);

    repository.addOrUpdateById("id", builder -> builder.setCdbCallStartTimestamp(1L));
    repository.moveById("id", mover);

    repository.addOrUpdateById("id", builder -> {
      assertEquals(builder.build(), Metric.builder("id").build());
    });
  }

  @Test
  public void moveById_GivenRepositoryWithMetricAndSuccessfulMove_RemoveMetric() throws Exception {
    MetricMover mover = mock(MetricMover.class);
    when(mover.offerToDestination(any())).thenReturn(true);

    repository.addOrUpdateById("id", builder -> {});
    givenNewRepository();

    repository.moveById("id", mover);

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    verify(mover, times(1)).offerToDestination(any());
    assertTrue(metrics.isEmpty());
  }

  @Test
  public void moveById_GivenRepositoryWithMetricAndUnsuccessfulMove_RollbackMetric() throws Exception {
    MetricMover mover = mock(MetricMover.class);
    when(mover.offerToDestination(any())).thenReturn(false);

    repository.addOrUpdateById("id", builder -> builder.setCdbCallStartTimestamp(2L));
    givenNewRepository();

    repository.moveById("id", mover);

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertTrue(metrics.contains(Metric.builder("id")
        .setCdbCallStartTimestamp(2L)
        .build()));
  }

  @Test
  public void moveById_GivenErrorWhenReadingMetric_IgnoreErrorAndDoNotMove() throws Exception {
    MetricMover mover = mock(MetricMover.class);
    when(mover.offerToDestination(any())).thenReturn(true);

    parser = spy(parser);

    repository.addOrUpdateById("id", builder -> {});
    givenNewRepository();

    doThrow(IOException.class)
        .doCallRealMethod()
        .when(parser).read(any());

    repository.moveById("id", mover);

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertTrue(metrics.contains(Metric.builder("id").build()));
  }

  private void awaitShortly(CountDownLatch latch) {
    try {
      // Timeout after 1 second to not block the test that is expected to only sleep for few IO operations.
      latch.await();
      assertTrue(latch.await(5, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void givenNewRepository() {
    directory = new MetricDirectory(context, buildConfigWrapper, parser);
    repository = new FileMetricRepository(directory);
  }

}
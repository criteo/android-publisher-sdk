package com.criteo.publisher.csm;

import static com.criteo.publisher.csm.MetricDirectoryHelper.clear;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.csm.MetricRepository.MetricUpdater;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MetricRepositoryTest {

  private MetricDirectory directory;

  private MetricParser parser;

  private MetricRepository repository;

  @Before
  public void setUp() throws Exception {
    parser = new MetricParser();

    givenNewRepository();
  }

  @After
  public void tearDown() throws Exception {
    clear(directory);
  }

  @Test
  public void getAllStoredMetrics_GivenNoOperations_ReturnEmpty() throws Exception {
    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertTrue(metrics.isEmpty());
  }

  @Test
  public void getAllStoredMetrics_GivenUpdatesAndThenNewRepository_ReturnPreviousResult() throws Exception {
    repository.updateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(42L);
      builder.setCdbCallTimeout(true);
    });

    repository.updateById("id2", builder -> {
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

    repository.updateById("id", builder -> {
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

    repository.updateById("id", builder -> {
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

    repository.updateById("impId", builder -> {
      builder.setCdbCallStartTimestamp(42L);
    });

    repository.updateById("impId", builder -> {
      builder.setCdbCallEndTimestamp(1337L);
    });

    repository.updateById("impId", builder -> {
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

    repository.updateById("impId", builder -> {
      builder.setCdbCallStartTimestamp(42L);
    });

    givenNewRepository();

    repository.updateById("impId", builder -> {
      builder.setCdbCallEndTimestamp(1337L);
    });

    givenNewRepository();

    repository.updateById("impId", builder -> {
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

    repository.updateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(42L);
      builder.setCdbCallTimeout(true);
    });

    repository.updateById("id2", builder -> {
      builder.setCdbCallEndTimestamp(1337L);
    });

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(2, metrics.size());
    assertTrue(metrics.contains(expected1));
    assertTrue(metrics.contains(expected2));
  }

  @Test
  public void updateById_GivenReadDuringAnUpdate_ReadOccurBeforeOrAfterTheUpdate() throws Exception {
    Metric expected = Metric.builder("id1")
        .setCdbCallStartTimestamp(1337L)
        .build();

    ExecutorService executor = Executors.newFixedThreadPool(3);
    CountDownLatch isInUpdate = new CountDownLatch(2);

    repository.updateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(0L);
    });

    Future<?> future1 = executor.submit(() -> {
      repository.updateById("id1", builder -> {
        builder.setCdbCallStartTimestamp(42L);
        isInUpdate.countDown();
        waitForPotentialIO();
        builder.setCdbCallStartTimestamp(1337L);
      });
    });

    Future<?> future2 = executor.submit(() -> {
      repository.updateById("id2", builder -> {
        builder.setCdbCallStartTimestamp(1L);
        isInUpdate.countDown();
        waitForPotentialIO();
        builder.setCdbCallStartTimestamp(2L);
      });
    });

    Future<Collection<Metric>> future3 = executor.submit(() -> {
      assertTrue(isInUpdate.await(1, TimeUnit.SECONDS));
      return repository.getAllStoredMetrics();
    });

    future1.get();
    future2.get();
    Collection<Metric> metrics = future3.get();

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
        repository.updateById("id", builder -> {
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

    repository.updateById("id", builder -> {
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
        repository.updateById("id", builder -> {
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

    repository.updateById("id", builder -> {
      builder.setCdbCallStartTimestamp(42L);
    });

    try {
      executor.submit(() -> {
        repository.updateById("id", builder -> {
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

    repository.updateById("id1", builder -> { });
    repository.updateById("id2", builder -> { });

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

    repository.updateById("id1", builder -> { });

    givenNewRepository();

    doThrow(IOException.class).when(parser).read(any());

    MetricUpdater updater = mock(MetricUpdater.class);
    repository.updateById("id1", updater);

    verify(updater, never()).update(any());
  }

  @Test
  public void updateById_GivenIoExceptionDuringWriteOfNewMetric_DoNotCreateMetric() throws Exception {
    parser = spy(parser);
    givenNewRepository();

    doThrow(IOException.class).when(parser).write(any(), any());

    repository.updateById("id1", builder -> { });

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

    repository.updateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(1337L);
    });

    repository.updateById("id1", builder -> {
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

    repository.updateById("id1", builder -> { });

    doThrow(IOException.class).when(parser).write(any(), any());

    repository.updateById("id1", builder -> {
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

    repository.updateById("id1", builder -> { });

    doThrow(IOException.class)
        .doCallRealMethod()
        .when(parser)
        .write(any(), any());

    repository.updateById("id1", builder -> {
      builder.setCdbCallStartTimestamp(1337L);
    });

    repository.updateById("id1", builder -> {
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
    when(mover.shouldMove(any())).thenReturn(true);
    when(mover.offerToDestination(any())).thenReturn(true);

    repository.updateById("id", builder -> builder.setCdbCallStartTimestamp(1L));
    repository.moveAllWith(mover);

    repository.updateById("id", builder -> {
      assertEquals(builder.build(), Metric.builder("id").build());
    });
  }

  @Test
  public void moveAllWith_GivenEmptyRepository_DoNothing() throws Exception {
    MetricMover mover = mock(MetricMover.class);

    repository.moveAllWith(mover);

    verifyNoInteractions(mover);
  }

  @Test
  public void moveAllWith_GivenRepositoryWithMetricAndSuccessfulMove_RemoveAll() throws Exception {
    MetricMover mover = mock(MetricMover.class);
    when(mover.shouldMove(any())).thenReturn(true);
    when(mover.offerToDestination(any())).thenReturn(true);

    repository.updateById("id1", builder -> {});
    givenNewRepository();
    repository.updateById("id2", builder -> {});

    repository.moveAllWith(mover);

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    verify(mover, times(2)).shouldMove(any());
    verify(mover, times(2)).offerToDestination(any());
    assertTrue(metrics.isEmpty());
  }

  @Test
  public void moveAllWith_GivenRepositoryWithMetricAndUnsuccessfulMove_RollbackAll() throws Exception {
    MetricMover mover = mock(MetricMover.class);
    when(mover.shouldMove(any())).thenReturn(true);
    when(mover.offerToDestination(any())).thenReturn(false);

    repository.updateById("id1", builder -> {});
    givenNewRepository();
    repository.updateById("id2", builder -> {});

    repository.moveAllWith(mover);

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(2, metrics.size());
    assertTrue(metrics.contains(Metric.builder("id1").build()));
    assertTrue(metrics.contains(Metric.builder("id2").build()));
  }

  @Test
  public void moveAllWith_GivenRepositoryWithSuccessfulAndUnsuccessfulMove_RemoveMovedOnes() throws Exception {
    MetricMover mover = mock(MetricMover.class);
    when(mover.shouldMove(any())).thenReturn(true);
    when(mover.offerToDestination(any())).thenReturn(false).thenReturn(true);

    repository.updateById("id1", builder -> {});
    givenNewRepository();
    repository.updateById("id2", builder -> {});

    repository.moveAllWith(mover);

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    assertEquals(1, metrics.size());
    assertTrue(metrics.contains(Metric.builder("id1").build()));
  }

  @Test
  public void moveAllWith_GivenErrorWhenReadingMetric_IgnoreErrorAndDeleteOthers() throws Exception {
    MetricMover mover = mock(MetricMover.class);
    when(mover.shouldMove(any())).thenReturn(true);
    when(mover.offerToDestination(any())).thenReturn(true);

    parser = spy(parser);

    repository.updateById("id1", builder -> {});
    repository.updateById("id2", builder -> {});
    givenNewRepository();

    doThrow(IOException.class)
        .doCallRealMethod()
        .when(parser).read(any());

    repository.moveAllWith(mover);

    Collection<Metric> metrics = repository.getAllStoredMetrics();

    verify(mover, times(1)).shouldMove(any());
    verify(mover).shouldMove(argThat(metric -> {
      assertEquals(Metric.builder("id2").build(), metric);
      return true;
    }));

    assertEquals(1, metrics.size());
    assertTrue(metrics.contains(Metric.builder("id1").build()));
  }

  private void waitForPotentialIO() {
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void givenNewRepository() {
    Context context = InstrumentationRegistry.getContext().getApplicationContext();
    directory = new MetricDirectory(context, parser);
    repository = new MetricRepository(directory);
  }

}
package com.criteo.publisher.csm;

import android.support.annotation.NonNull;
import com.criteo.publisher.DependencyProvider.Factory;
import java.util.ArrayList;
import java.util.List;

public class MetricSendingQueueFactory implements Factory<MetricSendingQueue> {

  @Override
  public MetricSendingQueue create() {
    // FIXME EE-985
    return new MetricSendingQueue() {
      @Override
      public boolean offer(@NonNull Metric metric) {
        return true;
      }

      @NonNull
      @Override
      public List<Metric> poll(int max) {
        return new ArrayList<>();
      }

      @Override
      public int getTotalSize() {
        return 0;
      }
    };
  }
}

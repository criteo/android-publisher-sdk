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

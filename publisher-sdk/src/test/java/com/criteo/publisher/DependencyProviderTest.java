package com.criteo.publisher;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.network.PubSdkApi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;

public class DependencyProviderTest {


  @Mock
  private DependencyProvider dependencyProvider;

  @Mock
  private PubSdkApi pubSdkApi;

  @Mock
  private AdvertisingInfo advertisingInfo;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() {
    DependencyProvider.setInstance(null);
  }

  @Test
  public void verifyDependencies() {
    // given
    when(dependencyProvider.providePubSdkApi(any())).thenReturn(pubSdkApi);
    when(dependencyProvider.provideAdvertisingInfo()).thenReturn(advertisingInfo);
    DependencyProvider.setInstance(dependencyProvider);

    // when, then
    assertTrue(pubSdkApi == DependencyProvider.getInstance().providePubSdkApi(any()));
    assertTrue(advertisingInfo == DependencyProvider.getInstance().provideAdvertisingInfo());

  }
}

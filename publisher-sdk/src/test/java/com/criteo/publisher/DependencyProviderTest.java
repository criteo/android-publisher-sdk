package com.criteo.publisher;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.network.PubSdkApi;
import java.util.function.Function;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
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
    when(dependencyProvider.providePubSdkApi()).thenReturn(pubSdkApi);
    when(dependencyProvider.provideAdvertisingInfo()).thenReturn(advertisingInfo);
    DependencyProvider.setInstance(dependencyProvider);

    // when, then
    assertTrue(pubSdkApi == DependencyProvider.getInstance().providePubSdkApi());
    assertTrue(advertisingInfo == DependencyProvider.getInstance().provideAdvertisingInfo());
  }

  @Test
  public void provideDeviceUtil_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(provider ->
        provider.provideDeviceUtil(mock(Context.class)));
  }

  @Test
  public void provideUserPrivacyUtil_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(provider ->
        provider.provideUserPrivacyUtil(mock(Context.class)));
  }

  @Test
  public void provideConfig_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(provider ->
        provider.provideConfig(mock(Context.class, Answers.RETURNS_DEEP_STUBS)));
  }

  @Test
  public void provideAndroidUtil_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(provider ->
        provider.provideAndroidUtil(mock(Context.class)));
  }

  @Test
  public void provideAdUnitMapper_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(provider ->
        provider.provideAdUnitMapper(mock(Context.class)));
  }

  @Test
  public void provideBidManager_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(provider -> {
      doReturn(mock(DeviceUtil.class)).when(provider).provideDeviceUtil(any());
      doReturn(mock(Config.class)).when(provider).provideConfig(any());

      return provider.provideBidManager(mock(Context.class), "cpId");
    });
  }

  @Test
  public void providePubSdkApi_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(DependencyProvider::providePubSdkApi);
  }

  @Test
  public void provideUser_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(provider -> {
      doReturn(mock(DeviceUtil.class)).when(provider).provideDeviceUtil(any());

      return provider.provideUser(mock(Context.class));
    });
  }

  @Test
  public void provideDeviceInfo_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(provider ->
        provider.provideDeviceInfo(mock(Context.class)));
  }

  @Test
  public void provideAdvertisingInfo_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(DependencyProvider::provideAdvertisingInfo);
  }

  @Test
  public void provideClock_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(DependencyProvider::provideClock);
  }

  @Test
  public void provideLoggingUtil_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(DependencyProvider::provideLoggingUtil);
  }

  @Test
  public void provideSerialExecutor_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(DependencyProvider::provideSerialExecutor);
  }

  @Test
  public void provideThreadPoolExecutor_WhenProvidedTwice_ReturnsTheSame() throws Exception {
    provideBean_WhenProvidedTwice_ReturnsTheSame(DependencyProvider::provideThreadPoolExecutor);
  }

  private <T> void provideBean_WhenProvidedTwice_ReturnsTheSame(
      Function<DependencyProvider, T> providing) {
    DependencyProvider instance = spy(DependencyProvider.getInstance());
    T bean1 = providing.apply(instance);
    T bean2 = providing.apply(instance);

    assertTrue(bean1 == bean2);
  }
}

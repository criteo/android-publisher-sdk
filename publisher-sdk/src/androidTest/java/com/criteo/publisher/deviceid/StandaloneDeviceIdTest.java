package com.criteo.publisher.deviceid;

import static org.mockito.Mockito.when;

import android.support.test.runner.AndroidJUnit4;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.network.PubSdkApi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

// TODO:  https://jira.criteois.com/browse/EE-597
@RunWith(AndroidJUnit4.class)
public class StandaloneDeviceIdTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

  @Mock
  private AdvertisingInfo advertisingInfo;

  @Mock
  private PubSdkApi pubSdkApi;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();
    when(dependencyProvider.providePubSdkApi()).thenReturn(pubSdkApi);
  }

  @Test
  public void testEmptyGAID_CallsBearcatWithEmptyParams() {

  }
}

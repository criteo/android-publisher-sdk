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

package com.criteo.publisher;

import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.view.LayoutInflater;
import com.criteo.publisher.mock.MockedDependenciesRule;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;

public class CriteoBannerViewTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  @Test
  public void inflatedFromXml_GivenNoAttribute_InHouseIsSet() {
    runOnMainThreadAndWait(() -> {
      LayoutInflater inflater = LayoutInflater.from(context);

      CriteoBannerView criteoBannerView = (CriteoBannerView) inflater
          .inflate(com.criteo.publisher.tests.R.layout.test_criteo_banner_view_inhouse, null);

      assertThat(criteoBannerView.bannerAdUnit).isNull();
    });
  }

  @Test
  public void inflatedFromXml_GivenAllAttributeSet_StandaloneIsSet() {
    runOnMainThreadAndWait(() -> {
      LayoutInflater inflater = LayoutInflater.from(context);

      CriteoBannerView criteoBannerView = (CriteoBannerView) inflater
          .inflate(com.criteo.publisher.tests.R.layout.test_criteo_banner_view_standalone, null);

      assertThat( criteoBannerView.bannerAdUnit.getSize().getWidth()).isEqualTo(100);
      assertThat(criteoBannerView.bannerAdUnit.getSize().getHeight()).isEqualTo(50);
      assertEquals("criteoAdUnitId", criteoBannerView.bannerAdUnit.getAdUnitId());
      assertThat(criteoBannerView.bannerAdUnit.getAdUnitId()).isEqualTo("criteoAdUnitId");
    });
  }

  @Test
  public void inflatedFromXml_GivenAdUnitIdIsOnlySet_ThenThrow() {
    assertThatCode(() -> runOnMainThreadAndWait(() -> {
      LayoutInflater inflater = LayoutInflater.from(context);
      inflater.inflate(
          com.criteo.publisher.tests.R.layout.test_criteo_banner_view_illegal_1,
          null
      );
    })).hasCauseInstanceOf(ExecutionException.class);
  }

  @Test
  public void inflatedFromXml_GivenAdUnitWidthIsOnlySet_ThenThrow() {
    assertThatCode(() -> runOnMainThreadAndWait(() -> {
      LayoutInflater inflater = LayoutInflater.from(context);
      inflater.inflate(
          com.criteo.publisher.tests.R.layout.test_criteo_banner_view_illegal_2,
          null
      );
    })).hasCauseInstanceOf(ExecutionException.class);
  }

  @Test
  public void inflatedFromXml_GivenAdUnitHeightIsOnlySet_ThenThrow() {
    assertThatCode(() -> runOnMainThreadAndWait(() -> {
      LayoutInflater inflater = LayoutInflater.from(context);
      inflater.inflate(
          com.criteo.publisher.tests.R.layout.test_criteo_banner_view_illegal_3,
          null
      );
    })).hasCauseInstanceOf(ExecutionException.class);
  }
}

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

package com.criteo.publisher.advancednative;

import static com.criteo.publisher.StubConstants.STUB_CREATIVE_IMAGE;
import static com.criteo.publisher.concurrent.ThreadingUtil.callOnMainThreadAndWait;
import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import android.widget.ImageView;
import com.criteo.publisher.mock.MockedDependenciesRule;
import java.net.URL;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;

public class CriteoImageLoaderTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  @Inject
  private ImageLoader imageLoader;

  @Test
  public void givenCreativeImageUrl_ImageLoader_LoadsIt() throws Exception {
    URL url = new URL(STUB_CREATIVE_IMAGE);

    ImageView imageView = callOnMainThreadAndWait(() -> {
      ImageView view = new ImageView(context);
      imageLoader.loadImageInto(url, view, /* placeholder */ null);
      return view;
    });

    mockedDependenciesRule.waitForIdleState();
    assertThat(imageView.getDrawable()).isNotNull();
  }
}

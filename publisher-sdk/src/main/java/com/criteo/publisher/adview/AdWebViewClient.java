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

package com.criteo.publisher.adview;

import android.content.ComponentName;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.criteo.publisher.DependencyProvider;

public class AdWebViewClient extends WebViewClient {

  @NonNull
  private final RedirectionListener listener;

  @Nullable
  private final ComponentName hostActivityName;

  @NonNull
  private final Redirection redirection;

  public AdWebViewClient(
      @NonNull Context context,
      @NonNull RedirectionListener listener,
      @Nullable ComponentName hostActivityName
  ) {
    this.listener = listener;
    this.hostActivityName = hostActivityName;
    this.redirection = DependencyProvider.getInstance().provideRedirection(context);
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    redirection.redirect(url, hostActivityName, listener);
    return true;
  }

}

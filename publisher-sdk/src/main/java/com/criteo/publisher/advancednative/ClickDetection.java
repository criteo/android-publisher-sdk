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

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import java.util.ArrayDeque;
import java.util.Queue;

public class ClickDetection {

  /**
   * Watch the given {@link View} and notify the given {@link NativeViewClickHandler} if clicked.
   * <p>
   * Clicks are detected on the given view and all its children. Visually, this means that clicking
   * anywhere inside the given view will trigger the handler.
   * <p>
   * It is safe to call again this method with the same view and handler, and it is also safe to
   * call again with the same view and an other handler. For a given view, only the last registered
   * handler will be invoked. Hence, when having recycled view, you do not need to clean it
   * before.
   *
   * @param rootView new view or recycle view to watch for visibility
   * @param handler handler to trigger once click is detected
   */
  void watch(@NonNull View rootView, @NonNull NativeViewClickHandler handler) {
    OnClickListener onClickListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        handler.onClick();
      }
    };

    Queue<View> hierarchy = new ArrayDeque<>();
    hierarchy.add(rootView);

    while (!hierarchy.isEmpty()) {
      View view = hierarchy.remove();

      view.setOnClickListener(onClickListener);

      if (view instanceof ViewGroup) {
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
          hierarchy.add(viewGroup.getChildAt(i));
        }
      }
    }
  }

}

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

package com.criteo.publisher.concurrent;

import androidx.annotation.NonNull;

/**
 * This is a mock implementation of the {@link RunOnUiThreadExecutor} class.
 * <p>
 * Instead of posting given commands on the UI thread, it executes them directly on the current one.
 * This may be used to ease the testing and ignore the complexity of having asynchronous code.
 * <p>
 * Please notice that you still need to provide tests validating the integrations in an async
 * context, and especially on the UI thread.
 */
public class DirectMockRunOnUiThreadExecutor extends RunOnUiThreadExecutor {

  private final DirectMockExecutor delegate = new DirectMockExecutor();

  @Override
  public void execute(@NonNull Runnable command) {
    delegate.execute(command);
  }

  @Override
  public void executeAsync(@NonNull Runnable command) {
    execute(command);
  }

  public void expectIsRunningInExecutor() {
    delegate.expectIsRunningInExecutor();
  }

  public void verifyExpectations() {
    delegate.verifyExpectations();
  }

}

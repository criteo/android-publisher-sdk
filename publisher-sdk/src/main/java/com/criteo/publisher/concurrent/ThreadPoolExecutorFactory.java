/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.criteo.publisher.concurrent;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import com.criteo.publisher.DependencyProvider.Factory;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorFactory implements Factory<ThreadPoolExecutor> {

  /**
   * These constants were taken from the default {@link android.os.AsyncTask} configuration as of
   * API level 29.
   */
  private static final int CORE_POOL_SIZE = 1;
  private static final int MAXIMUM_POOL_SIZE = 20;
  private static final int BACKUP_POOL_SIZE = 5;
  private static final int KEEP_ALIVE_SECONDS = 3;

  /**
   * Create new thread pools independent from the {@linkplain android.os.AsyncTask#THREAD_POOL_EXECUTOR
   * Android one}.
   *
   * Created executor is made for those needs:
   * <ul>
   *   <li>Tasks are IO bounds</li>
   *   <li>Tasks are independent, this means that a long task should not limit another one</li>
   *   <li>There is a burst of tasks at the initialization of the SDK</li>
   * </ul>
   */
  @NonNull
  @Override
  public ThreadPoolExecutor create() {
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        CORE_POOL_SIZE,
        MAXIMUM_POOL_SIZE,
        KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
        new SynchronousQueue<>(),
        Executors.defaultThreadFactory()
    );

    threadPoolExecutor.setRejectedExecutionHandler(new BackupExecutionHandler());

    return threadPoolExecutor;
  }

  private static class BackupExecutionHandler implements RejectedExecutionHandler {

    @GuardedBy("this")
    private ThreadPoolExecutor backupExecutor;

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
      // As a last ditch fallback, run it on an executor with an unbounded queue.
      // Create this executor lazily, hopefully almost never.
      synchronized (this) {
        if (backupExecutor == null) {
          backupExecutor = new ThreadPoolExecutor(
              BACKUP_POOL_SIZE,
              BACKUP_POOL_SIZE,
              KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
              new LinkedBlockingQueue<>(),
              Executors.defaultThreadFactory());
        }
      }

      backupExecutor.execute(r);
    }
  }

}

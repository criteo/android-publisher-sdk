/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      LICENSE-2.0" target="_blank" rel="nofollow">http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.criteo.publisher.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This factory recreate thread pools such as the {@linkplain android.os.AsyncTask#THREAD_POOL_EXECUTOR
 * ones used by Android}.
 */
public class AndroidThreadPoolExecutorFactory {

  private static final int CORE_POOL_SIZE = 1;
  private static final int MAXIMUM_POOL_SIZE = 20;
  private static final int KEEP_ALIVE_SECONDS = 3;

  public ExecutorService create() {
    BlockingQueue<Runnable> poolWorkQueue = new LinkedBlockingQueue<Runnable>();

    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
        poolWorkQueue, AndroidThreadFactory.INSTANCE);
    threadPoolExecutor.allowCoreThreadTimeOut(true);
    return threadPoolExecutor;
  }

  private static class AndroidThreadFactory implements ThreadFactory {

    private static final ThreadFactory INSTANCE = new AndroidThreadFactory();

    private final AtomicInteger mCount = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
    }
  }

}

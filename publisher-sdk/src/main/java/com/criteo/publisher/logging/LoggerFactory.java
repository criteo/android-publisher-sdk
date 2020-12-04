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

package com.criteo.publisher.logging;

import androidx.annotation.NonNull;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.dependency.LazyDependency;
import java.util.List;

public class LoggerFactory {

  @NonNull
  private final List<LazyDependency<LogHandler>> logHandlers;

  public LoggerFactory(@NonNull List<LazyDependency<LogHandler>> logHandlers) {
    this.logHandlers = logHandlers;
  }

  @NonNull
  public static Logger getLogger(@NonNull Class<?> klass) {
    return DependencyProvider.getInstance().provideLoggerFactory().createLogger(klass);
  }

  public Logger createLogger(@NonNull Class<?> klass) {
    return new Logger(klass, logHandlers);
  }

}

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

package com.criteo.publisher.application;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.annotation.SuppressLint;
import android.app.Application;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public class ApplicationMock {

  @SuppressLint({"CommitPrefEdits", "NewApi"})
  public static Application newMock() {
    // Used by CSM to store metric on filesystem
    File filesDir;
    try {
      filesDir = Files.createTempDirectory(ApplicationMock.class.getName()).toFile();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    Application application = mock(Application.class, RETURNS_DEEP_STUBS);
    when(application.getApplicationContext().getFilesDir()).thenReturn(filesDir);
    when(application.getApplicationContext().getPackageName()).thenReturn("com.criteo.dummy.bundle");
    return application;
  }

}

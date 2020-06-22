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

package com.criteo.publisher.mock;

import static org.mockito.AdditionalAnswers.returnsArgAt;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.SharedPreferences;

public class ApplicationMock {

  public static Application newMock() {
    // Explicitly mock shared preferences because it is a pain to handle them in unit tests.
    // This is more true for the getString one, because, since String class is final, it is
    // returning null, and provoke unexpected NPE.
    // Here we consider that all shared preferences are empty. We can still mock them if we want to
    // test scenarios where data is stored.
    SharedPreferences sharedPreferences = mock(SharedPreferences.class);
    when(sharedPreferences.getInt(any(), anyInt())).thenAnswer(returnsArgAt(1));
    when(sharedPreferences.getBoolean(any(), anyBoolean())).thenAnswer(returnsArgAt(1));
    when(sharedPreferences.getString(any(), any())).thenAnswer(returnsArgAt(1));

    Application application = mock(Application.class, RETURNS_DEEP_STUBS);
    when(application.getApplicationContext().getSharedPreferences(any(), anyInt()))
        .thenReturn(sharedPreferences);
    return application;
  }

}

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

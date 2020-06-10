package com.criteo.publisher;

public class CriteoNotInitializedException extends IllegalStateException {

  public CriteoNotInitializedException(String message) {
    super(message + "\n"
        + "Did you initialize the Criteo SDK ?\n"
        + "Please follow this step: https://publisherdocs.criteotilt.com/app/android/standalone/#sdk-initialization\n");
  }

}

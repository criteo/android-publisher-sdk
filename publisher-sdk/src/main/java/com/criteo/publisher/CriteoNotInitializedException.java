package com.criteo.publisher;

public class CriteoNotInitializedException extends IllegalStateException {

  public CriteoNotInitializedException(String message) {
    super(message + "\nDid you initialized the Criteo SDK ?");
  }

}

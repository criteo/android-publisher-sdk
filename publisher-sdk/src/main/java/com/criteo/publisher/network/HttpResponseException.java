package com.criteo.publisher.network;

import java.io.IOException;

public class HttpResponseException extends IOException {

  public HttpResponseException(int status) {
    super("Received HTTP error status: " + status);
  }
}

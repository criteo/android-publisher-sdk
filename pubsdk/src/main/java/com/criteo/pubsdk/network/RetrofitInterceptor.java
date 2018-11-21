package com.criteo.pubsdk.network;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetrofitInterceptor implements okhttp3.Interceptor {

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Request.Builder builder = request.newBuilder()
        .addHeader("Content-Type", "text/plain");
    request = builder.build();
    Response response = chain.proceed(request);
    return response;
  }
}

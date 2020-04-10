package com.criteo.publisher.view;

import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.junit.Assert.assertEquals;

import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.webkit.WebView;
import android.webkit.WebView.VisualStateCallback;
import com.criteo.publisher.util.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class WebViewClicker {

  private static final String AD_ID = "click";

  public String getAdHtmlWithClickUrl(@NonNull String clickUrl) {
    return "<html><body><a href='" + clickUrl + "' id='" + AD_ID + "'>My Awesome Ad</a></body></html>";
  }

  @RequiresApi(api = VERSION_CODES.O)
  public void loadHtmlAndSimulateClickOnAd(@NonNull WebView webView, @NonNull String clickUrl) throws Exception {
    String html = getAdHtmlWithClickUrl(clickUrl);

    runOnMainThreadAndWait(() -> {
      webView.loadData(html, "text/html", "UTF-8");
    });

    simulateClickOnAd(webView);
  }

  @RequiresApi(api = VERSION_CODES.M)
  public void simulateClickOnAd(@NonNull WebView webView) throws Exception {
    waitUntilWebViewIsLoaded(webView);

    CompletableFuture<Void> isClickDone = new CompletableFuture<>();

    // Simulate click via JavaScript
    runOnMainThreadAndWait(() -> {
      webView.evaluateJavascript("(function() {\n"
          + "  var element = document.getElementById('" + AD_ID + "');\n"
          + "  if (element === null) {\n"
          + "    return false;\n"
          + "  }\n"
          + "  element.click();\n"
          + "  return true;\n"
          + "})();", value -> {
        if (!"true".equals(value)) {
          isClickDone.completeExceptionally(new IllegalStateException("Clickable element was not found in the WebView"));
        } else {
          isClickDone.complete(null);
        }
        assertEquals("Clickable element was not found in the WebView", "true", value);
      });
    });

    isClickDone.get();
  }

  @RequiresApi(api = VERSION_CODES.M)
  private void waitUntilWebViewIsLoaded(@NonNull WebView webView) throws Exception {
    CountDownLatch isHtmlLoaded = new CountDownLatch(1);

    runOnMainThreadAndWait(() -> {
      webView.postVisualStateCallback(42, new VisualStateCallback() {
        @Override
        public void onComplete(long ignored) {
          isHtmlLoaded.countDown();
        }
      });
    });

    isHtmlLoaded.await();
  }

}

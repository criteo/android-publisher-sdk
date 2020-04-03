package com.criteo.publisher.view;

import static com.criteo.publisher.concurrent.ThreadingUtil.runOnMainThreadAndWait;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;
import android.support.test.InstrumentationRegistry;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.util.CompletableFuture;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RequiresApi(api = VERSION_CODES.N)
public class WebViewLookup {

  private static final String GET_OUTER_HTML = "(function() { return encodeURIComponent(document.getElementsByTagName('html')[0].outerHTML); })();";

  private final ExecutorService executor = Executors.newWorkStealingPool();

  /**
   * Look inside the given view for {@link android.webkit.WebView}. Then if exactly one webview is
   * found, then its HTML content is fetch.
   * <p>
   * If no webview is found, then the future will fail with a {@link java.util.NoSuchElementException}.
   * And if more than one webview are found, then the future will fail with a {@link
   * TooManyWebViewsException}.
   *
   * @param root the view to look into
   * @return either the HTML content of the found webview, either an error state.
   */
  public Future<String> lookForHtmlContent(View root) {
    CompletableFuture<String> future = new CompletableFuture<>();
    List<WebView> webViews = lookForWebViews(root);

    if (webViews.isEmpty()) {
      future.completeExceptionally(new NoSuchElementException());
    } else if (webViews.size() > 1) {
      future.completeExceptionally(new TooManyWebViewsException());
    } else {
      WebView webView = webViews.get(0);
      runOnMainThreadAndWait(() -> {
        webView.evaluateJavascript(GET_OUTER_HTML, value -> {
          try {
            future.complete(decodeHtmlString(value));
          } catch (UnsupportedEncodingException e) {
            future.completeExceptionally(e);
          }
        });
      });
    }

    return future;
  }

  private String decodeHtmlString(String value) throws UnsupportedEncodingException {
    String htmlWithoutQuotes = value.substring(1, value.length() - 1);
    return URLDecoder.decode(htmlWithoutQuotes, "UTF-8");
  }

  public List<WebView> lookForWebViews(View root) {
    List<WebView> views = new ArrayList<>();

    traverse(root, view -> {
      if (view instanceof WebView) {
        views.add(((WebView) view));
      }
    });

    return views;
  }

  public Future<Activity> lookForResumedActivity(CheckedRunnable action) {
    CompletableFuture<Activity> activityFuture = new CompletableFuture<>();
    ActivityLifecycleCallbacks lifecycleCallbacks = mock(ActivityLifecycleCallbacks.class);
    doAnswer(answerVoid(activityFuture::complete)).when(lifecycleCallbacks).onActivityResumed(any());

    Application application = (Application) InstrumentationRegistry.getTargetContext()
        .getApplicationContext();
    application.registerActivityLifecycleCallbacks(lifecycleCallbacks);

    executor.submit(() -> {
      try {
        action.run();
      } catch (Exception e) {
        activityFuture.completeExceptionally(e);
      }

      return null;
    });

    return executor.submit(() -> {
      try {
        return activityFuture.get();
      } finally {
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks);
      }
    });
  }

  public static View getRootView(Activity activity) {
    return activity.getWindow().getDecorView().getRootView();
  }

  private void traverse(View root, Processor processor) {
    processor.process(root);

    if (root instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) root;
      int childCount = group.getChildCount();

      for (int i = 0; i < childCount; ++i) {
        final View child = group.getChildAt(i);
        traverse(child, processor);
      }
    }
  }

  /**
   * Collect the list of all resources loaded by the given HTML code.
   * <p>
   * This method (almost) directly return an async future of the result. The future is completed
   * once the given HTML is completely loaded, without cache, including the javascript.
   * <p>
   * Note that only GET resources are triggered, coming from the HTML itself or from a JS script.
   *
   * @param html    code to evaluate
   * @param charset encoding of the given html
   * @return list of resources triggered by the given html.
   */
  public Future<List<String>> lookForLoadedResources(String html, Charset charset) {
    CompletableFuture<List<String>> future = new CompletableFuture<>();
    List<String> firedUrls = new ArrayList<>();

    WebViewClient client = new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        future.complete(firedUrls);
      }

      @Override
      public void onLoadResource(WebView view, String url) {
        firedUrls.add(url);
      }
    };

    runOnMainThreadAndWait(() -> {
      WebView webView = new WebView(InstrumentationRegistry.getContext());
      webView.clearCache(true);
      webView.setWebViewClient(client);
      webView.getSettings().setJavaScriptEnabled(true);
      webView.loadDataWithBaseURL(null, html, "text/html", charset.name(), null);
    });

    return future;
  }

  public interface CheckedRunnable {

    void run() throws Exception;
  }

  private interface Processor {

    void process(View view);
  }

  public static final class TooManyWebViewsException extends IllegalStateException {

  }

}

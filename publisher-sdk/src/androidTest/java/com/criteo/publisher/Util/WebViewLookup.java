package com.criteo.publisher.Util;

import static com.criteo.publisher.ThreadingUtil.runOnMainThreadAndWait;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.support.test.InstrumentationRegistry;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class WebViewLookup {

  private static final String GET_OUTER_HTML = "(function() { return encodeURI(document.getElementsByTagName('html')[0].outerHTML); })();";

  private final ExecutorService executor = Executors.newWorkStealingPool();

  /**
   * Look inside the given view for {@link android.webkit.WebView}.
   * Then if exactly one webview is found, then its HTML content is fetch.
   *
   * If no webview is found, then the future will fail with a
   * {@link java.util.NoSuchElementException}. And if more than one webview are found, then the
   * future will fail with a {@link TooManyWebViewsException}.
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

  public Future<View> lookForResumedActivityView(CheckedRunnable action) {
    BlockingQueue<Activity> queue = new LinkedBlockingQueue<>(1);
    ActivityLifecycleCallbacks lifecycleCallbacks = mock(ActivityLifecycleCallbacks.class);
    doAnswer(answerVoid(queue::put)).when(lifecycleCallbacks).onActivityResumed(any());

    Application application = (Application) InstrumentationRegistry.getTargetContext()
        .getApplicationContext();
    application.registerActivityLifecycleCallbacks(lifecycleCallbacks);

    executor.submit(() -> {
      action.run();
      return null;
    });

    return executor.submit(() -> {
      try {
        Activity activity = queue.take();
        return activity.getWindow().getDecorView().getRootView();
      } finally {
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks);
      }
    });
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

  public interface CheckedRunnable {
    void run() throws Exception;
  }

  private interface Processor {
    void process(View view);
  }

  public static final class TooManyWebViewsException extends IllegalStateException {
  }

}

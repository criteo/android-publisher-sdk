package com.criteo.publisher.memory;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GarbageCollector {

  /**
   * Force GC multiple times.
   *
   * Compared to {@link #forceGcOnce()}, this increase the reliability of the calling tests, but
   * this cost times.
   *
   * @param times number of times the GC should be forced
   * @throws InterruptedException if interrupted while trying to force the GC
   * @see #forceGcOnce()
   */
  public static void forceGc(int times) throws InterruptedException {
    for (int i = 0; i < times; i++) {
      forceGcOnce();
    }
  }

  /**
   * Force one GC.
   * <p>
   * This method is not totally reliable. A GC will occur, but your memory may not be collected
   * after this method.
   * <p>
   * Tests using with this are flaky, take times, and they should be extracted from other tests.
   * They should also be small, unit and restartable easily.
   *
   * @throws InterruptedException if interrupted while trying to force the GC
   */
  @SuppressWarnings({"UnusedAssignment", "MismatchedQueryAndUpdateOfCollection"})
  public static void forceGcOnce() throws InterruptedException {
    Object obj = new Object();
    WeakReference<?> ref = new WeakReference<>(obj);
    obj = null;

    List<SoftReference<?>> garbage = new ArrayList<>();

    while(ref.get() != null) {
      System.gc();
      System.runFinalization();

      // Consume garbage chunk of 16Mo until GC
      garbage.add(new SoftReference<>(new byte[16 * 1024 * 1024]));

      // Sleep a little to avoid spin lock
      Thread.sleep(1);
    }
  }

}

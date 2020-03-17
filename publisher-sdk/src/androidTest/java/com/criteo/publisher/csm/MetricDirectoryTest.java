package com.criteo.publisher.csm;

import static com.criteo.publisher.csm.MetricDirectoryHelper.clear;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.v4.util.AtomicFile;
import java.io.File;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MetricDirectoryTest {

  @Mock
  private MetricParser parser;

  private MetricDirectory directory;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    Context context = InstrumentationRegistry.getContext().getApplicationContext();
    directory = new MetricDirectory(context, parser);
  }

  @After
  public void tearDown() throws Exception {
    clear(directory);
  }

  @Test
  public void listFiles_GivenEmptyDirectory_ReturnEmpty() throws Exception {
    Collection<File> files = directory.listFiles();

    assertTrue(files.isEmpty());
  }

  @Test
  public void listFiles_WhileAnAtomicFileIsBeingWritten_IgnoreBackupFile() throws Exception {
    File file = directory.createMetricFile("impId");
    AtomicFile atomicFile = new AtomicFile(file);
    atomicFile.startWrite();

    Collection<File> files = directory.listFiles();

    assertEquals(1, files.size());
    assertTrue(files.contains(file));
  }

  @Test
  public void getMetricFile_UntilItIsNotWritten_ItIsNotCreatedOnDisk() throws Exception {
    File file = directory.createMetricFile("impId");
    new AtomicFile(file);

    assertFalse(file.exists());
  }

}
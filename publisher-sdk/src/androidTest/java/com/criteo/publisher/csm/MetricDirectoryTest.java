package com.criteo.publisher.csm;

import static com.criteo.publisher.csm.MetricDirectoryHelper.clear;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.support.v4.util.AtomicFile;
import com.criteo.publisher.Util.BuildConfigWrapper;
import com.criteo.publisher.mock.MockedDependenciesRule;
import java.io.File;
import java.util.Collection;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class MetricDirectoryTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  @Inject
  private Context context;

  @Inject
  private BuildConfigWrapper buildConfigWrapper;

  @Mock
  private MetricParser parser;

  private MetricDirectory directory;

  @Before
  public void setUp() throws Exception {
    directory = new MetricDirectory(context, buildConfigWrapper, parser);
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
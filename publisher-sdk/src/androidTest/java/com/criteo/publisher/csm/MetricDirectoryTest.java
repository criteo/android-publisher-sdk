/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.csm;

import static com.criteo.publisher.csm.MetricDirectoryHelper.clear;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.util.AtomicFile;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.util.BuildConfigWrapper;
import java.io.File;
import java.util.Collection;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
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
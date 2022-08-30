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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Build.VERSION;
import android.util.AtomicFile;
import com.criteo.publisher.csm.Metric.Builder;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.JsonSerializer;
import java.io.File;
import java.util.Arrays;
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
  private JsonSerializer jsonSerializer;

  private MetricDirectory directory;

  @Before
  public void setUp() throws Exception {
    directory = new MetricDirectory(context, buildConfigWrapper, jsonSerializer);
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
  public void listFiles_WhileAnAtomicFileIsBeingWritten_IgnoreTemporaryFile() throws Exception {
    File committedFile = directory.createMetricFile("committed");
    directory.createSyncMetricFile(committedFile).update(Builder::build);

    File pendingFile = directory.createMetricFile("pending");
    AtomicFile atomicFile = new AtomicFile(pendingFile);
    atomicFile.startWrite();

    // Until API 29 included, AtomicFile writes into the base file and store a backup file in case of revert. So the
    // base file is existing during a first write.
    // After API 29, AtomicFile writes into a new file and new file is renamed into the base file in case of commit. So
    // the base file is not existing yet during a first write.
    Collection<File> expected = VERSION.SDK_INT <= 29
        ? Arrays.asList(committedFile, pendingFile)
        : Arrays.asList(committedFile);

    Collection<File> files = directory.listFiles();

    assertThat(files).containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  public void getMetricFile_UntilItIsNotWritten_ItIsNotCreatedOnDisk() throws Exception {
    File file = directory.createMetricFile("impId");
    new AtomicFile(file);

    assertFalse(file.exists());
  }

}
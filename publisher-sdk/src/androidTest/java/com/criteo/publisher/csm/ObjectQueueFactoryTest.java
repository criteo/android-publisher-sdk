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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import com.criteo.publisher.mock.MockedDependenciesRule;
import com.criteo.publisher.mock.SpyBean;
import com.criteo.publisher.util.BuildConfigWrapper;
import com.criteo.publisher.util.JsonSerializer;
import com.squareup.tape.ObjectQueue;
import com.squareup.tape.QueueFile;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ObjectQueueFactoryTest {

  @Rule
  public MockedDependenciesRule mockedDependenciesRule = new MockedDependenciesRule();

  private File queueFile;

  @Inject
  private Context context;

  @Inject
  private JsonSerializer jsonSerializer;

  @SpyBean
  private MetricSendingQueueConfiguration configuration;

  @SpyBean
  private BuildConfigWrapper buildConfigWrapper;

  private ObjectQueueFactory<Metric> factory;

  @Before
  public void setUp() throws Exception {
    when(configuration.getQueueFilename()).thenReturn("queueFile");

    queueFile = new File(context.getFilesDir(), "queueFile");

    factory = spy(new ObjectQueueFactory<>(context, jsonSerializer, configuration));

    doReturn(queueFile).when(factory).getQueueFile();
  }

  @After
  public void tearDown() throws Exception {
    queueFile.delete();
  }

  @Test
  public void sanityCheck_GivenNotExistingFileAndDoNotCreateQueue_QueueFileDoesNotExist() throws Exception {
    queueFile.createNewFile();
    queueFile.delete();

    // no factory.create()

    assertFalse(queueFile.exists());
  }

  @Test
  public void create_GivenNotExistingQueueFile_CreateFileAndQueueIsWorking() throws Exception {
    queueFile.delete();

    ObjectQueue<Metric> queue = factory.create();

    assertTrue(queueFile.exists());
    assertNull(queue.peek());
  }


  @Test
  public void create_GivenStaleQueueFile_RecreateFileAndQueueIsWorking() throws Exception {
    givenDeactivatedPreconditionUtils();

    byte[] garbage = new byte[]{42, 13, 37};
    try (FileOutputStream fos = new FileOutputStream(queueFile)) {
      fos.write(garbage);
    }

    ObjectQueue<Metric> metricObjectQueue = factory.create();

    byte[] fileContent = Files.readAllBytes(queueFile.toPath());
    assertTrue(queueFile.exists());
    assertFalse(Arrays.equals(fileContent, garbage));
    assertNull(metricObjectQueue.peek());
  }

  /**
   * This scenario seems to happen randomly: the queue seems to get stale, maybe when writing
   * during a crash.
   */
  @Test
  public void create_GivenCorruptedQueueFileWithEmptyElements_RecreateFileAndQueueIsWorking() throws Exception {
    givenDeactivatedPreconditionUtils();

    QueueFile rawQueueFile = new QueueFile(queueFile);
    rawQueueFile.add(new byte[0]);

    ObjectQueue<Metric> metricObjectQueue = factory.create();

    assertTrue(queueFile.exists());
    assertNull(metricObjectQueue.peek());
  }

  @Test
  public void create_GivenQueueFileIsADirectory_RecreateFileAndQueueIsWorking() throws Exception {
    givenDeactivatedPreconditionUtils();

    queueFile.mkdirs();

    ObjectQueue<Metric> metricObjectQueue = factory.create();

    assertTrue(queueFile.exists());
    assertFalse(queueFile.isDirectory());
    assertNull(metricObjectQueue.peek());
  }

  @Test
  public void create_GivenQueueFileIsADirectoryWithContent_RecreateFileAndQueueIsWorking() throws Exception {
    givenDeactivatedPreconditionUtils();

    queueFile.mkdirs();
    new File(queueFile, "dummyContent").createNewFile();

    ObjectQueue<Metric> metricObjectQueue = factory.create();

    assertTrue(queueFile.exists());
    assertFalse(queueFile.isDirectory());
    assertNull(metricObjectQueue.peek());
  }

  private void givenDeactivatedPreconditionUtils() {
    when(buildConfigWrapper.preconditionThrowsOnException()).thenReturn(false);
  }
}

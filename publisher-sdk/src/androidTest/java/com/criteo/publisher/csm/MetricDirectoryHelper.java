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

import static org.junit.Assert.assertNotNull;

import java.io.File;

public class MetricDirectoryHelper {

  static void clear(MetricDirectory directory) {
    File repositoryDirectory = directory.getDirectoryFile();
    File[] files = repositoryDirectory.listFiles();

    assertNotNull(files);

    for (File file : files) {
      file.delete();
    }
    repositoryDirectory.delete();
  }

}

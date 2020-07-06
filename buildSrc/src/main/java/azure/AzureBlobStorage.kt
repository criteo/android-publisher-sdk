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

package azure

import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.CloudBlobContainer
import org.gradle.api.Action
import java.nio.file.Files
import java.nio.file.Path

class AzureBlobStorage(private val containerName: String) {

  var connectionString: String? = null

  private val blobContainer: CloudBlobContainer by lazy(::createBlobContainer)

  constructor(containerName: String, configure: Action<in AzureBlobStorage>): this(containerName) {
    configure.execute(this)
  }

  /**
   * Download the specified blob at the given path location.
   * <p>
   * If the given path location does not exist, it will be created automatically.
   * If the blob does not exist, nothing is done.
   */
  fun download(blobName: String, path: Path) {
    val blob = blobContainer.getBlockBlobReference(blobName)
    if (!blob.exists()) {
      return
    }

    createFileIfNotExists(path)

    Files.newOutputStream(path).use {
      blob.download(it)
    }
  }

  private fun createFileIfNotExists(path: Path) {
    if (Files.notExists(path)) {
      path.parent?.let {
        Files.createDirectories(it)
      }
      Files.createFile(path)
    }
  }

  /**
   * Upload the given file as a blob under the same name (prefixed if defined).
   * <p>
   * For instance, if a file <code>myFile.txt</code> is given, and the prefix <code>myPrefix/</code>
   * is given, then this will upload the file under the blob name <code>myPrefix/myFile.txt</code>
   * <p>
   * If the given file is a directory, then this will upload recursively all files under it.
   * For instance, if prefix is <code>myPrefix/</code> and if the directory is like this:
   * <ul>
   *   <li>myDirectory/myFile.txt</li>
   *   <li>myDirectory/mySubdirectory/myFile.txt</li>
   * </ul>
   * <p>
   * Then this will create blobs such as:
   * <ul>
   *   <li>myPrefix/myDirectory/myFile.txt</li>
   *   <li>myPrefix/myDirectory/mySubdirectory/myFile.txt</li>
   * </ul>
   * <p>
   * If a blob already exists at the target location, it will be overwritten.
   */
  fun upload(path: Path, blobPrefix: String = "") {
    val blobName = blobPrefix + path.fileName

    when {
      Files.isDirectory(path) -> {
        Files.list(path).forEach {
          upload(it, "$blobName/")
        }
      }
      Files.isRegularFile(path) -> {
        uploadBlob(path, blobName)
      }
      else -> {
        throw UnsupportedOperationException()
      }
    }
  }

  private fun uploadBlob(path: Path, blobName: String) {
    val blob = blobContainer.getBlockBlobReference(blobName)

    println("Upload $path to $blobName")
    Files.newInputStream(path).use {
      blob.upload(it, Files.size(path))
    }
  }

  private fun createBlobContainer(): CloudBlobContainer {
    val cloudStorageAccount = CloudStorageAccount.parse(connectionString)
    val serviceClient = cloudStorageAccount.createCloudBlobClient()
    return serviceClient.getContainerReference(containerName)
  }

}
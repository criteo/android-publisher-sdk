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

package com.criteo.publisher.util;

import androidx.annotation.NonNull;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;

public interface CdbCallListener {

  void onCdbRequest(@NonNull CdbRequest request);

  void onCdbResponse(@NonNull CdbRequest request, @NonNull CdbResponse response);

  void onCdbError(@NonNull CdbRequest request, @NonNull Exception exception);
}

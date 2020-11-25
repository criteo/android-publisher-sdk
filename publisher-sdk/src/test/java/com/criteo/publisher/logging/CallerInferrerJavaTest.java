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

package com.criteo.publisher.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CallerInferrerJavaTest {

  private String methodWithoutParameter() {
    return CallerInferrer.inferCallerName();
  }

  @SuppressWarnings({"unused", "SameParameterValue"})
  private String methodWithParameter(int a, String b, Integer c) {
    return CallerInferrer.inferCallerName();
  }

  @Test
  public void inferCallerName_GivenNamedJavaMethodWithoutParameters_InferNameViaStacktrace() {
    String callerName = methodWithoutParameter();

    assertThat(callerName).isEqualTo("logging.CallerInferrerJavaTest#methodWithoutParameter:26");
  }

  @Test
  public void inferCallerName_GivenNamedJavaMethodWithParameters_InferNameViaStacktrace() {
    String callerName = methodWithParameter(42, null, null);

    assertThat(callerName).isEqualTo("logging.CallerInferrerJavaTest#methodWithParameter:31");
  }

  @Test
  public void inferCallerName_GivenNamedKotlinMethodWithParametersCalledThroughInlineKotlinMethod_InferNameWithParameterTypes() {
    String callerName = CallerInferrerTest.methodInlined();

    assertThat(callerName).isEqualTo("CallerInferrerTest$Companion#methodWithParameter(int, String, Integer)");
  }
}

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

package com.criteo.testapp.mock

import android.util.Log

object NetworkUtil {
  @JvmStatic
  fun logCasperRedirectionWarning(tag: String) {
    Log.w(tag, """
          Casper redirection handler only works on *.criteo.com URL.
          To make it works with other URL, you must add this snippet in OnBeforeResponse method of Fiddler Script:

              if (oSession.uriContains("ads.criteo.com/redirect?url=")) {
                var indexOf = oSession.fullUrl.IndexOf('=');
                var encodedRedirect = oSession.fullUrl.Substring(indexOf + 1);
                var redirect = Uri.UnescapeDataString(encodedRedirect);
                oSession.responseCode = 302;
                oSession.oResponse.headers['location'] = redirect;
              }
          """.trimIndent())
  }
}

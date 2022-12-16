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

package com.criteo.publisher

class MraidData {

  fun getHtmlWithMraidScriptTag(): String {
    return """
      <html>
      <body>
      <script src="mraid.js"></script>
      </body>
      </html>
      """.trimIndent()
  }

  fun getHtmlWithDocumentWriteMraidScriptTag(): String {
    return """
      <html><head></head></html>
      <script type="text/javascript">
       var head = document.getElementsByTagName('head').item(0),
       js = document.createElement('script'),
       s = 'mraid.js';
       js.setAttribute('type', 'text/javascript');
       js.setAttribute('src', s);
       head.appendChild(js);
      </script>
    """.trimIndent()
  }

  fun getHtmlWithoutMraidScript(): String {
    return """
      <html>
      <body>
      <p><b>No MRAID script tag =(</b></p>
      </body>
      </html>
    """.trimIndent()
  }
}

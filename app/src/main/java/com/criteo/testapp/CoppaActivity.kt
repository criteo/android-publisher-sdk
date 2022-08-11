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

package com.criteo.testapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.criteo.publisher.Criteo

class CoppaActivity : AppCompatActivity() {

  private lateinit var spinner: Spinner
  private val coppaOptions = listOf("not set", "true", "false")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_coppa)

    spinner = findViewById(R.id.spinner)
    spinner.adapter = ArrayAdapter(
        this,
        R.layout.support_simple_spinner_dropdown_item,
        coppaOptions
    )
    spinner.setSelection(currentCoppaFlag?.toString()?.let { coppaOptions.indexOf(it) }
        ?: 0)

    findViewById<Button>(R.id.buttonSaveCoppaFlag).setOnClickListener {
      currentCoppaFlag = spinner.selectedItem.let { (it as String).toBooleanStrictOrNull() }
      Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show()
      finish()
    }
  }

  companion object {
    var currentCoppaFlag: Boolean? = null
      private set(value) {
        field = value
        Criteo.getInstance().setTagForChildDirectedTreatment(currentCoppaFlag)
      }
  }
}

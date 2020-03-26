package com.criteo.publisher.Util

import java.io.ByteArrayOutputStream

fun JsonSerializer.writeIntoString(value: Any): String {
  with(ByteArrayOutputStream()) {
    this@writeIntoString.write(value, this)
    return String(toByteArray(), Charsets.UTF_8)
  }
}

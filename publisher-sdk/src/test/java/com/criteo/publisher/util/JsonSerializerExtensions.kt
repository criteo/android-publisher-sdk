package com.criteo.publisher.util

import java.io.ByteArrayOutputStream

fun JsonSerializer.writeIntoString(value: Any): String {
  with(ByteArrayOutputStream()) {
    this@writeIntoString.write(value, this)
    return String(toByteArray(), Charsets.UTF_8)
  }
}

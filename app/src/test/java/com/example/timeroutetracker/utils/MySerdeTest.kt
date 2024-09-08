package com.example.timeroutetracker.utils

import kotlinx.serialization.Serializable
import org.junit.Test
import kotlin.test.assertEquals

class MySerdeTest {
  @Serializable
  enum class MyEnum {
    Option1,
    Option2,
    Option3
  }

  @Test
  fun testMySerde() {
    val a = MySerde.serialize("123456")
    val b: String = MySerde.deserialize(a)
    assertEquals("123456", b)

    val c = MySerde.serialize(123456)
    val d: Int = MySerde.deserialize(c)
    assertEquals(123456, d)

    val e = MySerde.serialize(true)
    val f: Boolean = MySerde.deserialize(e)
    assertEquals(true, f)


    val g = MySerde.serialize(MyEnum.Option1)
    val h: MyEnum = MySerde.deserialize(g)
    assertEquals(MyEnum.Option1, h)
  }
}
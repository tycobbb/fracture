package dev.wizrad.fracture.support

import org.jetbrains.spek.api.DescribeBody

class SharedExamples {
  private val store: MutableMap<String, DescribeBody.() -> Unit> = mutableMapOf()

  fun named(name: String, examples: DescribeBody.() -> Unit) {
    store[name] = examples
  }

  fun run(group: DescribeBody, name: String) {
    val examples = store[name]!!
    group.examples()
  }
}

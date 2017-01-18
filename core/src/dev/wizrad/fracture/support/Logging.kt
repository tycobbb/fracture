package dev.wizrad.fracture.support

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2

// MARK: Configuration
class Logging {
  companion object {
    val level = Application.LOG_DEBUG
  }
}

// MARK: Functions
enum class Tag(val key: String) {
  General("STD"),
  World("WRL"),
  Physics("PHY"),
  Interface("GUI"),
  Rendering("REN")
}

fun error(tag: Tag, message: String) {
  Gdx.app.error("(E) ${tag.key}", message)
}

fun info(tag: Tag, message: String) {
  Gdx.app.log("(I) ${tag.key}", message)
}

fun debug(tag: Tag, message: String) {
  Gdx.app.debug("(D) ${tag.key}", message)
}

// MARK: Formatting
val Any.className: String
  get() = javaClass.simpleName

val Any.debugPrefix: String
  get() = "$className@${hashCode()}"

fun Vector2.fmt(precision: Int = 3): String {
  return "(${x.fmt(precision)}, ${y.fmt(precision)})"
}

fun Number.fmt(precision: Int = 3): String {
  return String.format("%.${precision}f", this)
}

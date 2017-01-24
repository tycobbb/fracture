package dev.wizrad.fracture.game.world.components.contact

import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue

// MARK: Orientation
enum class Orientation {
  Bottom, Left, Top, Right;

  // MARK: Checks
  val isTop: Boolean get() = this == Top
  val isBottom: Boolean get() = this == Bottom
  val isLeft: Boolean get() = this == Left
  val isRight: Boolean get() = this == Right

  // MARK: Serializer
  class Serializer: Json.Serializer<Orientation> {
    override fun read(json: Json?, jsonData: JsonValue?, type: Class<*>?): Orientation {
      val data = jsonData ?: error("$this cannot parse missing json value")

      return when (data.asString()) {
        "top" -> Top
        "bottom" -> Bottom
        "left" -> Left
        "right" -> Right
        else -> error("attempted to deserialize unsupported orientation: ${data.asString()}")
      }
    }

    override fun write(json: Json?, `object`: Orientation?, knownType: Class<*>?) {
      throw UnsupportedOperationException("not implemented")
    }
  }
}
package dev.wizrad.fracture.game.world.level.loader

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.support.debugPrefix

class Loader {
  // MARK: Properties
  private val json = Json()

  // MARK: Lifecycle
  init {
    json.setSerializer(Vector2::class.java, VectorSerializer())
    json.setSerializer(Orientation::class.java, Orientation.Serializer())
  }

  fun load(): LevelData {
    val file = Gdx.files.internal("level.json")
    val level = json.fromJson(LevelData::class.java, file)
    return level
  }

  // MARK: Serializers
  private class VectorSerializer: Json.Serializer<Vector2> {
    override fun read(json: Json?, jsonData: JsonValue?, type: Class<*>?): Vector2 {
      val data = jsonData ?: error("$this cannot parse missing json value")
      return Vector2().fromString(data.asString())
    }

    override fun write(json: Json?, `object`: Vector2?, knownType: Class<*>?) {
      throw UnsupportedOperationException("not implemented")
    }

    override fun toString() = "[$debugPrefix]"
  }
}

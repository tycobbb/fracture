package dev.wizrad.fracture.game.world.components.loader

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.world.level.Wall
import java.util.*

class LevelData {
  lateinit var start: Feature
  lateinit var walls: ArrayList<Wall.Args>
  lateinit var features: ArrayList<Feature>

  open class Feature {
    lateinit var tag: String
    lateinit var center: Vector2
    lateinit var size: Vector2
    var features: ArrayList<Feature>? = null
  }
}

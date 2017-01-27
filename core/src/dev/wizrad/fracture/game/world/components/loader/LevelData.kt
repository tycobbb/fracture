package dev.wizrad.fracture.game.world.components.loader

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.world.level.Goal
import dev.wizrad.fracture.game.world.level.Platform
import dev.wizrad.fracture.game.world.level.Spikes
import dev.wizrad.fracture.game.world.level.Wall
import java.util.*

class LevelData {
  lateinit var hotspots: Hotspots
  lateinit var walls: ArrayList<Wall.Args>
  lateinit var platforms: ArrayList<Platform.Args>
  lateinit var spikes: ArrayList<Spikes.Args>

  class Hotspots {
    lateinit var start: Feature
    lateinit var goal: Goal.Args
  }

  open class Feature {
    lateinit var tag: String
    lateinit var center: Vector2
    lateinit var size: Vector2
  }
}

package dev.wizrad.fracture.game.world.level.loader

import dev.wizrad.fracture.game.world.level.Platform
import dev.wizrad.fracture.game.world.level.Wall
import java.util.*

class LevelData {
  lateinit var entryPoint: LevelEntryPoint
  lateinit var walls: ArrayList<Wall.Args>
  lateinit var platforms: ArrayList<Platform.Args>
}

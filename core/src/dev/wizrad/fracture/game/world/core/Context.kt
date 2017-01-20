package dev.wizrad.fracture.game.world.core

// MARK: Context
data class Context(
  val world: World,
  val parent: Entity? = null
)
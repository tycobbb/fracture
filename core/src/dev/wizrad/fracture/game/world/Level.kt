package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.world.core.BaseEntity
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.game.world.support.EntitySequence

class Level(
  world: World): BaseEntity(parent = null, world = world) {

  // MARK: BaseEntity
  override val name = "Level"
  override val size = Vector2(320.0f, 568.0f)
  override val center: Vector2 = size.cpy().scl(0.5f)

  // MARK: Children
  val hero = Hero(parent = this, world = world)
  val ground = Ground(parent = this, world = world)

  // MARK: Lifecycle
  override fun children(sequence: EntitySequence) =
    super.children(sequence)
      .then(ground)
      .then(hero)
}
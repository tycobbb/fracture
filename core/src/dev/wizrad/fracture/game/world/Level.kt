package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.world.core.EntityBase
import dev.wizrad.fracture.game.world.core.EntitySequence
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.game.world.hero.Hero

class Level(
  world: World): EntityBase(parent = null, w = world) {

  // MARK: EntityBase
  override val name = "Level"
  override val size = Vector2(10.0f, 17.75f)
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
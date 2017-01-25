package dev.wizrad.fracture.game.world.cycle

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntitySequence
import dev.wizrad.fracture.game.world.cycle.loader.LevelData
import dev.wizrad.fracture.game.world.cycle.loader.Loader
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.level.Level

class Cycle(
  body: Body, size: Vector2): Entity(body, size) {

  // MARK: Properties
  private val loader = Loader()
  private var levelData: LevelData? = null

  // MARK: Children
  val hero: Hero
  val level: Level

  // MARK: Initialization
  init {
    val data = loader.load()
    levelData = data
    level = Level.Factory(this).entity(Level.Args(data))

    hero = Hero.Factory(parent = this).entity()
    hero.moveTo(transform(data.hotspots.start.center))
  }

  // MARK: Lifecycle
  override fun children(sequence: EntitySequence): EntitySequence {
    return super.children(sequence)
      .then(hero)
      .then(level)
  }

  // MARK: Factory
  class Factory: Entity.UnitFactory<Cycle>(null) {
    private val size: Vector2 = Vector2(9.0f, 16.0f)

    // MARK: Output
    override fun entity(args: Unit) = Cycle(body(), size)

    // MARK: Body
    override fun defineBody(args: Unit): BodyDef {
      val bodyDef = super.defineBody(args)
      bodyDef.type = BodyDef.BodyType.StaticBody
      bodyDef.position.set(size).scl(0.5f)
      return bodyDef
    }
  }
}
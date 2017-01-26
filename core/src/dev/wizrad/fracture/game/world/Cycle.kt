package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import dev.wizrad.fracture.game.world.components.loader.LevelData
import dev.wizrad.fracture.game.world.components.session.Event
import dev.wizrad.fracture.game.world.components.session.toEvent
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntitySequence
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.level.Level

class Cycle(
  body: Body, size: Vector2): Entity(body, size) {

  // MARK: Children
  val hero: Hero
  var level: Level; private set
  var nextLevel: Level? = null; private set

  // MARK: Initialization
  init {
    hero = Hero.Factory(parent = this).entity()
    level = buildLevel(session.loadLevel(), isNext = false)
  }

  override fun start() {
    super.start()

    subscribe(
      toEvent<Event.LevelFinished> { onLevelFinished(it) },
      toEvent<Event.TransitionFinished> { onTransitionFinished(it) }
    )

    session.startLevel(level)
  }

  // MARK: Events
  private fun onLevelFinished(event: Event.LevelFinished) {
    val data = session.loadLevel()
    val level = buildLevel(data, isNext = true)
    nextLevel = level
    session.startTransition(level)
  }

  private fun onTransitionFinished(event: Event.TransitionFinished) {
    val nextLevel = checkNotNull(nextLevel)
    level = nextLevel
    session.startLevel(level)
  }

  // MARK: Level
  private fun buildLevel(data: LevelData, isNext: Boolean): Level {
    return Level.Factory(this).entity(Level.Args(
      data = data,
      offset = if (isNext) -size.y else 0.0f
    ))
  }

  // MARK: Relationships
  override fun children(sequence: EntitySequence): EntitySequence {
    return super.children(sequence)
      .then(hero)
      .then(level)
  }

  // MARK: Factory
  class Factory: UnitFactory<Cycle>(null) {
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

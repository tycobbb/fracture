package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import dev.wizrad.fracture.game.world.components.loader.LevelData
import dev.wizrad.fracture.game.world.components.session.Event
import dev.wizrad.fracture.game.world.components.session.toEvent
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntitySequence
import dev.wizrad.fracture.game.world.core.Scene
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.level.Level

class Cycle(
  body: Body, size: Vector2): Entity(body, size) {

  // MARK: Properties
  private var offset = 0.0f

  // MARK: Children
  val hero: Hero
  var currentLevel: Level; private set
  var nextLevel: Level? = null; private set

  // MARK: Initialization
  init {
    hero = Hero.entity(parent = this)
    currentLevel = buildLevel(session.loadLevel())
  }

  override fun start() {
    super.start()

    subscribe(
      toEvent<Event.LevelFinished> { onLevelFinished(it) },
      toEvent<Event.LevelFailed> { onLevelFailed(it) },
      toEvent<Event.TransitionFinished> { onTransitionFinished(it) }
    )

    session.startLevel(currentLevel)
  }

  // MARK: Events
  private fun onLevelFinished(event: Event.LevelFinished) {
    // keep positioning levels above the last; resetting position after transition
    // requires more effort
    offset -= size.y

    val data = session.loadLevel()
    val level = buildLevel(data, offset = offset)

    nextLevel = level
    nextLevel?.start()
    invalidateChildren()

    session.startTransition(level)
  }

  private fun onLevelFailed(event: Event.LevelFailed) {
    // reset offset
    offset = 0.0f

    // migrate to a new level
    val data = session.loadLevel()
    val level = buildLevel(data, offset = offset)

    currentLevel.destroy()
    currentLevel = level
    currentLevel.start()
    invalidateChildren()

    // start the level
    session.startLevel(level)
  }

  private fun onTransitionFinished(event: Event.TransitionFinished) {
    val level = checkNotNull(nextLevel)

    // clean up current level
    nextLevel = null
    currentLevel.destroy()

    // migrate to next level
    currentLevel = level
    invalidateChildren()

    session.startLevel(currentLevel)
  }

  // MARK: Level
  private fun buildLevel(data: LevelData, offset: Float = 0.0f): Level {
    return Level.entity(this, Level.Args(
      data = data,
      size = size,
      offset = offset
    ))
  }

  // MARK: Relationships
  override fun children(sequence: EntitySequence): EntitySequence {
    return super.children(sequence)
      .then(hero)
      .then(currentLevel)
      .then(nextLevel)
  }

  // MARK: Factory
  companion object: Entity.UnitFactory<Cycle>() {
    val size: Vector2 = Vector2(11.25f, 20.0f)

    override fun entity(parent: Entity?, args: Unit)
      = Cycle(body(), size)

    private fun body(): Body {
      val scene = checkNotNull(Scene.current) { "tried to create Cycle before scene was set" }

      val bodyDef = BodyDef()
      bodyDef.type = BodyDef.BodyType.StaticBody
      bodyDef.position.set(size).scl(0.5f)

      return scene.world.createBody(bodyDef)
    }
  }
}

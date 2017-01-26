package dev.wizrad.fracture.game.renderer.core

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import dev.wizrad.fracture.game.components.projection.Projection
import dev.wizrad.fracture.game.components.projection.Projections
import dev.wizrad.fracture.game.components.projection.then
import dev.wizrad.fracture.game.core.Renderable
import dev.wizrad.fracture.game.support.Animation
import dev.wizrad.fracture.game.world.MainScene
import dev.wizrad.fracture.game.world.components.session.Event
import dev.wizrad.fracture.game.world.components.session.toEvent
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.support.extensions.Vector2
import dev.wizrad.fracture.support.extensions.set

class Camera(
  scene: MainScene): OrthographicCamera(), Renderable {

  private val viewport: Viewport = ScreenViewport(this)
  private var animation: Animation<Vector2>? = null
  private var needsUpdate: Boolean = false

  init {
    setToOrtho(true)

    // FIXME: this leaks
    scene.session.subscribe(arrayOf(
      toEvent<Event.LevelStarted> { onLevelStarted(it) },
      toEvent<Event.TransitionStarted> { onTransitionStarted(it) }
    ))
  }

  // MARK: Updatable
  override fun update(delta: Float) {
    if (animation?.isFinished ?: false) {
      animation = null
    }

    val animation = animation
    if (animation != null) {
      position.set(animation.next(delta))
      needsUpdate = true
    }

    if (needsUpdate) {
      update()
      needsUpdate = false
    }
  }

  // MARK: Events
  private fun onLevelStarted(event: Event.LevelStarted) {
    position.set(calculateCenter(event.level))
  }

  private fun onTransitionStarted(event: Event.TransitionStarted) {
    val end = calculateCenter(event.level)
    animation = Animation.Vector(
      start = position,
      end = end,
      duration = 2.0f,
      interpolation = Interpolation.exp10
    )
  }

  private fun calculateCenter(entity: Entity): Vector2 {
    return scratch
      .set(entity.center)
      .add(entity.size.x / 2, entity.size.y / 2)
      .scl(scale)
  }

  // MARK: Rendereable
  override fun resize(width: Int, height: Int) {
    position.set(width, height)
    viewport.update(width, height)
    resizeProjections(width, height)
  }

  private fun resizeProjections(width: Int, height: Int) {
    val p = position
    val o = Vector2(width / 2, height / 2)

    val offset = Projection(
      normalizer = { it.add(p.x - o.x, p.y - o.y) },
      denormalizer = { it.sub(p.x - o.x, p.y - o.y) }
    )

    Projections.viewport = offset then Projections.world
  }

  companion object {
    val scale = Vector2(41.66f, 41.66f)
    val scratch = Vector2()
  }
}

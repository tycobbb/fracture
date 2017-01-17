package dev.wizrad.fracture.game.renderer.core

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import dev.wizrad.fracture.game.core.Renderable
import dev.wizrad.fracture.game.components.projection.Projection
import dev.wizrad.fracture.game.components.projection.Projections
import dev.wizrad.fracture.game.components.projection.then

class Camera: OrthographicCamera(), Renderable {
  // MARK: Properties
  private val viewport: Viewport = ScreenViewport(this)

  // MARK: Lifecycle
  init {
    setToOrtho(true)
  }

  override fun update(delta: Float) {
    update()
  }

  override fun resize(width: Int, height: Int) {
    viewport.update(width, height)
    resizeProjections(width, height)
  }

  private fun resizeProjections(width: Int, height: Int) {
    val p = position
    val o = Vector2(width / 2.0f, height/ 2.0f)

    val offset = Projection(
      normalizer = { it.add(p.x - o.x, p.y - o.y) },
      denormalizer = { it.sub(p.x - o.x, p.y - o.y) }
    )

    Projections.viewport = offset then Projections.world
  }
}

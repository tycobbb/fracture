package dev.wizrad.fracture.game

import com.badlogic.gdx.Screen
import dev.wizrad.fracture.game.components.projection.Projection
import dev.wizrad.fracture.game.components.projection.Projections
import dev.wizrad.fracture.game.components.projection.then
import dev.wizrad.fracture.game.renderer.core.Camera
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.ui.core.MainStage
import dev.wizrad.fracture.game.world.core.EntityWorld
import dev.wizrad.fracture.support.extensions.Vector2

class MainScreen: Screen {
  private val world: EntityWorld = EntityWorld()
  private val stage: MainStage = MainStage()
  private val renderer: Renderer

  init {
    renderer = Renderer(world, camera = Camera())
  }

  // MARK: Screen
  override fun render(delta: Float) {
    world.update(delta)
    renderer.update(delta)
    stage.update(delta)
  }

  override fun resize(width: Int, height: Int) {
    val scale = Projection.scaling(Vector2(width, height))
    val reflect = Projection.reflecting(y = height)

    Projections.touch = scale
    Projections.screen = reflect then scale

    renderer.resize(width, height)
    stage.resize(width, height)
  }

  override fun show() {
  }

  override fun hide() {
  }

  override fun pause() {
  }

  override fun resume() {
  }

  override fun dispose() {
  }
}

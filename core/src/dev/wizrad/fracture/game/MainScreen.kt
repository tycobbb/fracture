package dev.wizrad.fracture.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import dev.wizrad.fracture.game.components.projection.Projection
import dev.wizrad.fracture.game.components.projection.Projections
import dev.wizrad.fracture.game.components.projection.then
import dev.wizrad.fracture.game.renderer.core.Camera
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.ui.core.MainStage
import dev.wizrad.fracture.game.world.MainScene
import dev.wizrad.fracture.support.extensions.Vector2

class MainScreen: Screen {
  private val scene = MainScene()
  private val renderer = Renderer(scene, camera = Camera())
  private val stage = MainStage(scene)

  init {
    Gdx.input.inputProcessor = InputMultiplexer(stage, scene.controls.touch)
  }

  // MARK: Screen
  override fun render(delta: Float) {
    scene.update(delta)
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

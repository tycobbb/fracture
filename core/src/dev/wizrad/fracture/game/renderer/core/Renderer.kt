package dev.wizrad.fracture.game.renderer.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import dev.wizrad.fracture.game.core.Renderable
import dev.wizrad.fracture.game.renderer.render
import dev.wizrad.fracture.game.world.core.World

class Renderer constructor(
  val world: World,
  val camera: Camera): Renderable {

  // MARK: Renderers
  val batch = SpriteBatch()
  val shaper = ShapeRenderer()

  // MARK: Lifecycle
  init {
  }

  override fun update(delta: Float) {
    // render background color to prevent flickering
    Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // reposition the camera
    camera.update(delta)
    batch.projectionMatrix = camera.combined
    shaper.projectionMatrix = camera.combined

    // render the world
    batch.begin()
    render(world, delta)
    batch.end()
  }

  override fun resize(width: Int, height: Int) {
    camera.resize(width, height)
  }
}

package dev.wizrad.fracture.game.renderer.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import dev.wizrad.fracture.game.core.Renderable
import dev.wizrad.fracture.game.renderer.render
import dev.wizrad.fracture.game.world.MainScene

class Renderer constructor(
  val scene: MainScene,
  val camera: Camera = Camera(scene)): Renderable {

  // MARK: Renderers
  val batch = SpriteBatch()
  val shaper = ShapeRenderer()
  val debugr = Box2DDebugRenderer()

  // MARK: Properties
  private val debugEnabled = true

  // MARK: Lifecycle
  override fun update(delta: Float) {
    // render background color to prevent flickering
    Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    camera.update(delta)
    batch.projectionMatrix = camera.combined
    shaper.projectionMatrix = camera.combined

    // render the scene
    batch.begin()
    render(scene, delta)
    render(debugr)
    batch.end()
  }

  override fun resize(width: Int, height: Int) {
    camera.resize(width, height)
  }

  private fun render(debugRenderer: Box2DDebugRenderer) {
    if (!debugEnabled) {
      return
    }

    val debugMatrix = camera.combined.cpy()
    debugMatrix.scale(Camera.scale.x, Camera.scale.y, 1.0f)
    debugRenderer.render(scene.world, debugMatrix)
  }

  // MARK: Scale
  fun scale(vector: Vector2, scratch: Vector2): Vector2 {
    return scratch.set(vector).scl(Camera.scale)
  }

  companion object {
    val scratch1 = Vector2(0.0f, 0.0f)
    val scratch2 = Vector2(0.0f, 0.0f)
  }
}

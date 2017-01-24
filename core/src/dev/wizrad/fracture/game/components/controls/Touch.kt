package dev.wizrad.fracture.game.components.controls

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.components.projection.Projections
import dev.wizrad.fracture.game.components.projection.project

class Touch: InputProcessor {
  // MARK: Properties
  val location = Vector2()
  var isActive = false; private set

  // MARK: Updatable
  private fun setLocation(screenX: Int, screenY: Int) {
    val point = scratch.set(screenX.toFloat(), screenY.toFloat())
    val projected = project(point, from = Projections.touch, to = Projections.world)
    location.set(projected)
  }

  // MARK: InputProcessor
  override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
    isActive = true
    setLocation(screenX, screenY)
    return true
  }

  override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
    setLocation(screenX, screenY)
    return true
  }

  override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
    isActive = false
    setLocation(screenX, screenY)
    return true
  }

  override fun keyDown(keycode: Int) = false
  override fun keyTyped(character: Char) = false
  override fun keyUp(keycode: Int) = false
  override fun mouseMoved(screenX: Int, screenY: Int) = false
  override fun scrolled(amount: Int) = false

  // MARK: Companion
  companion object {
    val scratch = Vector2()
  }
}

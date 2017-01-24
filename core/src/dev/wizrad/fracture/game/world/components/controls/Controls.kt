package dev.wizrad.fracture.game.world.components.controls

import com.badlogic.gdx.Input
import dev.wizrad.fracture.game.core.Updatable

class Controls: Updatable {
  // MARK: Properties
  val jump = Key(code = Input.Keys.SPACE)
  val left = Key(code = Input.Keys.A)
  val right = Key(code = Input.Keys.D)
  val touch = Touch()

  // MARK: Updatable
  override fun update(delta: Float) {
    jump.update(delta)
    left.update(delta)
    right.update(delta)
  }
}

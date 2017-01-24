package dev.wizrad.fracture.game.components.controls

import com.badlogic.gdx.Gdx
import dev.wizrad.fracture.game.core.Updatable

class Key(val code: Int): Updatable {
  // MARK: Properties
  private var count = 0
  private var mark = 0

  /** Indicates that the key is currently pressed */
  var isPressed: Boolean = false; private set
  /** Indicates that the key is pressed this frame */
  val isJustPressed: Boolean get() = Gdx.input.isKeyJustPressed(code)
  /** Indicates that the key is currently press, and it is a new press since the last requireUniquePress */
  val isPressedUnique: Boolean get() = isPressed && count != mark

  // MARK: Uniqueness
  fun requireUniquePress() {
    mark = count
  }

  // MARK: Updatable
  override fun update(delta: Float) {
    val isNowPressed = Gdx.input.isKeyPressed(code)

    if (isNowPressed != isPressed) {
      isPressed = isNowPressed
      if (isNowPressed) {
        count++
      }
    }
  }
}

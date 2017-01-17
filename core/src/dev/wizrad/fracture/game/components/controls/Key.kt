package dev.wizrad.fracture.game.components.controls

import com.badlogic.gdx.Input

enum class Key {
  Left,
  Right,
  Jump;

  companion object  {
    fun map() = mapOf(
      Left to Input.Keys.A,
      Right to Input.Keys.D,
      Jump to Input.Keys.SPACE
    )
  }
}

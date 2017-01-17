package dev.wizrad.fracture.game.components.controls

import com.badlogic.gdx.Gdx

class Controls {
  // MARK: Properties
  private val keys = Key.map()

  // MARK: Evaluation
  fun pressed(key: Key): Boolean {
    val code = checkNotNull(keys[key]) { "$this no code for $key" }
    val result = Gdx.input.isKeyPressed(code)
    return result
  }

  // MARK: Debugging
  override fun toString(): String {
    return "[Controls]"
  }
}
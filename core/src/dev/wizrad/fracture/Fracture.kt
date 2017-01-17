package dev.wizrad.fracture

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d.Box2D
import dev.wizrad.fracture.game.MainScreen
import dev.wizrad.fracture.support.Logging

class Fracture: Game() {
  // MARK: Game
  override fun create() {
    Gdx.app.logLevel = Logging.level
    // bootstrap box2d
    Box2D.init()
    // transition to initial screen
    setScreen(MainScreen())
  }

  // MARK: Debugging
  override fun toString(): String {
    return "[fracture]"
  }
}
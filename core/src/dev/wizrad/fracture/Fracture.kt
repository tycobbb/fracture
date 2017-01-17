package dev.wizrad.fracture

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d.Box2D
import dev.wizrad.fracture.game.MainScreen

class Fracture: Game() {
  // MARK: Game
  override fun create() {
    Gdx.app.logLevel = Application.LOG_DEBUG
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
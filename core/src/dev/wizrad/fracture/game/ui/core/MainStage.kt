package dev.wizrad.fracture.game.ui.core

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport

class MainStage: Stage(ScreenViewport()) {
  fun update(delta: Float) {
    act(delta)
    draw()
  }

  fun resize(width: Int, height: Int) {
    viewport.update(width, height, true)
  }
}

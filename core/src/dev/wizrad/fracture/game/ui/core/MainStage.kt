package dev.wizrad.fracture.game.ui.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.viewport.ScreenViewport

class MainStage: Stage(ScreenViewport()) {
  init {
    val font = BitmapFont()
    val skin = Skin(Gdx.files.internal("uiskin.json"))
    val button = TextButton("Foo", skin)
    addActor(button)
  }

  fun update(delta: Float) {
    act(delta)
    draw()
  }

  fun resize(width: Int, height: Int) {
    viewport.update(width, height, true)
  }
}

package dev.wizrad.fracture.game.ui.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.viewport.ScreenViewport
import dev.wizrad.fracture.game.ui.support.onChange
import dev.wizrad.fracture.game.world.EntityWorld
import dev.wizrad.fracture.game.world.hero.Hero

class MainStage(
  private val world: EntityWorld): Stage(ScreenViewport()) {

  // MARK: Properties
  private val skin = Skin(Gdx.files.internal("uiskin.json"))
  private val formButton = TextButton("form", skin)

  // MARK: Lifecycle
  init {
    Gdx.input.inputProcessor = this
    attachFormButton()
  }

  fun update(delta: Float) {
    act(delta)
    draw()
  }

  fun resize(width: Int, height: Int) {
    viewport.update(width, height, true)
  }

  // MARK: Form Button
  private fun attachFormButton() {
    formButton.setPosition((width - formButton.width) / 2, formButton.y)
    addActor(formButton)

    val model = world.level.hero
    updateFormButtonText(model)

    formButton.onChange { event, actor ->
      model.selectForm()
      updateFormButtonText(model)
    }
  }

  private fun updateFormButtonText(model: Hero) {
    formButton.setText(model.form.javaClass.simpleName)
  }
}

package dev.wizrad.fracture.game.ui.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import dev.wizrad.fracture.game.ui.support.onChange
import dev.wizrad.fracture.game.world.MainScene
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.PhasingForm
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class MainStage(
  private val scene: MainScene): Stage(ScreenViewport()) {

  // MARK: Properties
  private val skin = Skin(Gdx.files.internal("uiskin.json"))
  private val formButton = TextButton("form", skin)
  private val formConfigButton = TextButton("(#)", skin)
  private val phasesLeftLabel = Label("phases", skin)

  // MARK: Lifecycle
  init {
    addFormButton()
    addFormConfigButton()
    addPhasesLeftLabel()
  }

  fun update(delta: Float) {
    updatePhasesLeftLabel()
    act(delta)
    draw()
  }

  fun resize(width: Int, height: Int) {
    viewport.update(width, height, true)
  }

  // MARK: Form
  private fun addFormButton() {
    formButton.setPosition((width - formButton.width) / 2, formButton.y)
    addActor(formButton)

    val model = scene.cycle.hero
    updateFormButtonText(model)

    formButton.onChange { event, actor ->
      model.randomizeForm()
      updateFormButtonText(model)
    }
  }

  private fun addFormConfigButton() {
    formConfigButton.setPosition(width - formConfigButton.width, height - formConfigButton.height)
    addActor(formConfigButton)

    val model = scene.cycle.hero.form.state
    formConfigButton.onChange { event, actor ->
      Dialog("")
      debug(Tag.Interface, "config")
    }
  }

  private fun updateFormButtonText(model: Hero) {
    formButton.setText(model.form.javaClass.simpleName)
  }

  // MARK: PhasesLeft
  private fun addPhasesLeftLabel() {
    phasesLeftLabel.setPosition(10.0f, height - 10.0f, Align.topLeft)
    addActor(phasesLeftLabel)
  }

  private fun updatePhasesLeftLabel() {
    val model = scene.cycle.hero.form.state

    if (model is PhasingForm.PhasingState) {
      phasesLeftLabel.isVisible = true
      phasesLeftLabel.setText("Phases: ${model.phasesLeft}")
    } else {
      phasesLeftLabel.isVisible = false
    }
  }
}

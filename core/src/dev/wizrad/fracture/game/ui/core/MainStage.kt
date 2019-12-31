package dev.wizrad.fracture.game.ui.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import dev.wizrad.fracture.game.ui.support.onChange
import dev.wizrad.fracture.game.world.MainScene
import dev.wizrad.fracture.game.world.hero.forms.PhasingForm

class MainStage(
  private val scene: MainScene): Stage(ScreenViewport()) {

  // MARK: Properties
  private val skin = Skin(Gdx.files.internal("uiskin.json"))
  private val formButton = TextButton("form", skin)
  private val debugButton = TextButton("Debug", skin)
  private val phasesLeftLabel = Label("phases", skin)

  // MARK: Lifecycle
  init {
    addDebugButton()
    addFormButton()
    addPhasesLeftLabel()
  }

  fun update(delta: Float) {
    updateFormLabel()
    updatePhasesLeftLabel()
    act(delta)
    draw()
  }

  fun resize(width: Int, height: Int) {
    viewport.update(width, height, true)
  }


  // MARK: Debug
  private fun addDebugButton() {
    debugButton.width = 50.0f
    debugButton.setPosition(width * 0.75f - debugButton.width / 2, debugButton.y)
    addActor(debugButton)

    val model = scene.cycle.hero
    debugButton.onChange { event, actor ->
      model.setDebugForm()
    }
  }

  // MARK: Form
  private fun addFormButton() {
    formButton.width = 50.0f
    formButton.setPosition(width * 0.25f - formButton.width / 2, formButton.y)
    addActor(formButton)

    val model = scene.cycle.hero
    formButton.onChange { event, actor ->
      model.randomizeForm()
    }

    updateFormLabel()
  }

  private fun updateFormLabel() {
    val model = scene.cycle.hero
    val title = model.form.javaClass.simpleName
    formButton.setText(title)
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

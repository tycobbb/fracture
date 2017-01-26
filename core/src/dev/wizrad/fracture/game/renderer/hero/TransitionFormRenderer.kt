package dev.wizrad.fracture.game.renderer.hero

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.TransitionForm
import dev.wizrad.fracture.support.sin

fun Renderer.render(hero: Hero, form: TransitionForm, delta: Float) {
  batch.pause {
    drawRect(hero, heroColor(form.state, delta))
  }
}

private fun heroColor(state: State, delta: Float): Color {
  if (state !is TransitionForm.Transitioning) {
    return Color.SCARLET
  }

  val n = 255
  val f = 5.0f / n
  val i = (state.translation.elapsed / state.translation.duration * n).toInt()

  val r = (sin(f * i + 0) * 127 + 128) / n
  val g = (sin(f * i + 1) * 127 + 128) / n
  val b = (sin(f * i + 3) * 127 + 128) / n

  return Color(r, g, b, 1.0f)
}

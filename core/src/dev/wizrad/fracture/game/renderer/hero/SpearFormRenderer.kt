package dev.wizrad.fracture.game.renderer.hero

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.SpearForm

fun Renderer.render(hero: Hero, form: SpearForm, delta: Float) {
  batch.pause {
    drawRect(hero, heroColor(form.state))
  }
}

private fun heroColor(state: State): Color {
  return when (state) {
    is SpearForm.Ready -> Color.SCARLET
    else -> Color.CORAL
  }
}

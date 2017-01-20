package dev.wizrad.fracture.game.renderer.hero

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.SpearForm

fun Renderer.render(hero: Hero, form: SpearForm, delta: Float) {
  batch.pause {
    shaper.draw {
      it.color = currentColor(form.state)
      val center = scale(hero.center, Renderer.scratch1)
      val size = scale(hero.size, Renderer.scratch2)
      it.rect(center.x, center.y, size.x, size.y)
    }
  }
}

private fun currentColor(state: State): Color {
  return when (state) {
    is SpearForm.Ready -> Color.SCARLET
    else -> Color.CORAL
  }
}

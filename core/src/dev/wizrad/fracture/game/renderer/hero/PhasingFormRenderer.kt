package dev.wizrad.fracture.game.renderer.hero

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.PhasingForm

fun Renderer.render(hero: Hero, form: PhasingForm, delta: Float) {
  batch.pause {
    shaper.draw {
      it.color = Color.BLACK
      val center = scale(hero.center, Renderer.scratch1)
      val size = scale(hero.size, Renderer.scratch2)
      it.rect(center.x - size.x / 2, center.y - size.y / 2, size.x, size.y)
    }

    val state = form.state
    when (state) {
      is PhasingForm.PhasingState -> render(state.phasingTarget, delta)
    }
  }
}

private fun Renderer.render(phaseTarget: PhasingForm.Target?, delta: Float) {
  if (phaseTarget == null) {
    return
  }

  shaper.draw {
    it.color = Color.SCARLET
    val center = scale(phaseTarget.point, Renderer.scratch1)
    val size = scale(Renderer.scratch2.set(0.25f, 0.25f), Renderer.scratch2)
    it.rect(center.x - size.x / 2, center.y - size.y / 2, size.x, size.y)
  }
}

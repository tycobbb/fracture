package dev.wizrad.fracture.game.renderer.hero

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.PhasingForm

fun Renderer.render(hero: Hero, form: PhasingForm, delta: Float) {
  batch.pause {
    Gdx.gl.glEnable(GL20.GL_BLEND)
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

    shaper.draw {
      val center = scale(hero.center, Renderer.scratch1)
      val size = scale(hero.size, Renderer.scratch2)

      it.color = getColor(form.state)
      it.rect(center.x - size.x / 2, center.y - size.y / 2, size.x, size.y)
    }

    Gdx.gl.glDisable(GL20.GL_BLEND)

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

private fun getColor(state: State): Color {
  return when (state) {
    is PhasingForm.Phasing, is PhasingForm.PhasingEnd -> Color(0.0f, 0.0f, 0.0f, 0.5f)
    else -> Color.BLACK
  }
}

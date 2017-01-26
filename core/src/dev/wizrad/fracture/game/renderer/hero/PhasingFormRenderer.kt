package dev.wizrad.fracture.game.renderer.hero

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.PhasingForm

fun Renderer.render(hero: Hero, form: PhasingForm, delta: Float) {
  batch.pause {
    Gdx.gl.glEnable(GL20.GL_BLEND)
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    drawRect(hero, heroColor(form.state))
    Gdx.gl.glDisable(GL20.GL_BLEND)

    val state = form.state
    when (state) {
      is PhasingForm.PhasingState -> render(state.phasingTarget, delta)
    }
  }
}

private fun Renderer.render(target: PhasingForm.Target?, delta: Float) {
  if (target != null) {
    drawRect(target.point, Renderer.scratch1.set(0.25f, 0.25f), 0.0f, Color.SCARLET)
  }
}

private fun heroColor(state: State): Color {
  return when (state) {
    is PhasingForm.Phasing, is PhasingForm.PhasingEnd -> Color(0.0f, 0.0f, 0.0f, 0.5f)
    else -> Color.BLACK
  }
}

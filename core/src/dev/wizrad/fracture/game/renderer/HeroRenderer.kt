package dev.wizrad.fracture.game.renderer

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.Form

fun Renderer.render(hero: Hero, delta: Float) {
  batch.pause {
    shaper.draw {
      it.color = getColor(hero)

      val center = scale(hero.center, Renderer.scratch1)
      val size = scale(hero.size, Renderer.scratch2)
      it.rect(center.x, center.y, size.x, size.y)
    }
  }
}

private fun getColor(hero: Hero): Color {
  return when (hero.form.type) {
    Form.Type.SingleJump -> Color.WHITE
    Form.Type.SpaceJump -> Color.FIREBRICK
    Form.Type.Rebound -> Color.CHARTREUSE
    Form.Type.Spear -> Color.CORAL
  }
}

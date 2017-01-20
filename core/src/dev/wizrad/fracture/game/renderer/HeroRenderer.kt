package dev.wizrad.fracture.game.renderer

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.ReboundForm
import dev.wizrad.fracture.game.world.hero.forms.SingleJumpForm
import dev.wizrad.fracture.game.world.hero.forms.SpaceJumpForm
import dev.wizrad.fracture.game.world.hero.forms.SpearForm

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
  val form = hero.form
  return when (form) {
    is SingleJumpForm -> Color.WHITE
    is SpaceJumpForm -> Color.FIREBRICK
    is ReboundForm -> Color.CHARTREUSE
    is SpearForm -> Color.CORAL
    else -> error("attempted to render unknown form: $form")
  }
}

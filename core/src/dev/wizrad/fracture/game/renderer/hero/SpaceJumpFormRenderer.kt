package dev.wizrad.fracture.game.renderer.hero

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.SpaceJumpForm

fun Renderer.render(hero: Hero, form: SpaceJumpForm, delta: Float) {
  batch.pause {
    shaper.draw {
      it.color = Color.CHARTREUSE
      val center = scale(hero.center, Renderer.scratch1)
      val size = scale(hero.size, Renderer.scratch2)
      it.rect(center.x, center.y, size.x, size.y)
    }
  }
}
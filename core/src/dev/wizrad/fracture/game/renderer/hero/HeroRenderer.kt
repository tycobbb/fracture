package dev.wizrad.fracture.game.renderer.hero

import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.ReboundForm
import dev.wizrad.fracture.game.world.hero.forms.SingleJumpForm
import dev.wizrad.fracture.game.world.hero.forms.SpaceJumpForm
import dev.wizrad.fracture.game.world.hero.forms.SpearForm

fun Renderer.render(hero: Hero, delta: Float) {
  val form = hero.form

  when (form) {
    is SingleJumpForm -> render(hero, form, delta)
    is SpaceJumpForm -> render(hero, form, delta)
    is ReboundForm -> render(hero, form, delta)
    is SpearForm -> render(hero, form, delta)
    else -> error("attempted to render unknown form: $form")
  }
}

package dev.wizrad.fracture.game.renderer.hero

import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.*

fun Renderer.render(hero: Hero, delta: Float) {
  val form = hero.form

  when (form) {
    is VanillaForm -> render(hero, form, delta)
    is SpaceJumpForm -> render(hero, form, delta)
    is ReboundForm -> render(hero, form, delta)
    is SpearForm -> render(hero, form, delta)
    is PhasingForm -> render(hero, form, delta)
    is AirDashForm -> render(hero, form, delta)
    is FluidForm -> render(hero, form, delta)
    else -> error("attempted to render unknown form: $form")
  }
}

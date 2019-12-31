package dev.wizrad.fracture.game.world.hero.core

import dev.wizrad.fracture.game.world.hero.Hero

interface FormContext {
  val hero: Hero

  companion object {
    fun wrap(hero: Hero): FormContext {
      return object: FormContext {
        override val hero = hero
      }
    }
  }
}

package dev.wizrad.fracture

import dev.wizrad.fracture.game.world.hero.Hero
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.jetbrains.spek.api.Spek

class HeroSpec: Spek({
  var hero: Hero? = null

  beforeEach {
    hero = Hero()
  }

  describe("a hero") {
    it("exists") {
      assertThat(hero, `is`(notNullValue()))
    }
  }
})

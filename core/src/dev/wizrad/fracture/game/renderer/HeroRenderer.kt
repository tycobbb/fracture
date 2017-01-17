package dev.wizrad.fracture.game.renderer

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.Hero
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

fun Renderer.render(hero: Hero, delta: Float) {
  debug(Tag.Rendering, "rendering hero $hero")
  batch.pause {
    shaper.draw {
      it.color = Color(0xFFFFFF)
      it.rect(hero.center.x, hero.center.y, hero.size.x, hero.size.y)
    }
  }
}

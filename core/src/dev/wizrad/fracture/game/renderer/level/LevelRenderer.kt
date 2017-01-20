package dev.wizrad.fracture.game.renderer.level

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.hero.render
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.level.Level

fun Renderer.render(level: Level, delta: Float) {
  batch.pause {
    shaper.draw {
      it.color = Color(0x7F89AE)

      val center = scale(level.center, Renderer.scratch1)
      val size = scale(level.size, Renderer.scratch2)
      it.rect(center.x, center.y, size.x, size.y)
    }
  }

  render(level.ground, delta)
  render(level.hero, delta)
}

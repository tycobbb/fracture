package dev.wizrad.fracture.game.renderer

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.Level

fun Renderer.render(level: Level, delta: Float) {
  batch.pause {
    shaper.draw {
      it.color = Color(0x7F89AE)
      it.rect(0.0f, 0.0f, level.size.x, level.size.y)
    }
  }

  render(level.hero, delta)
}

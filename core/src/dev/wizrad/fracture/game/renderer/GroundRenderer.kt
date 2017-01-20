package dev.wizrad.fracture.game.renderer

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.level.Ground

fun Renderer.render(ground: Ground, delta: Float) {
  batch.pause {
    shaper.draw {
      it.color = Color(0x596D85)
      val center = scale(ground.center, Renderer.scratch1)
      val size = scale(ground.size, Renderer.scratch2)
      it.rect(center.x, center.y, size.x, size.y)
    }
  }
}

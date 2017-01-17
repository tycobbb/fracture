package dev.wizrad.fracture.game.renderer

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.Ground

fun Renderer.render(ground: Ground, delta: Float) {
  batch.pause {
    shaper.draw {
      it.color = Color(0x596D85)
      it.rect(ground.center.x, ground.center.y, ground.size.x, ground.size.y)
    }
  }
}

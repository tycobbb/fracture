package dev.wizrad.fracture.game.renderer

import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.world.Level

fun Renderer.render(level: Level, delta: Float) {
  render(level.hero, delta)
}

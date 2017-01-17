package dev.wizrad.fracture.game.renderer

import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.world.core.EntityWorld

fun Renderer.render(world: EntityWorld, delta: Float) {
  render(world.level, delta)
}

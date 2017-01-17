package dev.wizrad.fracture.game.renderer

import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.world.core.World

fun Renderer.render(world: World, delta: Float) {
  render(world.level, delta)
}

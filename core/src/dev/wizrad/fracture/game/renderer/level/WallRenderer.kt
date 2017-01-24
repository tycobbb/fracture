package dev.wizrad.fracture.game.renderer.level

import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.world.level.Wall

fun Renderer.render(wall: Wall, delta: Float) {
}

fun Renderer.render(walls: Iterable<Wall>, delta: Float) {
  walls.forEach { render(it, delta) }
}

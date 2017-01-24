package dev.wizrad.fracture.game.renderer.level

import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.world.level.Platform

fun Renderer.render(platform: Platform, delta: Float) {
}

fun Renderer.render(walls: Iterable<Platform>, delta: Float) {
  walls.forEach { render(it, delta) }
}

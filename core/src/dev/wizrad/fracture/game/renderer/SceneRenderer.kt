package dev.wizrad.fracture.game.renderer

import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.hero.render
import dev.wizrad.fracture.game.renderer.level.render
import dev.wizrad.fracture.game.world.MainScene

fun Renderer.render(world: MainScene, delta: Float) {
  render(world.cycle.currentLevel, delta)
  render(world.cycle.hero, delta)
}

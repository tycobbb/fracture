package dev.wizrad.fracture.game.renderer.hero

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.draw
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.*
import dev.wizrad.fracture.support.Maths

fun Renderer.render(hero: Hero, delta: Float) {
  val form = hero.form

  when (form) {
    is VanillaForm -> render(hero, form, delta)
    is SpaceJumpForm -> render(hero, form, delta)
    is ReboundForm -> render(hero, form, delta)
    is SpearForm -> render(hero, form, delta)
    is PhasingForm -> render(hero, form, delta)
    is AirDashForm -> render(hero, form, delta)
    is FluidForm -> render(hero, form, delta)
    is FlutterForm -> render(hero, form, delta)
    is TransitionForm -> render(hero, form, delta)
    is DebugForm -> render(hero, form, delta)
    else -> error("attempted to render unknown form: $form")
  }
}

fun Renderer.drawRect(hero: Hero, color: Color) {
  drawRect(hero.center, hero.size, hero.body.angle, color)
}

fun Renderer.drawRect(worldCenter: Vector2, worldSize: Vector2, angle: Float, color: Color) {
  val center = scale(worldCenter, Renderer.scratch1)
  val size = scale(worldSize, Renderer.scratch2)
  val width = size.x / 2
  val height = size.y / 2

  shaper.draw {
    it.color = color
    it.rect(center.x - width, center.y - height, width, height, size.x, size.y, 1.0f, 1.0f, angle * Maths.RAD2DEG)
  }
}

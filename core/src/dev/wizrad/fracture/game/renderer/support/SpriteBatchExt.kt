package dev.wizrad.fracture.game.renderer.support

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.support.Maths

inline fun SpriteBatch.with(tint: Color, crossinline action: () -> Unit) {
  val previous = color

  color = tint
  action()
  color = previous
}

inline fun SpriteBatch.pause(crossinline action: () -> Unit) {
  end()
  action()
  begin()
}

fun SpriteBatch.drawc(region: TextureRegion, center: Vector2, radius: Float) {
  val x  = center.x - radius
  val y  = center.y - radius

  draw(region, x, y, radius, radius)
}

fun SpriteBatch.drawr(region: TextureRegion, center: Vector2, angle: Float) {
  val width  = region.regionWidth.toFloat()
  val height = region.regionHeight.toFloat()

  val x  = center.x - width
  val y  = center.y - height
  val ox = width  / 2
  val oy = height / 2

  val rotation = (angle + Maths.F_PI_2) * Maths.RAD2DEG

  draw(region, x, y, ox, oy, width, height, 1.0f, 1.0f, rotation)
}

package dev.wizrad.fracture.game.renderer.support

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2

fun ShapeRenderer.rect(size: Vector2) {
  rect(0.0f, 0.0f, size.x, size.y)
}

fun ShapeRenderer.circle(center: Vector2, radius: Float) {
  circle(center.x, center.y, radius)
}

inline fun ShapeRenderer.draw(type: ShapeType = ShapeType.Filled, crossinline closure: (ShapeRenderer) -> Unit) {
  begin(type)
  closure(this)
  end()
}

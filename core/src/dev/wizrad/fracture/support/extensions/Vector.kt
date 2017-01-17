package dev.wizrad.fracture.support.extensions

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.support.atan2

// MARK: Constructors
fun Vector2(width: Int, height: Int): Vector2 {
  return Vector2(width.toFloat(), height.toFloat())
}

// MARK: Operations
fun Vector2.update(x: Float = this.x, y: Float = this.y): Vector2 {
  this.x = x
  this.y = y
  return this
}

fun Vector2.invert(): Vector2 {
  x = 1.0f / x
  y = 1.0f / y
  return this
}

// MARK: Geometry
fun Vector2.angleTo(other: Vector2): Float {
  return atan2(other.y - y, other.x - x)
}

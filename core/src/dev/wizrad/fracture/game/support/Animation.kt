package dev.wizrad.fracture.game.support

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dev.wizrad.fracture.support.extensions.min
import dev.wizrad.fracture.support.extensions.set
import dev.wizrad.fracture.support.lerp

sealed class Animation<T>(
  /** The initial position of the animation; copied from constructor parameter */
  val start: T,
  /** The final position of the animation; copied from contructor parameter */
  val end: T,
  /** The total time this animation should run */
  val duration: Float,
  /** The interpolation to apply to alpha values; defaults to linear */
  val interpolation: Interpolation) {

  // MARK: Properties
  /** The current time for this animation */
  var elapsed: Float = 0.0f; private set
  /** Flag indiciatin if this animation is finished */
  val isFinished: Boolean get() = elapsed >= duration

  // MARK: Updates
  /** Computes an intermediate value given a completion percent */
  abstract protected fun interpolate(percent: Float): T

  /** Call every frame to compute the animation's intermediate value */
  fun next(delta: Float): T {
    if (!isFinished) {
      elapsed += delta
    }

    return interpolate(min(elapsed / duration, 1.0f))
  }

  // MARK: Float
  class Value(
    start: Float, end: Float, duration: Float, interpolation: Interpolation): Animation<Float>(
      start = start, end = end, duration = duration, interpolation = interpolation) {

    override fun interpolate(percent: Float): Float {
      return lerp(start, end, interpolation.apply(percent))
    }
  }

  // MARK: Vector
  class Vector(
    start: Vector2, end: Vector2, duration: Float, interpolation: Interpolation):
      Animation<Vector2>(start.cpy(), end.cpy(), duration, interpolation) {

    constructor(
      start: Vector3, end: Vector2, duration: Float, interpolation: Interpolation):
        this(scratch.set(start), end, duration, interpolation)

    override fun interpolate(percent: Float): Vector2 {
      return scratch.set(start).interpolate(end, percent, interpolation)
    }

    // MARK: Scratch
    private companion object {
      val scratch = Vector2()
    }
  }
}

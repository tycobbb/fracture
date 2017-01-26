package dev.wizrad.fracture.game.support

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dev.wizrad.fracture.support.extensions.min
import dev.wizrad.fracture.support.extensions.set

class Animation(
  start: Vector2,
  end: Vector2,
  duration: Float,
  interpolation: Interpolation = Interpolation.linear) {

  constructor(
    start: Vector3, end: Vector2, duration: Float, interpolation: Interpolation = Interpolation.linear):
    this(scratch.set(start), end, duration, interpolation)

  // MARK: Properties
  /** The initial position of the animation; copied from constructor parameter */
  val start = Vector2(start)
  /** The final position of the animation; copied from contructor parameter */
  val end = Vector2(end)
  /** The interpolation to apply to alpha values; defaults to linear */
  val interpolation = interpolation
  /** The current time for this animation */
  var elapsed: Float = 0.0f; private set
  /** The total time this animation should run */
  val duration = duration
  /** Flag indiciatin if this animation is finished */
  val isFinished: Boolean get() = elapsed >= duration

  // MARK: Updates
  /** Call every frame to compute the animation's intermediate value */
  fun next(delta: Float): Vector2 {
    if (!isFinished) {
      elapsed += delta
    }

    val percent = min(elapsed / duration, 1.0f)
    val point = scratch.set(start).interpolate(end, percent, interpolation)

    return point
  }

  // MARK: Scratch
  private companion object {
    val scratch = Vector2()
  }
}

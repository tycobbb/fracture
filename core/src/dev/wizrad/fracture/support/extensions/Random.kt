package dev.wizrad.fracture.support.extensions

import com.badlogic.gdx.math.MathUtils
import java.util.*

// surface companion at top level for prettiness
val rand = Rand

// wrapper around existing random functions
class Rand {
  companion object {
    // backing random instance
    private val random: Random get() = MathUtils.random

    // MARK: Upto
    fun upto(max: Int) = MathUtils.random(max)
    fun upto(max: Double): Double = random.nextDouble() * max

    // MARK: Between
    fun between(min: Int, max: Int) = min + upto(max - min)
    fun between(range: ClosedRange<Int>) = between(range.start, range.endInclusive)

    fun between(min: Double, max: Double) = min + upto(max - min)
    fun between(range: ClosedRange<Double>) = between(range.start, range.endInclusive)

    // MARK: Stride
    fun stride(min: Int, length: Int) = min + upto(length)
    fun stride(min: Double, length: Double) = min + upto(length)

    // MARK: Seed
    private var _seed: Long = -1

    var seed: Long?
      get() = _seed
      set(value) {
        _seed = value ?: Random().nextLong()
        random.setSeed(_seed)
      }
  }
}

package dev.wizrad.fracture.game.components.projection

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.support.extensions.invert

data class Projection(
  val normalizer:   (Vector2) -> Vector2,
  val denormalizer: (Vector2) -> Vector2) {

  companion object {
    fun scaling(width: Int, height: Int): Projection {
      return scaling(width.toFloat(), height.toFloat())
    }

    fun scaling(width: Float, height: Float): Projection {
      return scaling(Vector2(width, height))
    }

    fun scaling(scale: Vector2): Projection {
      val scale = scale.cpy()
      val inverse = scale.cpy().invert()

      return Projection(
        normalizer = { it.scl(inverse) },
        denormalizer = { it.scl(scale) }
      )
    }

    fun reflecting(x: Int? = null, y: Int? = null): Projection {
      return reflecting(x = x?.toFloat(), y = y?.toFloat())
    }

    fun reflecting(x: Float? = null, y: Float? = null): Projection {
      val reflector = { v: Vector2 ->
        if(x != null) { v.x = x - v.x }
        if(y != null) { v.y = y - v.y }
        v
      }

      return Projection(
        normalizer = reflector,
        denormalizer = reflector
      )
    }
  }
}

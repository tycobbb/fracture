package dev.wizrad.fracture.game.components.projection

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.support.then

fun project(point: Vector2, from: Projection, to: Projection): Vector2 {
  to.denormalizer(from.normalizer(point))
  return point
}

fun projector(from: Projection, to: Projection): (Vector2) -> Vector2 {
  return { project(it, from, to) }
}

infix fun Projection.then(other: Projection): Projection {
  return Projection(
    normalizer = normalizer then other.normalizer,
    denormalizer = other.denormalizer then denormalizer
  )
}

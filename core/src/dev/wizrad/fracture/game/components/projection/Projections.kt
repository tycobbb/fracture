package dev.wizrad.fracture.game.components.projection

class Projections {
  companion object {
    val normal = Projection(normalizer = { it }, denormalizer = { it })

    lateinit var touch: Projection
    lateinit var screen: Projection
    lateinit var world: Projection
    lateinit var viewport: Projection
  }
}

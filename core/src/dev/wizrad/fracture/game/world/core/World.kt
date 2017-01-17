package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.components.projection.Projection
import dev.wizrad.fracture.game.components.projection.Projections
import dev.wizrad.fracture.game.world.Level
import dev.wizrad.fracture.support.extensions.Vector2
import dev.wizrad.fracture.support.extensions.min
import com.badlogic.gdx.physics.box2d.World as PhysicsWorld

class World {
  // MARK: Components
  val physics = com.badlogic.gdx.physics.box2d.World(gravity, true)

  // MARK: Children
  val level = Level()

  // MARK: Properties
  /** accumulates frame time to determine when to run fixed-step physics simulation */
  private var accumulator: Float = 0.0f

  // MARK: Lifecycle
  init {
    // setup the world projection
    Projections.world = Projection.scaling(Vector2(50, 50))
  }

  fun update(delta: Float) {
    // update model layer
    level.update(delta)

    // update physics according to fixed time step
    // See: http://gafferongames.com/game-physics/fix-your-timestep/
    val frame = dev.wizrad.fracture.support.extensions.min(delta, 0.25f)
    accumulator += frame

    while(accumulator >= timestep) {
      level.step(timestep)
      physics.step(timestep, 6, 2)
      level.afterStep(timestep)

      accumulator -= timestep
    }
  }

  companion object {
    // MARK: Constants
    private val timestep = 1.0f / 60.0f
    private val gravity = Vector2(0.0f, 0.0f)
  }
}

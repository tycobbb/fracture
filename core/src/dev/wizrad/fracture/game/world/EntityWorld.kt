package dev.wizrad.fracture.game.world

import dev.wizrad.fracture.game.components.controls.Controls
import dev.wizrad.fracture.game.components.projection.Projection
import dev.wizrad.fracture.game.components.projection.Projections
import dev.wizrad.fracture.game.components.projection.then
import dev.wizrad.fracture.game.world.components.contact.ContactGraph
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.game.world.level.Level
import dev.wizrad.fracture.game.world.support.Physics
import dev.wizrad.fracture.support.extensions.min
import com.badlogic.gdx.physics.box2d.World as PhysicsWorld

class EntityWorld: World {
  // MARK: World
  override val physics = PhysicsWorld(Physics.gravity, true)
  override val controls = Controls()
  override val contact = ContactGraph()

  // MARK: Children
  val level = Level.Factory(Context(this, null)).entity()

  // MARK: Properties
  /** accumulates frame time to determine when to run fixed-step physics simulation */
  private var accumulator: Float = 0.0f

  // MARK: Lifecycle
  init {
    physics.setContactListener(contact)
    level.start()

    val offset = Projection.offset(level.center)
    val scale = Projection.scaling(level.size)
    Projections.world = offset then scale
  }

  fun update(delta: Float) {
    // run updates before physics simulation
    controls.update(delta)
    level.update(delta)

    // update physics according to fixed time step
    // See: http://gafferongames.com/game-physics/fix-your-timestep/
    val frame = min(delta, 0.25f)
    accumulator += frame

    while(accumulator >= timestep) {
      level.step(timestep)
      physics.step(timestep, 6, 2)
      accumulator -= timestep
    }
  }

  companion object {
    // MARK: Constants
    private val timestep = 1.0f / 60.0f
  }
}

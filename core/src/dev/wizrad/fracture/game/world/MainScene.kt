package dev.wizrad.fracture.game.world

import com.badlogic.gdx.physics.box2d.World
import dev.wizrad.fracture.game.components.projection.Projection
import dev.wizrad.fracture.game.components.projection.Projections
import dev.wizrad.fracture.game.components.projection.then
import dev.wizrad.fracture.game.world.components.contact.ContactGraph
import dev.wizrad.fracture.game.world.components.controls.Controls
import dev.wizrad.fracture.game.world.components.session.Session
import dev.wizrad.fracture.game.world.core.Scene
import dev.wizrad.fracture.game.world.cycle.Cycle
import dev.wizrad.fracture.game.world.support.Physics
import dev.wizrad.fracture.support.extensions.min

class MainScene: Scene {
  // MARK: Scene
  override val session = Session()
  override val world = World(Physics.gravity, true)
  override val controls = Controls()
  override val contact = ContactGraph()

  // MARK: Properties
  val cycle: Cycle
  private var accumulator: Float = 0.0f

  // MARK: Lifecycle
  init {
    Scene.current = this

    // hook up contact graph
    world.setContactListener(contact)
    world.setContactFilter( contact)

    // boostrap cycle
    cycle = Cycle.Factory().entity()
    cycle.start()

    // set the world coordinate space transform
    Projections.world = Projection.offset(cycle.center) then Projection.scaling(cycle.size)
  }

  fun update(delta: Float) {
    // 1. update controls / scene
    controls.update(delta)
    cycle.update(delta)

    // 2. update scene according to fixed time step
    // See: http://gafferongames.com/game-physics/fix-your-timestep/
    val frame = min(delta, 0.25f)
    accumulator += frame

    while(accumulator >= timestep) {
      cycle.step(timestep)
      world.step(timestep, 6, 2)
      accumulator -= timestep
    }

    // 3. give the scene a last chance to update after scene
    cycle.lateUpdate(delta)
  }

  companion object {
    private val timestep = 1.0f / 60.0f
  }
}

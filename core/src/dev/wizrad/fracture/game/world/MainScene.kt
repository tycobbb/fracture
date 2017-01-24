package dev.wizrad.fracture.game.world

import com.badlogic.gdx.physics.box2d.World
import dev.wizrad.fracture.game.components.projection.Projection
import dev.wizrad.fracture.game.components.projection.Projections
import dev.wizrad.fracture.game.components.projection.then
import dev.wizrad.fracture.game.world.components.contact.ContactGraph
import dev.wizrad.fracture.game.world.components.controls.Controls
import dev.wizrad.fracture.game.world.core.Scene
import dev.wizrad.fracture.game.world.level.Level
import dev.wizrad.fracture.game.world.support.Physics
import dev.wizrad.fracture.support.extensions.min

class MainScene: Scene {
  // MARK: Scene
  override val world = World(Physics.gravity, true)
  override val controls = Controls()
  override val contact = ContactGraph()

  // MARK: Properties
  val level: Level
  private var accumulator: Float = 0.0f

  // MARK: Lifecycle
  init {
    Scene.current = this

    // hook up contact graph
    world.setContactListener(contact)
    world.setContactFilter( contact)

    // boostrap level
    level = Level.Factory().entity()
    level.start()

    // set the world coordinate space transform
    Projections.world = Projection.offset(level.center) then Projection.scaling(level.size)
  }

  fun update(delta: Float) {
    // 1. update controls / scene
    controls.update(delta)
    level.update(delta)

    // 2. update scene according to fixed time step
    // See: http://gafferongames.com/game-physics/fix-your-timestep/
    val frame = min(delta, 0.25f)
    accumulator += frame

    while(accumulator >= timestep) {
      level.step(timestep)
      world.step(timestep, 6, 2)
      accumulator -= timestep
    }

    // 3. give the scene a last chance to update after scene
    level.lateUpdate(delta)
  }

  companion object {
    private val timestep = 1.0f / 60.0f
  }
}

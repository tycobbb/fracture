package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.physics.box2d.World
import dev.wizrad.fracture.game.world.components.contact.ContactGraph
import dev.wizrad.fracture.game.world.components.controls.Controls

/** Top-level accessor to current scene */
val scene: Scene get() = Scene.instance!!

interface SceneAware {
  /** The scene this object is aware of */
  val scene: Scene

  // MARK: Accessors
  /** A reference to the scene's shared scene */
  val world: World get() = scene.world
  /** A reference to the scene's shared contact graph */
  val contact: ContactGraph get() = scene.contact
  /** A reference to the scene's shared contact controls */
  val controls: Controls get() = scene.controls
}
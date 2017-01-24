package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.physics.box2d.World
import dev.wizrad.fracture.game.world.components.contact.ContactGraph
import dev.wizrad.fracture.game.world.components.controls.Controls

/** Interface providing reference to shared scene components */
interface Scene {
  /** The box2d scene for interfacing with the scene engine */
  val world: World
  /** The contact listener / filter used to control collision behavior */
  val contact: ContactGraph
  /** The controls used to listen for user input */
  val controls: Controls

  companion object {
    var current: Scene? = null
    val instance: Scene get() = current!!
  }
}

package dev.wizrad.fracture.game.world.core

import dev.wizrad.fracture.game.components.controls.Controls
import dev.wizrad.fracture.game.world.components.contact.Contact
import com.badlogic.gdx.physics.box2d.World as PhysicsWorld

interface World {
  val physics: PhysicsWorld
  val controls: Controls
  val contact: Contact
}

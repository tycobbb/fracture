package dev.wizrad.fracture.game.world.core

import dev.wizrad.fracture.game.components.controls.Controls
import com.badlogic.gdx.physics.box2d.World as PhysicsWorld

interface World {
  val physics: PhysicsWorld
  val controls: Controls
  val contacts: Contacts
}

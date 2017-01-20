package dev.wizrad.fracture.game.world.support

import com.badlogic.gdx.physics.box2d.Fixture
import dev.wizrad.fracture.game.world.components.contact.ContactInfo

var Fixture.contactInfo: ContactInfo?
  get() { return userData as? ContactInfo }
  set(value) { userData = value }

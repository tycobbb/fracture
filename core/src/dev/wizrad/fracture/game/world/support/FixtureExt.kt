package dev.wizrad.fracture.game.world.support

import com.badlogic.gdx.physics.box2d.Fixture
import dev.wizrad.fracture.game.world.components.contact.ContactInfo

// MARK: ContactInfo
var Fixture.contactInfo: ContactInfo?
  get() = userData as? ContactInfo
  set(value) {
    userData = value
  }

val Fixture.hero: ContactInfo.Hero?
  get() = userData as? ContactInfo.Hero

val Fixture.appendage: ContactInfo.Appendage?
  get() = userData as? ContactInfo.Appendage

val Fixture.obstruction: ContactInfo.Obstruction?
  get() = userData as? ContactInfo.Obstruction

val Fixture.barrier: ContactInfo.Barrier?
  get() = userData as? ContactInfo.Barrier

val Fixture.surface: ContactInfo.Surface?
  get() = userData as? ContactInfo.Surface

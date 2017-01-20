package dev.wizrad.fracture.game.world.components.contact

import com.badlogic.gdx.physics.box2d.*
import dev.wizrad.fracture.game.world.support.contactInfo
import dev.wizrad.fracture.support.extensions.findMapped

class ContactGraph: ContactListener {
  // MARK: Properties
  private val map = mutableMapOf<Fixture, MutableSet<Fixture>>()
  private val defaultSet = { mutableSetOf<Fixture>() }

  // MARK: Lookup
  fun first(fixture: Fixture): ContactInfo? {
    return contactSet(fixture).findMapped { it.contactInfo }
  }

  fun oriented(fixture: Fixture, orientation: ContactInfo.Orientation): Boolean {
    return contactSet(fixture)
      .find { (it.userData as? ContactInfo)?.orientation == orientation } != null
  }

  private fun contactSet(fixture: Fixture): MutableSet<Fixture> {
    return map.getOrPut(fixture, defaultSet)
  }

  // MARK: ContactListener
  override fun beginContact(contact: Contact?) {
    val contact = contact ?: return

    contactSet(contact.fixtureA).add(contact.fixtureB)
    contactSet(contact.fixtureB).add(contact.fixtureA)
  }

  override fun endContact(contact: Contact?) {
    val contact = contact ?: return

    contactSet(contact.fixtureA).remove(contact.fixtureB)
    contactSet(contact.fixtureB).remove(contact.fixtureA)
  }

  override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
  }

  override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
  }
}


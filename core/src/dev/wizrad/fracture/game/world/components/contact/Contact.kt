package dev.wizrad.fracture.game.world.components.contact

import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.Contact


class Contact : ContactListener {
  // MARK: Properties
  private val map = mutableMapOf<Fixture, MutableSet<Fixture>>()
  private val defaultSet = { mutableSetOf<Fixture>() }

  // MARK: Lookup
  fun exists(fixture: Fixture): Boolean {
    return contactSet(fixture).size != 0
  }

  fun exists(fixture: Fixture, type: ContactType): Boolean {
    return contactSet(fixture)
      .find { (it.filterData.categoryBits and type) != 0 } != null
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


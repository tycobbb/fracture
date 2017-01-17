package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.physics.box2d.*
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug


class Contacts: ContactListener {
  // MARK: Properties
  private val map = mutableMapOf<Fixture, MutableSet<Fixture>>()
  private val defaultSet = { mutableSetOf<Fixture>() }

  // MARK: Lookup
  fun count(fixture: Fixture): Int {
    return contactSet(fixture).size
  }

  private fun contactSet(fixture: Fixture): MutableSet<Fixture> {
    return map.getOrPut(fixture, defaultSet)
  }

  // MARK: ContactListener
  override fun beginContact(contact: Contact?) {
    val contact = contact ?: return

    debug(Tag.Physics, "begin contact ${contact.fixtureA} to ${contact.fixtureB}")

    contactSet(contact.fixtureA).add(contact.fixtureB)
    contactSet(contact.fixtureB).add(contact.fixtureA)
  }

  override fun endContact(contact: Contact?) {
    val contact = contact ?: return

    debug(Tag.Physics, "end contact ${contact.fixtureA} to ${contact.fixtureB}")

    contactSet(contact.fixtureA).remove(contact.fixtureB)
    contactSet(contact.fixtureB).remove(contact.fixtureA)
  }

  override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
  }

  override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
  }
}


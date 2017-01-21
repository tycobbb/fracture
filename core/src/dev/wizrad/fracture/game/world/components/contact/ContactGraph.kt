package dev.wizrad.fracture.game.world.components.contact

import com.badlogic.gdx.physics.box2d.*
import dev.wizrad.fracture.game.world.support.contactInfo
import dev.wizrad.fracture.game.world.support.orientation
import dev.wizrad.fracture.support.extensions.findMapped

class ContactGraph: ContactListener {
  // MARK: Properties
  private val map = mutableMapOf<Fixture, MutableSet<Fixture>>()
  private val defaultSet = { mutableSetOf<Fixture>() }

  // MARK: Lookup
  fun closestSurface(fixture: Fixture): ContactInfo? {
    return contactSet(fixture).findMapped { it.contactInfo }
  }

  fun oriented(fixture: Fixture, orientation: ContactInfo.Orientation): Boolean {
    return contactSet(fixture).find { it.orientation == orientation } != null
  }

  fun all(fixture: Fixture): Collection<Fixture> {
    return contactSet(fixture)
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
//    debug(Tag.World, "pre-solve: ${format(contact?.fixtureA)} vs. ${format(contact?.fixtureB)}")
  }

  override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
//    debug(Tag.World, "post-solve: ${format(contact?.fixtureA)} vs. ${format(contact?.fixtureB)}")
  }

  private fun format(fixture: Fixture?): String {
    return if (fixture != null) "${fixture.filterData.categoryBits}: ${fixture.contactInfo}" else "<null>"
  }
}


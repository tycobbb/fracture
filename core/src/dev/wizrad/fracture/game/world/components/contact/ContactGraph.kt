package dev.wizrad.fracture.game.world.components.contact

import com.badlogic.gdx.physics.box2d.*
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Surface
import dev.wizrad.fracture.game.world.support.contactInfo
import dev.wizrad.fracture.game.world.support.surface
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.debugPrefix
import dev.wizrad.fracture.support.extensions.findMapped

class ContactGraph: ContactListener, ContactFilter {
  // MARK: Properties
  private val map = mutableMapOf<Fixture, MutableSet<Fixture>>()
  private val defaultSet = { mutableSetOf<Fixture>() }

  // MARK: Lookup
  fun nearestSurface(fixture: Fixture): Surface? {
    return contactSet(fixture).findMapped { it.surface }
  }

  fun nearestSurface(fixture: Fixture, filter: (Surface) -> Boolean): Surface? {
    return contactSet(fixture).findMapped { fixture ->
      fixture.surface?.let {
        if (filter(it)) it else null
      }
    }
  }

  fun isOnSurface(fixture: Fixture, orientation: Orientation): Boolean {
    return contactSet(fixture).any { fixture ->
      val surface = fixture.surface
      when (surface) {
        null -> false
        else -> surface.orientation == orientation
      }
    }
  }

  private fun contactSet(fixture: Fixture): MutableSet<Fixture> {
    return map.getOrPut(fixture, defaultSet)
  }

  // MARK: ContactListener
  override fun beginContact(contact: Contact?) {
    val contact = contact ?: return

//    debug(Tag.Physics, "$this begin contact: ${contact.fixtureA.contactInfo} on ${contact.fixtureB.contactInfo}")
    contactSet(contact.fixtureA).add(contact.fixtureB)
    contactSet(contact.fixtureB).add(contact.fixtureA)
  }

  override fun endContact(contact: Contact?) {
    val contact = contact ?: return

//    debug(Tag.Physics, "$this end contact: ${contact.fixtureA.contactInfo} on ${contact.fixtureB.contactInfo}")
    contactSet(contact.fixtureA).remove(contact.fixtureB)
    contactSet(contact.fixtureB).remove(contact.fixtureA)
  }

  override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
    val contact = contact ?: return

    // manually disable contacts for fixtures that have already collided if
    // they should now be filtered
    if (!shouldCollide(contact.fixtureA, contact.fixtureB)) {
      contact.isEnabled = false
    }
  }

  override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
  }

  // MARK: ContactFilter
  override fun shouldCollide(fixtureA: Fixture?, fixtureB: Fixture?): Boolean {
    val contact1 = fixtureA?.contactInfo ?: return true
    val contact2 = fixtureB?.contactInfo ?: return true

    val shouldPhase = when {
      contact1 is ContactInfo.Obstruction && contact2 is ContactInfo.Hero ->
        contact1.isPhaseable && contact2.isPhasing
      contact1 is ContactInfo.Hero && contact2 is ContactInfo.Obstruction ->
        contact1.isPhasing && contact2.isPhaseable
      else -> false
    }

    if (shouldPhase) {
      debug(Tag.Physics, "$this ignoring collision: $contact1 $contact2")
      return false
    }

    return true
  }

  // MARK: Debugging
  override fun toString(): String {
    return "[$debugPrefix]"
  }
}


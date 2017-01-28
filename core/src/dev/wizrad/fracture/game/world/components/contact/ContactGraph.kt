package dev.wizrad.fracture.game.world.components.contact

import com.badlogic.gdx.physics.box2d.*
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.support.extensions.contactInfo
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.debugPrefix

class ContactGraph: ContactListener, ContactFilter {
  // MARK: ContactListener
  override fun beginContact(contact: Contact?) {
    val fixture1 = contact?.fixtureA ?: return
    val fixture2 = contact?.fixtureB ?: return

    val entity1 = fixture1.body?.userData as? Entity
    val entity2 = fixture2.body?.userData as? Entity

    if (entity1 != null && entity2 != null) {
      entity1.onContact(fixture1, entity2, fixture2, didStart = true)
      entity2.onContact(fixture2, entity1, fixture1, didStart = true)
    }
  }

  override fun endContact(contact: Contact?) {
    val fixture1 = contact?.fixtureA ?: return
    val fixture2 = contact?.fixtureB ?: return

    val entity1 = fixture1.body?.userData as? Entity
    val entity2 = fixture2.body?.userData as? Entity

    if (entity1 != null && entity2 != null) {
      entity1.onContact(fixture1, entity2, fixture2, didStart = false)
      entity2.onContact(fixture2, entity1, fixture1, didStart = false)
    }
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
    val fixture1 = fixtureA ?: return true
    val fixture2 = fixtureB ?: return true

    // check category filters first
    val filter1 = fixture1.filterData
    val filter2 = fixture2.filterData

    val collision1 = filter1.categoryBits.and(filter2.maskBits)
    val collision2 = filter2.categoryBits.and(filter1.maskBits)

    if (collision1.eq(0) || collision2.eq(0)) {
      return false
    }

    // check phasing
    val contact1 = fixtureA.contactInfo
    val contact2 = fixtureB.contactInfo

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


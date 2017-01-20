package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class ReboundForm(context: Context): Form(context) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(context)
  }

  override fun defineFixtures(size: Vector2) {
    // create fixtures
    val square = PolygonShape()
    square.setAsBox(size.x, size.y)

    val fixture = FixtureDef()
    fixture.shape = square
    fixture.density = 1.0f
    fixture.friction = 0.2f
    fixture.restitution = 0.5f
    fixture.filter.categoryBits = ContactType.Hero.bits
    body.createFixture(fixture)

    // dispose shapes
    square.dispose()
  }

  // MARK: States
  class Standing(context: Context): FormState(context) {
    private val runMagnitude = 30.0f

    override fun update(delta: Float) {
      super.update(delta)

      // apply running movement
      val force = scratch1.setZero()
      if (controls.left.isPressed) {
        force.x -= runMagnitude
      }

      if (controls.right.isPressed) {
        force.x += runMagnitude
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      if (controls.jump.isPressedUnique && canJump()) {
        return Windup(context)
      }

      return null
    }

    private fun canJump(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return contact.oriented(body.fixtureList.first(), Orientation.Bottom)
    }
  }

  class Windup(context: Context): FormState(context) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(context, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(context: Context, val isShort: Boolean): FormState(context) {
    private val magnitude = if (isShort) 10.0f else 15.0f
    private val frameLength = 3

    override fun start() {
      debug(Tag.World, "$this applying impulse: $magnitude")
      val center = body.worldCenter
      body.applyLinearImpulse(0.0f, -magnitude, center.x, center.y, true)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): FormState(context) {
    private val driftMagnitude = 20.0f
    private val restingFrameLength = 2

    private var restingFrames = 0
    private var canFastfall = false

    override fun update(delta: Float) {
      super.update(delta)

      // apply directional influence
      val force = scratch1.setZero()
      if (controls.left.isPressed) {
        force.x -= driftMagnitude
      }

      if (controls.right.isPressed) {
        force.x += driftMagnitude
      }

      body.applyForceToCenter(force, true)

      // allow fastfalling any time after reaching the first jump's peak
      if (!canFastfall && isFalling()) {
        canFastfall = true
        controls.jump.requireUniquePress()
      }

      // if in contact with floor, increment resting frame count
      if (isLanding()) {
        restingFrames++
      } else {
        restingFrames = 0
      }
    }

    override fun nextState(): State? {
      // land once we've rested for enough frames (no longer bouncing)
      if (restingFrames >= restingFrameLength) {
        return Landing(context)
      } else if (controls.jump.isPressedUnique && canFastfall) {
        return FastFalling(context)
      }

      return null
    }

    private fun isLanding(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return contact.oriented(body.fixtureList.first(), Orientation.Bottom)
    }

    private fun isFalling(): Boolean {
      return body.linearVelocity.y >= 0.0
    }
  }

  class FastFalling(context: Context): FormState(context) {
    private val magnitude = 50.0f
    private val driftMagnitude = 20.0f

    override fun start() {
      debug(Tag.World, "$this applying fastfall impulse")
      val center = body.worldCenter
      body.applyLinearImpulse(0.0f, magnitude, center.x, center.y, true)
    }

    override fun update(delta: Float) {
      super.update(delta)

      // apply directional influence
      val force = Vector2()
      if (controls.left.isPressed) {
        force.x -= driftMagnitude
      }

      if (controls.right.isPressed) {
        force.x += driftMagnitude
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      // return to jumping at first contact to allow for re-falling
      return if (didLand()) Jumping(context) else null
    }

    private fun didLand(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return contact.oriented(body.fixtureList.first(), Orientation.Bottom)
    }
  }

  class Landing(context: Context): FormState(context) {
    private val frameLength = 3

    override fun start() {
      super.start()
      controls.jump.requireUniquePress()
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Standing(context) else null
    }
  }
}

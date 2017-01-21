package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context

class ReboundForm(context: Context): Form(context) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(context)
  }

  override fun defineFixtures(size: Vector2) {
    // create fixtures
    val width = size.x / 2
    val height = size.y / 2
    val square = PolygonShape()
    square.setAsBox(width, height, Vector2(width, height), 0.0f)

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
    private val runMagnitude = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMagnitude)
    }

    override fun nextState(): State? {
      if (controls.jump.isPressedUnique && isOnGround()) {
        return Windup(context)
      }

      return null
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

  class JumpStart(context: Context, isShort: Boolean): FormState(context) {
    private val frameLength = 3
    private val jumpMagnitude = if (isShort) 3.75f else 5.0f

    override fun start() {
      super.start()
      applyJumpForce(jumpMagnitude)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): FormState(context) {
    private val restingFrameLength = 2
    private val driftMagnitude = 5.0f

    private var restingFrames = 0
    private var canFastfall = false

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMagnitude)

      // allow fastfalling any time after reaching the first jump's peak
      if (!canFastfall && isFalling()) {
        canFastfall = true
        requireUniqueJump()
      }

      // if in contact with ground, increment resting frame count
      if (isOnGround()) {
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

    private fun isFalling(): Boolean {
      return body.linearVelocity.y >= 0.0
    }
  }

  class FastFalling(context: Context): FormState(context) {
    private val magnitude = 12.5f
    private val driftMagnitude = 5.0f

    override fun start() {
      super.start()
      applyFastfallImpulse(magnitude)
    }

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMagnitude)
    }

    override fun nextState(): State? {
      // return to jumping at first contact to allow for re-falling
      return if (isOnGround()) Jumping(context) else null
    }
  }

  class Landing(context: Context): FormState(context) {
    private val frameLength = 3

    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Standing(context) else null
    }
  }
}

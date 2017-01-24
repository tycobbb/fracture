package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context

class ReboundForm(context: Context): Form(context) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(context)
  }

  override fun defineFixtures() {
    val polygon = PolygonShape()

    // create fixtures
    val boxDef = defineBox(polygon)
    boxDef.restitution = 0.5f
    createBox(boxDef)
    createFoot(polygon)

    // dispose shapes
    polygon.dispose()
  }

  // MARK: States
  class Standing(context: Context): FormState(context) {
    private val runMagnitude = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMagnitude)
    }

    override fun nextState(): State? {
      return if (!isOnGround()) {
        Jumping(context)
      } else if (controls.jump.isPressedUnique) {
        Windup(context)
      } else null
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
      applyJumpImpulse(jumpMagnitude)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): FormState(context) {
    private val restingFrameLength = 10
    private val driftMagnitude = 5.0f

    private var restingFrames = 0
    private var canFastfall = false

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMagnitude)

      // allow fastfalling any time after reaching the first jump's peak
      if (!canFastfall && hasReachedApex()) {
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

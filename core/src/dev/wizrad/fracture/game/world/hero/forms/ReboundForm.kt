package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState

class ReboundForm(entity: Entity): Form(entity) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(this)
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
  class Standing(form: ReboundForm): FormState<ReboundForm>(form) {
    private val runMagnitude = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMagnitude)
    }

    override fun nextState(): State? {
      return if (!isOnGround()) {
        Jumping(form)
      } else if (controls.jump.isPressedUnique) {
        Windup(form)
      } else null
    }
  }

  class Windup(form: ReboundForm): FormState<ReboundForm>(form) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(form, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(form: ReboundForm, isShort: Boolean): FormState<ReboundForm>(form) {
    private val frameLength = 3
    private val jumpMagnitude = if (isShort) 3.75f else 5.0f

    override fun start() {
      super.start()
      applyJumpImpulse(jumpMagnitude)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(form) else null
    }
  }

  class Jumping(form: ReboundForm): FormState<ReboundForm>(form) {
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
        return Landing(form)
      } else if (controls.jump.isPressedUnique && canFastfall) {
        return FastFalling(form)
      }

      return null
    }
  }

  class FastFalling(form: ReboundForm): FormState<ReboundForm>(form) {
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
      return if (isOnGround()) Jumping(form) else null
    }
  }

  class Landing(form: ReboundForm): FormState<ReboundForm>(form) {
    private val frameLength = 3

    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Standing(form) else null
    }
  }
}

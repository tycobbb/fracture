package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormContext
import dev.wizrad.fracture.game.world.hero.core.FormState

class ReboundForm(hero: Hero): Form(hero), FormContext {
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
  class Standing(context: ReboundForm): FormState<ReboundForm>(context) {
    private val runMagnitude = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMagnitude)
    }

    override fun nextState() = when {
      !isOnGround() ->
        Jumping(context)
      controls.jump.isPressedUnique ->
        Windup(context)
      else -> null
    }
  }

  class Windup(context: ReboundForm): FormState<ReboundForm>(context) {
    private val frameLength = 4

    override fun nextState() = when {
      frame >= frameLength ->
        JumpStart(context, isShort = !controls.jump.isPressed)
      else -> null
    }
  }

  class JumpStart(context: ReboundForm, isShort: Boolean): FormState<ReboundForm>(context) {
    private val frameLength = 3
    private val jumpMagnitude = if (isShort) 3.75f else 5.0f

    override fun start() {
      super.start()
      applyJumpImpulse(jumpMagnitude)
    }

    override fun nextState() = when {
      frame >= frameLength ->
        Jumping(context)
      else -> null
    }
  }

  class Jumping(context: ReboundForm): FormState<ReboundForm>(context) {
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

    override fun nextState() = when {
      // land once we've rested for enough frames (no longer bouncing)
      restingFrames >= restingFrameLength ->
        Landing(context)
      controls.jump.isPressedUnique && canFastfall ->
        FastFalling(context)
      else -> null
    }
  }

  class FastFalling(context: ReboundForm): FormState<ReboundForm>(context) {
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

    override fun nextState() = when {
      // return to jumping at first contact to allow for re-falling
      isOnGround() ->
        Jumping(context)
      else -> null
    }
  }

  class Landing(context: ReboundForm): FormState<ReboundForm>(context) {
    private val frameLength = 3

    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun nextState() = when {
      frame >= frameLength ->
        Standing(context)
      else -> null
    }
  }
}

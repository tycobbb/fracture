package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.support.Maths

class AirDashForm(context: Context): Form(context) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(context)
  }

  override fun defineFixtures() {
    val polygon = PolygonShape()

    // create fixtures
    createBox(defineBox(polygon))
    createFoot(defineFoot(polygon))

    // dispose shapes
    polygon.dispose()
  }

  // MARK: States
  class Standing(context: Context): FormState(context) {
    private val runMag = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
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
    private val jumpMag = if (isShort) 3.75f else 7.5f

    override fun start() {
      applyJumpImpulse(jumpMag)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): FormState(context) {
    private val driftMag = 5.0f

    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState(): State? {
      val direction = inputDirection()
      return if (controls.jump.isPressedUnique && direction != Direction.None) {
        AirDashStart(context, direction)
      } else if(isOnGround()) {
        Landing(context)
      } else null
    }
  }

  class AirDashStart(context: Context, direction: Direction): FormState(context) {
    private val direction = direction
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return AirDash(context, direction, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class AirDash(context: Context, direction: Direction, isShort: Boolean): FormState(context) {
    private val dashMag = if (isShort) 15.0f else 25.0f
    private val dashDamping = 10.0f
    private val dashAngle = if (direction == Direction.Left) {
      Maths.F_PI + Maths.F_PI_4
    } else {
      Maths.F_PI + Maths.F_PI_4 * 3
    }

    override fun start() {
      super.start()

      stopGravity()
      startDamping(dashDamping)

      cancelMomentum()
      applyImpulse(magnitude = dashMag, angle = dashAngle)
    }

    override fun destroy() {
      super.destroy()

      startGravity()
      stopDamping()
    }

    override fun nextState(): State? {
      return if (isNearStationary()) AirDashEnd(context) else null
    }
  }

  class AirDashEnd(context: Context): FormState(context) {
    private val driftMag = 5.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState(): State? {
      return if (isOnGround()) Landing(context) else null
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

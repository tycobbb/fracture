package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Direction
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState
import dev.wizrad.fracture.support.Maths

class AirDashForm(hero: Hero): Form(hero) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(this)
  }

  override fun defineFixtures() {
    val polygon = PolygonShape()

    // create fixtures
    createBox(defineBox(polygon))
    createFoot(polygon)

    // dispose shapes
    polygon.dispose()
  }

  // MARK: States
  class Standing(form: AirDashForm): FormState<AirDashForm>(form) {
    private val runMag = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
    }

    override fun nextState(): State? {
      return if (!isOnGround()) {
        Jumping(form)
      } else if (controls.jump.isPressedUnique) {
        Windup(form)
      } else null
    }
  }

  class Windup(form: AirDashForm): FormState<AirDashForm>(form) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(form, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(form: AirDashForm, isShort: Boolean): FormState<AirDashForm>(form) {
    private val frameLength = 3
    private val jumpMag = if (isShort) 3.75f else 7.5f

    override fun start() {
      applyJumpImpulse(jumpMag)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(form) else null
    }
  }

  class Jumping(form: AirDashForm): FormState<AirDashForm>(form) {
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
        AirDashStart(form, direction)
      } else if(isOnGround()) {
        Landing(form)
      } else null
    }
  }

  class AirDashStart(form: AirDashForm, direction: Direction): FormState<AirDashForm>(form) {
    private val direction = direction
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return AirDash(form, direction, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class AirDash(form: AirDashForm, direction: Direction, isShort: Boolean): FormState<AirDashForm>(form) {
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
      return if (isNearStationary()) AirDashEnd(form) else null
    }
  }

  class AirDashEnd(form: AirDashForm): FormState<AirDashForm>(form) {
    private val driftMag = 5.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState(): State? {
      return if (isOnGround()) Landing(form) else null
    }
  }

  class Landing(form: AirDashForm): FormState<AirDashForm>(form) {
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

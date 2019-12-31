package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Direction
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormContext
import dev.wizrad.fracture.game.world.hero.core.FormState
import dev.wizrad.fracture.support.Maths

class AirDashForm(hero: Hero): Form(hero), FormContext {
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
  class Standing(context: AirDashForm): FormState<AirDashForm>(context) {
    private val runMag = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
    }

    override fun nextState() = when {
      !isOnGround() ->
        Jumping(context)
      controls.jump.isPressedUnique ->
        Windup(context)
      else -> null
    }
  }

  class Windup(context: AirDashForm): FormState<AirDashForm>(context) {
    private val frameLength = 4

    override fun nextState() = when {
      frame >= frameLength ->
        JumpStart(context, isShort = !controls.jump.isPressed)
      else -> null
    }
  }

  class JumpStart(context: AirDashForm, isShort: Boolean): FormState<AirDashForm>(context) {
    private val frameLength = 3
    private val jumpMag = if (isShort) 3.75f else 5.5f

    override fun start() {
      applyJumpImpulse(jumpMag)
    }

    override fun nextState() = when {
      frame >= frameLength ->
        Jumping(context)
      else -> null
    }
  }

  class Jumping(context: AirDashForm): FormState<AirDashForm>(context) {
    private val driftMag = 5.0f

    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState() = when {
      isOnGround() ->
        Landing(context)
      else -> inputDirectionOrNull()?.let {
        if (controls.jump.isPressedUnique) AirDashStart(context, direction = it) else null
      }
    }
  }

  class AirDashStart(context: AirDashForm, direction: Direction): FormState<AirDashForm>(context) {
    private val direction = direction
    private val frameLength = 4

    override fun nextState() = when {
      frame >= frameLength ->
        AirDash(context, direction, isShort = !controls.jump.isPressed)
      else -> null
    }
  }

  class AirDash(context: AirDashForm, direction: Direction, isShort: Boolean): FormState<AirDashForm>(context) {
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

    override fun nextState() = when {
      isNearStationary() ->
        AirDashEnd(context)
      else -> null
    }
  }

  class AirDashEnd(context: AirDashForm): FormState<AirDashForm>(context) {
    private val driftMag = 5.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState() = when {
      isOnGround() ->
        Landing(context)
      else -> null
    }
  }

  class Landing(context: AirDashForm): FormState<AirDashForm>(context) {
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

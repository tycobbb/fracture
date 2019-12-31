package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Direction
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormContext
import dev.wizrad.fracture.game.world.hero.core.FormState
import dev.wizrad.fracture.game.world.hero.forms.VanillaForm.Context
import dev.wizrad.fracture.support.Maths
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class VanillaForm(context: Context): Form<Context>(context) {
  class Context(
    override val hero: Hero): FormContext {

    var runStartFrames = 10
    var runStartDashSpeed = 3.0f
    var runCancelDashDamping = 1.5f
    var runningMag = 9.5f
    var runningMaxSpeed = 6.0f
    var jumpWindupFrames = 7
    var jumpStartFrames = 3
    var jumpStartShortMag = 4.75f
    var jumpStartMag = 6.75f
    var jumpingDriftMag = 7.0f
    var jumpingMaxSpeed = 6.0f
    var wallJumpFrames = 16
    var wallJumpMag = 5.0f
    var landingFrames = 3
  }

  // MARK: Form
  override fun initialState(): State {
    return Standing(context)
  }

  override fun defineFixtures() {
    val polygon = PolygonShape()

    // create fixtures
    val boxDef = defineBox(polygon)
    boxDef.friction = 0.2f
    createBox(boxDef)
    createFoot(polygon)

    // dispose shapes
    polygon.dispose()
  }

  // MARK: States
  abstract class GroundState(context: Context): FormState<Context>(context) {
    override fun nextState(): State? = when {
      !isOnGround() ->
        Jumping(context)
      controls.jump.isPressedUnique ->
        JumpWindup(context)
      else -> null
    }
  }

  class Standing(context: Context): GroundState(context) {
    override fun start() {
      super.start()
      requireUniqueMovement()
    }

    override fun nextState() = super.nextState() ?:
      inputDirectionOrNull(isUniqueInput = true)?.let {
        RunStart(context, direction = it)
      }
  }

  class RunStart(context: Context, private val direction: Direction): GroundState(context) {
    override fun start() {
      super.start()
      debug(Tag.Hero, "$this direction: $direction")
      requireUniqueMovement()
    }

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementSpeed(context.runStartDashSpeed, direction)
    }

    override fun nextState() = super.nextState() ?: when {
      frame >= context.runStartFrames ->
        finalState()
      inputDirection(isUniqueInput = true).opposes(direction) ->
        RunStart(context, direction.reverse())
      else -> null
    }

    private fun finalState() = when (inputDirection()) {
      direction ->
        Running(context, direction)
      else -> RunCancel(context)
    }
  }

  class RunCancel(context: Context): GroundState(context) {
    override fun start() {
      super.start()
      startDamping(context.runCancelDashDamping)
    }

    override fun destroy() {
      super.destroy()
      stopDamping()
    }

    override fun nextState() = super.nextState() ?: when {
      isStopping(threshold = 0.0f) ->
        Standing(context)
      else -> inputDirectionOrNull()?.let {
        RunStart(context, direction = it)
      }
    }
  }

  class Running(context: Context, private val direction: Direction): GroundState(context) {
    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(context.runningMag)
      applyMaxSpeed(context.runningMaxSpeed)
    }

    override fun nextState() = super.nextState() ?: when {
      currentDirection().opposes(direction) ->
        Running(context, direction.reverse())
      isStopping(threshold = 0.0f) ->
        stoppingState()
      else -> null
    }

    private fun stoppingState() = when {
      inputDirection().opposes(direction) ->
        Running(context, direction.reverse())
      else -> Standing(context)
    }
  }

  class JumpWindup(context: Context): FormState<Context>(context) {
    override fun nextState() = when {
      frame >= context.jumpWindupFrames ->
        JumpStart(context, isShort = !controls.jump.isPressed)
      else -> null
    }
  }

  class JumpStart(context: Context, isShort: Boolean): FormState<Context>(context) {
    private val jumpMag = if (isShort) context.jumpStartShortMag else context.jumpStartMag

    override fun start() {
      super.start()
      applyJumpImpulse(jumpMag)
    }

    override fun nextState() = when {
      frame >= context.jumpStartFrames ->
        Jumping(context)
      else -> null
    }
  }

  class Jumping(context: Context): FormState<Context>(context) {
    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(context.jumpingDriftMag)
      applyMaxSpeed(context.jumpingMaxSpeed)
    }

    override fun nextState() = when {
      isOnGround() ->
        Landing(context)
      else -> currentWallContactOrientation()?.let {
        if (controls.jump.isPressedUnique) WallJumpStart(context, orientation = it) else null
      }
    }
  }

  class WallJumpStart(context: Context, orientation: Orientation): FormState<Context>(context) {
    private val wallJumpAngle = if (orientation.isLeft) {
      Maths.F_PI * 11 / 8
    } else {
      Maths.F_PI * 13 / 8
    }

    override fun start() {
      super.start()
      cancelMomentum()
      applyImpulse(magnitude = context.wallJumpMag, angle = wallJumpAngle)
    }

    override fun nextState() = when {
      frame >= context.wallJumpFrames ->
        Jumping(context)
      else -> null
    }
  }

  class Landing(context: Context): GroundState(context) {
    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun nextState() = super.nextState() ?: when {
      frame >= context.landingFrames ->
        finalState()
      else -> null
    }

    private fun finalState(): State {
      var direction = currentDirection()
      if (direction == Direction.None) {
        direction = inputDirection()
      }

      return when (direction) {
        Direction.Left, Direction.Right ->
          Running(context, direction)
        else -> Standing(context)
      }
    }
  }
}

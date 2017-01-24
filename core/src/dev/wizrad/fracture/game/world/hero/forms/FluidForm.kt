package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.support.Maths
import dev.wizrad.fracture.support.abs

class FluidForm(context: Context): Form(context) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(context)
  }

  override fun defineFixtures() {
    val polygon = PolygonShape()

    // create fixtures
    val boxDef = defineBox(polygon)
    boxDef.friction = 0.1f
    createBox(defineBox(polygon))
    createFoot(polygon)

    // dispose shapes
    polygon.dispose()
  }

  // MARK: States
  class Standing(context: Context): FormState(context) {
    override fun nextState(): State? = when {
      !isOnGround() ->
        Jumping(context)
      controls.jump.isPressedUnique ->
        JumpWindup(context)
      controls.left.isPressed || controls.right.isPressed ->
        RunStart(context)
      else -> null
    }
  }

  class RunStart(context: Context): FormState(context) {
    private val frameLength = 3

    override fun nextState(): State? = when {
      frame < frameLength ->
        null
      canStartRunning() ->
        Running(context)
      else -> Standing(context)
    }
  }

  class Running(context: Context, impact: Impact? = null): FormState(context) {
    private val impact = impact
    private val runMag = 12.5f
    private val wallRunFrameTimeout = 5
    private val transferDecay = 0.85f
    private var lastVelocity = body.linearVelocity.cpy()

    override fun start() {
      super.start()

      if (impact != null) {
        body.setLinearVelocity(initialSpeed(impact), 0.0f)
      }
    }

    override fun update(delta: Float) {
      super.update(delta)
      lastVelocity.set(body.linearVelocity)
    }

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
    }

    override fun nextState(): State? {
      if (frame >= wallRunFrameTimeout) {
        val direction = currentDirection(vx = lastVelocity.x)
        val orientation = currentWallContactOrientation()

        when {
          direction.isRight && orientation == Orientation.Left ->
            return WallRunning(context, impact = Impact(lastVelocity, Orientation.Left))
          direction.isLeft && orientation == Orientation.Right ->
            return WallRunning(context, impact = Impact(lastVelocity, Orientation.Right))
        }
      }

      return when {
        isStopping() ->
          Standing(context) // TODO: probably go into a run-stop type state
        !isOnGround() ->
          Jumping(context)
        controls.jump.isPressedUnique ->
          JumpWindup(context)
        else -> null
      }
    }

    private fun initialSpeed(impact: Impact): Float {
      val transferredSpeed = abs(impact.velocity.y) * transferDecay
      return if (impact.orientation.isLeft) -transferredSpeed else transferredSpeed
    }
  }

  class JumpWindup(context: Context): FormState(context) {
    private val frameLength = 4

    override fun nextState(): State? = when {
      frame >= frameLength ->
        JumpStart(context, isShort = !controls.jump.isPressed)
      else -> null
    }
  }

  class JumpStart(context: Context, isShort: Boolean): FormState(context) {
    private val frameLength = 3
    private val jumpMag = if (isShort) 2.5f else 5.0f

    override fun start() {
      super.start()
      applyJumpImpulse(jumpMag)
    }

    override fun nextState(): State? = when {
      frame >= frameLength ->
        Jumping(context)
      else -> null
    }
  }

  class Jumping(context: Context): FormState(context) {
    private val driftMag = 5.0f
    private var lastVelocity = body.linearVelocity.cpy()

    override fun update(delta: Float) {
      super.update(delta)
      lastVelocity.set(0.0f, body.linearVelocity.y)
    }

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState(): State? {
      val orientation = currentWallContactOrientation()

      return when {
        orientation == Orientation.Left ->
          WallRunning(context, impact = Impact(lastVelocity, orientation))
        orientation == Orientation.Right ->
          WallRunning(context, impact = Impact(lastVelocity, orientation))
        isOnGround() ->
          Landing(context)
        else -> null
      }
    }
  }

  class Landing(context: Context): FormState(context) {
    private val frameLength = 3

    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun nextState(): State? = when {
      frame >= frameLength ->
        Standing(context)
      else -> null
    }
  }

  class WallRunning(context: Context, impact: Impact): FormState(context) {
    private val impact = impact
    private val landingFrameTimeout = 5
    private val transferDecay = 0.85f
    private var lastVelocity = body.linearVelocity.cpy()

    override fun start() {
      super.start()
      requireUniqueJump()
      body.setLinearVelocity(0.0f, initialSpeed())
    }

    override fun step(delta: Float) {
      super.step(delta)
      lastVelocity.set(body.linearVelocity)
    }

    override fun nextState(): State? = when {
      isOnGround(frameTimeout = landingFrameTimeout) ->
        Running(context, impact = Impact(lastVelocity, impact.orientation))
      isAirborne() ->
        Jumping(context)
      controls.jump.isPressedUnique ->
        WallJumpWindup(context, impact.orientation)
      else -> null
    }

    private fun initialSpeed(): Float {
      val impactSpeed = impact.velocity.y
      val transferredSpeed = abs(impact.velocity.x) * transferDecay

      return if (impactSpeed > 0.0) {
        impactSpeed + transferredSpeed
      } else {
        impactSpeed - transferredSpeed
      }
    }
  }

  class WallJumpWindup(context: Context, orientation: Orientation): FormState(context) {
    private val orientation = orientation
    private val frameLength = 4

    override fun nextState(): State? = when {
      frame >= frameLength ->
        WallJumpStart(context, orientation, isShort = !controls.jump.isPressed)
      else -> null
    }
  }

  class WallJumpStart(context: Context, orientation: Orientation, isShort: Boolean): FormState(context) {
    private val frameLength = 3
    private val wallJumpMag = if (isShort) 6.0f else 8.0f
    private val wallJumpAngle = if (orientation.isLeft) {
      Maths.F_PI * 11 / 8
    } else {
      Maths.F_PI * 13 / 8
    }

    override fun start() {
      super.start()
      cancelMomentum()
      applyImpulse(magnitude = wallJumpMag, angle = wallJumpAngle)
    }

    override fun nextState(): State? = when {
      frame >= frameLength ->
        Jumping(context)
      else -> null
    }
  }

  // MARK: Impact
  data class Impact(
    val velocity: Vector2,
    val orientation: Orientation
  )
}

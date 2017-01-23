package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.support.Maths
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.abs
import dev.wizrad.fracture.support.debug

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

    createAppendage(polygon, orientation = Orientation.Left)
    createAppendage(polygon, orientation = Orientation.Right)
    createAppendage(polygon, orientation = Orientation.Bottom)

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
      frame >= frameLength ->
        Running(context)
      else -> null
    }
  }

  class Running(context: Context): FormState(context) {
    private val runMag = 12.5f
    private var lastVx = 0.0f

    override fun update(delta: Float) {
      super.update(delta)
      lastVx = body.linearVelocity.x
      debug(Tag.World, "$this update -> vx: $lastVx")
    }

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
      debug(Tag.World, "$this step -> vx: $lastVx")
    }

    override fun nextState(): State? {
      val direction = currentDirection(vx = lastVx)
      val orientation = wallContactOrientation()

      debug(Tag.World, "$this ns -> d: $direction o: $orientation")

      return when {
        direction.isRight && orientation == Orientation.Left ->
          WallRunning(context, Orientation.Left, vx = lastVx)
        direction.isLeft && orientation == Orientation.Right ->
          WallRunning(context, Orientation.Right, vx = lastVx)
        isStopping() ->
          Standing(context) // TODO: probably go into a run-stop type state
        !isOnGround() ->
          Jumping(context)
        controls.jump.isPressedUnique ->
          JumpWindup(context)
        else -> null
      }
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
    private val jumpMag = if (isShort) 3.75f else 9.5f

    override fun start() {
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

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState(): State? = when {
      isOnGround() ->
        Landing(context)
      else -> null
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

  class WallRunning(context: Context, orientation: Orientation, vx: Float): FormState(context) {
    private val orientation = orientation
    private val landingFrameTimeout = 10
    private val initalVy = -abs(vx)

    override fun start() {
      super.start()

      requireUniqueJump()
      // rotate velocity up the wall
      body.setLinearVelocity(0.0f, initalVy)
    }

    override fun nextState(): State? = when {
      isOnGround(frameTimeout = landingFrameTimeout) ->
        Running(context)
      controls.jump.isPressedUnique ->
        WallJumpWindup(context, orientation)
      else -> null
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
}

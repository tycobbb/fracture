package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context

class SpaceJumpForm(context: Context): Form(context) {
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
    private val jumpMag = if (isShort) 2.5f else 5.0f

    override fun start() {
      super.start()
      applyJumpImpulse(jumpMag)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): FormState(context) {
    private val driftMag = 10.0f
    private var canJump: Boolean = false

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)

      // allow re-jump when the apex is reached
      if (!canJump && isFalling()) {
        canJump = true
        requireUniqueJump()
      }
    }

    override fun nextState(): State? {
      if (isOnGround()) {
        return Landing(context)
      } else if (controls.jump.isPressedUnique && canJump) {
        return Windup2(context)
      }

      return null
    }

    private fun isFalling(): Boolean {
      return body.linearVelocity.y >= 0.0
    }
  }

  class Windup2(context: Context): FormState(context) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart2(context, isShort = !controls.jump.isPressed, direction = inputDirection())
      }

      return null
    }
  }

  class JumpStart2(context: Context, direction: Direction, isShort: Boolean): FormState(context) {
    private val frameLength = 3
    private val direction = direction
    private val jumpMag = if (isShort) 5.0f else 7.5f

    override fun start() {
      super.start()

      cancelComponentMomentum(
        x = direction != Direction.None && currentDirection() != direction,
        y = true
      )

      applyJumpImpulse(jumpMag)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping2(context) else null
    }
  }

  class Jumping2(context: Context): FormState(context) {
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

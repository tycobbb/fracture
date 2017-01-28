package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Direction
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState

class SpaceJumpForm(hero: Hero): Form(hero) {
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
  class Standing(form: SpaceJumpForm): FormState<SpaceJumpForm>(form) {
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

  class Windup(form: SpaceJumpForm): FormState<SpaceJumpForm>(form) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(form, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(form: SpaceJumpForm, isShort: Boolean): FormState<SpaceJumpForm>(form) {
    private val frameLength = 3
    private val jumpMag = if (isShort) 4.0f else 5.5f

    override fun start() {
      super.start()
      applyJumpImpulse(jumpMag)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(form) else null
    }
  }

  class Jumping(form: SpaceJumpForm): FormState<SpaceJumpForm>(form) {
    private val driftMag = 10.0f
    private var canJump: Boolean = false

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)

      // allow re-jump when the apex is reached
      if (!canJump && hasReachedApex()) {
        canJump = true
        requireUniqueJump()
      }
    }

    override fun nextState(): State? {
      if (isOnGround()) {
        return Landing(form)
      } else if (controls.jump.isPressedUnique && canJump) {
        return Windup2(form)
      }

      return null
    }
  }

  class Windup2(form: SpaceJumpForm): FormState<SpaceJumpForm>(form) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart2(form, isShort = !controls.jump.isPressed, direction = inputDirection())
      }

      return null
    }
  }

  class JumpStart2(form: SpaceJumpForm, isShort: Boolean, direction: Direction): FormState<SpaceJumpForm>(form) {
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
      return if (frame >= frameLength) Jumping2(form) else null
    }
  }

  class Jumping2(form: SpaceJumpForm): FormState<SpaceJumpForm>(form) {
    private val driftMag = 5.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState(): State? {
      return if (isOnGround()) Landing(form) else null
    }
  }

  class Landing(form: SpaceJumpForm): FormState<SpaceJumpForm>(form) {
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

package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Direction
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormContext
import dev.wizrad.fracture.game.world.hero.core.FormState

class SpaceJumpForm(hero: Hero): Form(hero), FormContext {
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
  class Standing(context: SpaceJumpForm): FormState<SpaceJumpForm>(context) {
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

  class Windup(context: SpaceJumpForm): FormState<SpaceJumpForm>(context) {
    private val frameLength = 4

    override fun nextState() = when {
      frame >= frameLength ->
        JumpStart(context, isShort = !controls.jump.isPressed)
      else -> null
    }
  }

  class JumpStart(context: SpaceJumpForm, isShort: Boolean): FormState<SpaceJumpForm>(context) {
    private val frameLength = 3
    private val jumpMag = if (isShort) 4.0f else 5.5f

    override fun start() {
      super.start()
      applyJumpImpulse(jumpMag)
    }

    override fun nextState() = when {
      frame >= frameLength ->
        Jumping(context)
      else -> null
    }
  }

  class Jumping(context: SpaceJumpForm): FormState<SpaceJumpForm>(context) {
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

    override fun nextState() = when {
      isOnGround() ->
        Landing(context)
      controls.jump.isPressedUnique && canJump ->
        Windup2(context)
      else -> null
    }
  }

  class Windup2(context: SpaceJumpForm): FormState<SpaceJumpForm>(context) {
    private val frameLength = 4

    override fun nextState() = when {
      frame >= frameLength ->
        JumpStart2(context, isShort = !controls.jump.isPressed, direction = inputDirection())
      else -> null
    }
  }

  class JumpStart2(context: SpaceJumpForm, isShort: Boolean, direction: Direction): FormState<SpaceJumpForm>(context) {
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

    override fun nextState() = when {
      frame >= frameLength ->
        Jumping2(context)
      else -> null
    }
  }

  class Jumping2(context: SpaceJumpForm): FormState<SpaceJumpForm>(context) {
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

  class Landing(context: SpaceJumpForm): FormState<SpaceJumpForm>(context) {
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

package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Direction
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class VanillaForm(hero: Hero): Form(hero) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(this)
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
  class Standing(form: VanillaForm): FormState<VanillaForm>(form) {
    override fun start() {
      super.start()
      requireUniqueMovement()
    }

    override fun nextState(): State? = when {
      !isOnGround() ->
        Jumping(form)
      controls.jump.isPressedUnique ->
        JumpWindup(form)
      else -> inputDirectionOrNull(isUniqueInput = true)?.let {
        RunStart(form, direction = it)
      }
    }
  }

  class RunStart(form: VanillaForm, direction: Direction): FormState<VanillaForm>(form) {
    private val direction = direction
    private val frameLength = 10
    private val dashSpeed = 3.0f

    override fun start() {
      super.start()
      debug(Tag.Hero, "$this direction: $direction")
      requireUniqueMovement()
    }

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementSpeed(dashSpeed, direction)
    }

    override fun nextState() = when {
      !isOnGround() ->
        Jumping(form)
      frame >= frameLength ->
        finalState()
      controls.jump.isPressedUnique ->
        JumpWindup(form)
      inputDirection(isUniqueInput = true).opposes(direction) ->
        RunStart(form, direction.reverse())
      else -> null
    }

    private fun finalState() = when (inputDirection()) {
      direction ->
        Running(form, direction)
      else -> RunCancel(form)
    }
  }

  class RunCancel(form: VanillaForm): FormState<VanillaForm>(form) {
    private val dashDamping = 1.5f

    override fun start() {
      super.start()
      startDamping(dashDamping)
    }

    override fun destroy() {
      super.destroy()
      stopDamping()
    }

    override fun nextState() = when {
      isStopping(threshold = 0.0f) ->
        Standing(form)
      else -> inputDirectionOrNull()?.let {
        RunStart(form, direction = it)
      }
    }
  }

  class Running(form: VanillaForm, direction: Direction): FormState<VanillaForm>(form) {
    private val direction = direction
    private val runMag = 9.5f
    private val maxSpeed = 6.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
      applyMaxSpeed(maxSpeed)
    }

    override fun nextState() = when {
      !isOnGround() ->
        Jumping(form)
      controls.jump.isPressedUnique ->
        JumpWindup(form)
      currentDirection().opposes(direction) ->
        Running(form, direction.reverse())
      isStopping(threshold = 0.0f) ->
        stoppingState()
      else -> null
    }

    private fun stoppingState() = when {
      inputDirection().opposes(direction) ->
        Running(form, direction.reverse())
      else -> Standing(form)
    }
  }

  class JumpWindup(form: VanillaForm): FormState<VanillaForm>(form) {
    private val frameLength = 7

    override fun nextState() = when {
      frame >= frameLength ->
        JumpStart(form, isShort = !controls.jump.isPressed)
      else -> null
    }
  }

  class JumpStart(form: VanillaForm, isShort: Boolean): FormState<VanillaForm>(form) {
    private val frameLength = 3
    private val jumpMag = if (isShort) 4.75f else 6.75f

    override fun start() {
      super.start()
      applyJumpImpulse(jumpMag)
    }

    override fun nextState() = when {
      frame >= frameLength ->
        Jumping(form)
      else -> null
    }
  }

  class Jumping(form: VanillaForm): FormState<VanillaForm>(form) {
    private val driftMag = 7.0f
    private val maxSpeed = 6.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
      applyMaxSpeed(maxSpeed)
    }

    override fun nextState() = when {
      isOnGround() ->
        Landing(form)
      else -> null
    }
  }

  class Landing(form: VanillaForm): FormState<VanillaForm>(form) {
    private val frameLength = 5

    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun nextState() = when {
      frame >= frameLength ->
        finalState()
      controls.jump.isPressedUnique ->
        JumpWindup(form)
      else -> null
    }

    private fun finalState(): State {
      var direction = currentDirection()
      if (direction == Direction.None) {
        direction = inputDirection()
      }

      return when (direction) {
        Direction.Left, Direction.Right ->
          Running(form, direction)
        else -> Standing(form)
      }
    }
  }
}

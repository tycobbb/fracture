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
    createBox(boxDef)
    createFoot(polygon)

    // dispose shapes
    polygon.dispose()
  }

  // MARK: States
  class Standing(form: VanillaForm): FormState<VanillaForm>(form) {
    override fun nextState(): State? = when {
      !isOnGround() ->
        Jumping(form)
      controls.jump.isPressedUnique ->
        JumpWindup(form)
      else -> inputDirectionOrNull()?.let {
        RunStart(form, direction = it)
      }
    }
  }

  class RunStart(form: VanillaForm, direction: Direction): FormState<VanillaForm>(form) {
    private val direction = direction
    private val frameLength = 14
    private val dashImpulse = 3.0f

    override fun start() {
      super.start()
      cancelComponentMomentum(x = true)
      applyMovementImpulse(dashImpulse, direction)
    }

    override fun nextState() = when {
      !isOnGround() ->
        Jumping(form)
      frame >= frameLength ->
        Running(form, direction)
      controls.jump.isPressedUnique ->
        JumpWindup(form)
      else -> inputDirectionOrNull()?.let {
        if (it != direction) RunStart(form, direction = it) else null
      }
    }
  }

  class Running(form: VanillaForm, direction: Direction): FormState<VanillaForm>(form) {
    private val direction = direction
    private val runMag = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
    }

    override fun nextState() = when {
      !isOnGround() ->
        Jumping(form)
      isStopping(frameTimeout = 5, threshold = 0.0f) ->
        Stopping(form, direction.reverse())
      controls.jump.isPressedUnique ->
        JumpWindup(form)
      else -> inputDirectionOrNull()?.let {
        if (it != direction) Stopping(form, it) else null
      }
    }
  }

  class Stopping(form: VanillaForm, direction: Direction): FormState<VanillaForm>(form) {
    private val direction = direction
    private val damping = 5.0f

    override fun start() {
      super.start()
      startDamping(damping)
    }

    override fun destroy() {
      super.destroy()
      stopDamping()
    }

    override fun nextState() = when {
      isStopping(threshold = 0.1f) -> {
        debug(Tag.World, "d: $direction vs ${inputDirection()}")
        if (direction == inputDirection()) Running(form, direction) else Standing(form)
      }
      else -> null
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
    private val jumpMag = if (isShort) 4.75f else 8.25f

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

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
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

    override fun nextState(): State? = when {
      frame >= frameLength ->
        if (isStopping()) Standing(form) else Running(form, currentDirection())
      controls.jump.isPressedUnique ->
        JumpWindup(form)
      else -> null
    }
  }
}

package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState

class VanillaForm(hero: Hero): Form(hero) {
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
  class Standing(form: VanillaForm): FormState<VanillaForm>(form) {
    private val runMag = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
    }

    override fun nextState(): State? {
      return if (!isOnGround()) {
        Jumping(form)
      } else if (controls.jump.isPressedUnique) {
        JumpWindup(form)
      } else null
    }
  }

  class JumpWindup(form: VanillaForm): FormState<VanillaForm>(form) {
    private val frameLength = 7

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(form, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(form: VanillaForm, isShort: Boolean): FormState<VanillaForm>(form) {
    private val frameLength = 3
    private val jumpMag = if (isShort) 4.75f else 8.25f

    override fun start() {
      applyJumpImpulse(jumpMag)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(form) else null
    }
  }

  class Jumping(form: VanillaForm): FormState<VanillaForm>(form) {
    private val driftMag = 7.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState(): State? {
      return if (isOnGround()) Landing(form) else null
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
        Standing(form)
      controls.jump.isPressedUnique ->
        JumpWindup(form)
      else -> null
    }
  }
}

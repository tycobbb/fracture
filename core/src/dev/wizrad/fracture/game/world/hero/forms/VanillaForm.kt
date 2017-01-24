package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState

class VanillaForm(context: Context): Form(context) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(context)
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
  class Standing(context: Context): FormState(context) {
    private val runMag = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
    }

    override fun nextState(): State? {
      return if (!isOnGround()) {
        Jumping(context)
      } else if (controls.jump.isPressedUnique) {
        JumpWindup(context)
      } else null
    }
  }

  class JumpWindup(context: Context): FormState(context) {
    private val frameLength = 7

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(context, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(context: Context, isShort: Boolean): FormState(context) {
    private val frameLength = 3
    private val jumpMag = if (isShort) 4.75f else 8.25f

    override fun start() {
      applyJumpImpulse(jumpMag)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): FormState(context) {
    private val driftMag = 7.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState(): State? {
      return if (isOnGround()) Landing(context) else null
    }
  }

  class Landing(context: Context): FormState(context) {
    private val frameLength = 5

    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun nextState(): State? = when {
      frame >= frameLength ->
        Standing(context)
      controls.jump.isPressedUnique ->
        JumpWindup(context)
      else -> null
    }
  }
}

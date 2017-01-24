package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState

class FlutterForm(context: Context): Form(context) {
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
    private val runMag = 5.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
    }

    override fun nextState() = when {
      !isOnGround() ->
        Jumping(context)
      controls.jump.isJustPressed ->
        Windup(context)
      else -> null
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
    private val jumpMag = if (isShort) 3.75f else 5.0f

    override fun start() {
      applyJumpImpulse(jumpMag)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): FormState(context) {
    private val driftMag = 5.0f
    private val flutterMag = 3.5f
    private var shouldFlutter = false

    override fun update(delta: Float) {
      super.update(delta)
      shouldFlutter = controls.jump.isJustPressed
    }

    override fun step(delta: Float) {
      super.step(delta)

      applyMovementForce(driftMag)

      if(shouldFlutter) {
        applyJumpImpulse(flutterMag)
        shouldFlutter = false
      }
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

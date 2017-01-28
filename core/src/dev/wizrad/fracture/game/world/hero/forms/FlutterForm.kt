package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState

class FlutterForm(hero: Hero): Form(hero) {
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
  class Standing(form: FlutterForm): FormState<FlutterForm>(form) {
    private val runMag = 5.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
    }

    override fun nextState() = when {
      !isOnGround() ->
        Jumping(form)
      controls.jump.isJustPressed ->
        Windup(form)
      else -> null
    }
  }

  class Windup(form: FlutterForm): FormState<FlutterForm>(form) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(form, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(form: FlutterForm, isShort: Boolean): FormState<FlutterForm>(form) {
    private val frameLength = 3
    private val jumpMag = if (isShort) 3.75f else 5.0f

    override fun start() {
      applyJumpImpulse(jumpMag)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(form) else null
    }
  }

  class Jumping(form: FlutterForm): FormState<FlutterForm>(form) {
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
      return if (isOnGround()) Landing(form) else null
    }
  }

  class Landing(form: FlutterForm): FormState<FlutterForm>(form) {
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

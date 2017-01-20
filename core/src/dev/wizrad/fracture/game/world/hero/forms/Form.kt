package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.components.statemachine.StateMachine
import dev.wizrad.fracture.game.world.core.Context

abstract class Form(
  val context: Context): StateMachine() {

  // MARK: Properties
  protected val body: Body get() = context.parent!!.body

  // MARK: Lifecycle
  protected abstract fun initialState(): State

  // MARK: Hooks
  abstract fun defineFixtures(size: Vector2)

  // MARK: Behavior
  override fun start() {
    super.start()
    state = initialState()
  }
}
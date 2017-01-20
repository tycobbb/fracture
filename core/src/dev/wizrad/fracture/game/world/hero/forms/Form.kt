package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.components.statemachine.StateMachine

abstract class Form(initialState: State): StateMachine(initialState) {
  abstract fun defineFixtures(size: Vector2)
}
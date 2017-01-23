package dev.wizrad.fracture.game.world.components.statemachine

import dev.wizrad.fracture.game.world.core.Behavior
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.className
import dev.wizrad.fracture.support.debug

open class StateMachine(
  initialState: State = State.Stopped()): Behavior() {

  // MARK: Properties
  var state: State = initialState; protected set

  // MARK: Behavior
  override fun start() {
    super.start()
    state.start()
  }

  override fun update(delta: Float) {
    super.update(delta)
    state.update(delta)
  }

  override fun step(delta: Float) {
    super.step(delta)
    state.step(delta)
  }

  override fun lateUpdate(delta: Float) {
    super.lateUpdate(delta)

    state.lateUpdate(delta)
    state.nextState()?.let {
      debug(Tag.World, "$state ended -> ${it.className}")
      state.destroy()
      state = it
      state.start()
    }
  }

  override fun destroy() {
    super.destroy()
    state.destroy()
  }
}
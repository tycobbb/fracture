package dev.wizrad.fracture.game.world.components.statemachine

import com.badlogic.gdx.physics.box2d.Body
import dev.wizrad.fracture.game.world.core.Behavior
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.support.debugPrefix

abstract class State(
  val context: Context): Behavior() {

  // MARK: Properties
  protected var frame: Int = 0
  protected val body: Body get() = context.body
  protected val world: World get() = context.world

  // MARK: Sequence
  abstract fun nextState(): State?

  // MARK: Behavior
  override fun update(delta: Float) {
    super.update(delta)
    frame++
  }

  // MARK: Debugging
  override fun toString(): String {
    return "[$debugPrefix frame=$frame]"
  }

  // MARK: Context
  data class Context(
    val body: Body,
    val world: World
  )
}

package dev.wizrad.fracture.game.world.core

import dev.wizrad.fracture.game.core.Updatable

interface Behavior: Updatable {
  fun step(delta: Float)
  fun destroy()
}

fun <E: Behavior> Array<E>.step(delta: Float) = forEach { it.step(delta) }
fun <E: Behavior> Array<E>.destroy() = forEach { it.destroy() }

package dev.wizrad.fracture.game.core

interface Updatable {
  fun update(delta: Float)
}



fun <E: Updatable> Array<E>.update(delta: Float) = forEach { it.update(delta) }
fun <E: Updatable> Iterable<E>.update(delta: Float) = forEach { it.update(delta) }

package dev.wizrad.fracture.game.core

interface Updatable {
  fun update(delta: Float)
}

fun <E: Updatable> Array<E>.update(delta: Float) {
  for(element in this) {
    element.update(delta)
  }
}

fun <E: Updatable> Iterable<E>.update(delta: Float) {
  for(element in this) {
    element.update(delta)
  }
}

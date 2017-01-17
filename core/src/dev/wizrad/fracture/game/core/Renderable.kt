package dev.wizrad.fracture.game.core

interface Renderable: Updatable {
  fun resize(width: Int, height: Int)
}

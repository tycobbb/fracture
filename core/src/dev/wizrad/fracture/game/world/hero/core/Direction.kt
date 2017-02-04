package dev.wizrad.fracture.game.world.hero.core

enum class Direction {
  None, Left, Right;

  val isNone: Boolean get() = this == None
  val isLeft: Boolean get() = this == Left
  val isRight: Boolean get() = this == Right

  fun reverse() = when(this) {
    Left -> Right
    Right -> Left
    None -> None
  }
}
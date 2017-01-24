package dev.wizrad.fracture.game.world.components.contact

// MARK: Orientation
enum class Orientation {
  Bottom, Left, Top, Right;

  // MARK: Checks
  val isTop: Boolean get() = this == Top
  val isBottom: Boolean get() = this == Bottom
  val isLeft: Boolean get() = this == Left
  val isRight: Boolean get() = this == Right

  // MARK: Operators
  fun mirror() = when (this) {
    Top -> Bottom
    Bottom -> Top
    Left -> Right
    Right -> Left
  }
}
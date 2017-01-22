package dev.wizrad.fracture.game.world.components.contact

data class ContactInfo(
  val orientation: Orientation,
  val isPhaseable: Boolean = false) {

  enum class Orientation {
    Bottom, Left, Top, Right
  }
}

package dev.wizrad.fracture.game.world.components.contact

data class ContactInfo(
  val orientation: Orientation) {

  enum class Orientation {
    Bottom, Left, Top, Right
  }
}

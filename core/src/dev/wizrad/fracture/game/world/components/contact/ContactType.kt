package dev.wizrad.fracture.game.world.components.contact

enum class ContactType(val value: Int) {
  None(0),
  Hero(1 shl 0),
  Terrain(1 shl 1),
  Event(1 shl 2);

  // MARK: Representations
  val category: Short get()
    = value.toShort()

  val mask: Short get() = when(this) {
    None -> 0
    Hero -> !Hero
    Terrain -> !Terrain
    Event -> -1
  }
}

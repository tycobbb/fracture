package dev.wizrad.fracture.game.world.components.contact

import dev.wizrad.fracture.support.className

sealed class ContactInfo {
  // MARK: Actors
  class Hero(
    var isPhasing: Boolean = false): ContactInfo() {
    override fun toString(): String = "[$className p=$isPhasing]"
  }

  class Foot: ContactInfo() {
    override fun toString(): String = "[$className]"
  }

  // MARK: Obstructions
  interface Obstruction {
    val isPhaseable: Boolean
  }

  class Surface(
    val orientation: Orientation,
    val isPhasingTarget: Boolean = false,
    override val isPhaseable: Boolean = false): ContactInfo(), Obstruction {
    override fun toString(): String = "[$className o=$orientation pt=$isPhasingTarget p=$isPhaseable]"
  }
}

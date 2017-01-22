package dev.wizrad.fracture.game.world.components.contact

import dev.wizrad.fracture.support.className

sealed class ContactInfo {
  enum class Orientation { Bottom, Left, Top, Right }

  // MARK: Actors
  class Hero(
    val isPhasing: Boolean): ContactInfo() {
    override fun toString(): String = "[$className p=$isPhasing]"
  }

  // MARK: Obstructions
  interface Obstruction {
    val isPhaseable: Boolean
  }

  class Barrier(
    override val isPhaseable: Boolean = false): ContactInfo(), Obstruction {
    override fun toString(): String = "[$className p=$isPhaseable]"
  }

  class Surface(
    val orientation: Orientation,
    val isPhasingTarget: Boolean = false,
    override val isPhaseable: Boolean = false): ContactInfo(), Obstruction {
    override fun toString(): String = "[$className o=$orientation pt=$isPhasingTarget p=$isPhaseable]"
  }
}

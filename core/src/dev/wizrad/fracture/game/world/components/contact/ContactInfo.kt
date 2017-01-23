package dev.wizrad.fracture.game.world.components.contact

import dev.wizrad.fracture.support.className

sealed class ContactInfo {
  // MARK: Orientation
  enum class Orientation {
    Bottom, Left, Top, Right;

    val isTop: Boolean get() = this == Top
    val isBottom: Boolean get() = this == Bottom
    val isLeft: Boolean get() = this == Left
    val isRight: Boolean get() = this == Right
  }

  // MARK: Actors
  class Hero(
    var isPhasing: Boolean = false): ContactInfo() {
    override fun toString(): String = "[$className p=$isPhasing]"
  }

  class Appendage(
    val orientation: Orientation): ContactInfo() {
    override fun toString(): String = "[$className o=$orientation]"
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

package dev.wizrad.fracture.game.world.components.session

import dev.wizrad.fracture.game.world.components.loader.LevelData
import dev.wizrad.fracture.game.world.core.Entity

sealed class Event {
  // MARK: Level
  class LevelStarted(
    val level: Entity,
    var start: LevelData.Feature): Event()

  class LevelFinished: Event()
  class LevelFailed: Event()

  // MARK: Transition
  class TransitionStarted(
    val level: Entity,
    val start: LevelData.Feature,
    val duration: Float): Event()

  class TransitionFinished()
}

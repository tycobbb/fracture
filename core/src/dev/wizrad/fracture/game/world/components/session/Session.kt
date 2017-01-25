package dev.wizrad.fracture.game.world.components.session

class Session {
  // MARK: Properties
  private var currentLevel = -1
  private var levelsFinished = 0

  // MARK: Screen Progress
  val justFinishedLevel: Boolean get()
    = levelsFinished > currentLevel

  fun startLevel() {
    currentLevel++
  }

  fun finishLevel() {
    levelsFinished++
  }
}

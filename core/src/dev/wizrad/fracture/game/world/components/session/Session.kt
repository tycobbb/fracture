package dev.wizrad.fracture.game.world.components.session

import com.badlogic.gdx.utils.Timer
import dev.wizrad.fracture.game.world.components.loader.LevelData
import dev.wizrad.fracture.game.world.components.loader.Loader
import dev.wizrad.fracture.game.world.core.Entity

class Session {
  // MARK: Dependencies
  private val bus = Bus()
  private val loader = Loader()

  // MARK: Properties
  private var currentLevel = -1
  private var levelsFinished = 0
  private var nextLevelData: LevelData? = null

  // MARK: Actions
  fun startLevel(level: Entity) {
    val data = checkNotNull(nextLevelData)

    nextLevelData = null
    currentLevel++

    bus.post(Event.LevelStarted(
      level = level,
      start = data.hotspots.start
    ))
  }

  fun finishLevel() {
    levelsFinished++
    bus.post(Event.LevelFinished())
  }

  fun failLevel() {
    currentLevel = -1
    levelsFinished = 0
    bus.post(Event.LevelFailed())
  }

  fun loadLevel(): LevelData {
    val data = loader.load()
    nextLevelData = data
    return data
  }

  fun startTransition(level: Entity) {
    val data = checkNotNull(nextLevelData)

    val duration = 2.0f
    bus.post(Event.TransitionStarted(
      level = level,
      start = data.hotspots.start,
      duration = 2.0f
    ))

    Timer.schedule(object: Timer.Task() {
      override fun run() {
        finishTransition()
      }
    }, 2.0f)
  }

  private fun finishTransition() {
    bus.post(Event.TransitionFinished())
  }

  // MARK: Pub/Sub
  fun subscribe(subscriber: Pair<Class<*>, Function1<*, Unit>>): () -> Unit {
    return bus.subscribe(subscriber)
  }

  fun subscribe(subscribers: Array<out Pair<Class<*>, Function1<*, Unit>>>): () -> Unit {
    return bus.subscribe(subscribers)
  }
}

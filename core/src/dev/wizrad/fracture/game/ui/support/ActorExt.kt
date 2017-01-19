package dev.wizrad.fracture.game.ui.support

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent

fun Actor.setPosition(vector: Vector2) {
  setPosition(vector.x, vector.y)
}

fun Actor.onChange(handler: (event: ChangeEvent, actor: Actor) -> Unit): () -> Unit {
  val listener = object: ChangeListener() {
    override fun changed(event: ChangeEvent?, actor: Actor?) {
      if (event != null && actor != null) {
        handler(event, actor)
      }
    }
  }

  addListener(listener)

  return {
    removeListener(listener)
  }
}

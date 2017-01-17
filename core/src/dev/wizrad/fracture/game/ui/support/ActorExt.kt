package dev.wizrad.fracture.game.ui.support

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor

fun Actor.setPosition(vector: Vector2) {
  setPosition(vector.x, vector.y)
}

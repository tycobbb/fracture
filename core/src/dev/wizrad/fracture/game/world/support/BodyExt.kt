package dev.wizrad.fracture.game.world.support

import com.badlogic.gdx.physics.box2d.Body

fun Body.applyImpulseToCenter(x: Float, y: Float) {
  val center = worldCenter
  applyLinearImpulse(x, y, center.x, center.y, true)
}

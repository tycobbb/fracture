package dev.wizrad.fracture.game.world.support

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body

fun Body.cancelMomentum() {
  linearVelocity = Vector2.Zero
}

fun Body.applyImpulseToCenter(x: Float, y: Float) {
  val center = worldCenter
  applyLinearImpulse(x, y, center.x, center.y, true)
}

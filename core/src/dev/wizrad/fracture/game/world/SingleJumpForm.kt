package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import dev.wizrad.fracture.game.components.controls.Key
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class SingleJumpForm(
  val body: Body,
  val w: World): Form {

  // MARK: Lifecycle
  override fun update(delta: Float) {
    // move if necessary
    val force = Vector2()
    if(w.controls.pressed(Key.Left)) {
      force.x -= 20.0f
    }

    if(w.controls.pressed(Key.Right)) {
      force.x += 20.0f
    }

    body.applyForceToCenter(force, true)

    // jump if possible
    if(w.controls.pressed(Key.Jump) && canJump()) {
      debug(Tag.Physics, "jumping")
      val center = body.worldCenter
      body.applyLinearImpulse(0.0f, 30.0f, center.x, center.y, true)
    }
  }

  override fun step(delta: Float) {
  }

  override fun destroy() {
  }

  // MARK: Jumping
  private fun canJump(): Boolean {
    val fixture = body.fixtureList.firstOrNull() ?: return false
    return w.contacts.count(fixture) != 0
  }
}

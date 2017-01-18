package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import dev.wizrad.fracture.game.components.controls.Key
import dev.wizrad.fracture.game.world.core.Behavior
import dev.wizrad.fracture.game.world.core.State
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class SingleJumpForm(val body: Body, val w: World): Behavior(), Form {
  // MARK: Form
  override val type: Form.Type get() = Form.Type.SingleJump
  override val behavior: Behavior get() = this

  // MARK: Properties
  private var state: State = Standing(body, w)

  // MARK: Lifecycle
  override fun update(delta: Float) {
    super.update(delta)

    state.update(delta)
    state.nextState()?.let {
      state = it
    }
  }

  // MARK: States
  private class Standing(
    val body: Body,
    val w: World): State() {

    private fun canJump(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      val contactCount = w.contacts.count(body.fixtureList.first())
      return contactCount != 0
    }

    override fun nextState(): State? {
      if (w.controls.pressed(Key.Jump) && canJump()) {
        return Windup(body, w)
      }

      return null
    }
  }

  private class Windup(
    val body: Body,
    val w: World): State() {

    override fun nextState(): State? {
      if (!w.controls.pressed(Key.Jump)) {
        return Jumping(body, w, isShort = frames <= 4)
      }

      return null
    }
  }

  private class Jumping(
    val body: Body,
    val w: World,
    val isShort: Boolean): State() {

    override fun update(delta: Float) {
      super.update(delta)

      // jump on first frame
      if (frames == 1) {
        debug(Tag.Physics, "applying jump impulse")
        val center = body.worldCenter
        val magnitude = if (isShort) 30.0f else 60.0f
        body.applyLinearImpulse(0.0f, magnitude, center.x, center.y, true)
      }

      // apply directional influence
      val force = Vector2()
      if (w.controls.pressed(Key.Left)) {
        force.x -= 20.0f
      }

      if (w.controls.pressed(Key.Right)) {
        force.x += 20.0f
      }

      body.applyForceToCenter(force, true)
    }

    private fun didLand(): Boolean {
      assert(body.fixtureList.size != 0) { "body have at least one fixture" }
      val contactCount = w.contacts.count(body.fixtureList.first())
      return contactCount != 0
    }

    override fun nextState(): State? {
      if (didLand()) {
        return Standing(body, w)
      }

      return null
    }
  }
}

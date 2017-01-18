package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import dev.wizrad.fracture.game.components.controls.Key
import dev.wizrad.fracture.game.world.core.Behavior
import dev.wizrad.fracture.game.world.core.State
import dev.wizrad.fracture.game.world.core.StateMachine
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class SingleJumpForm(body: Body, w: World): Form {
  // MARK: Form
  override val type = Form.Type.SingleJump
  override val behavior: Behavior get() = states

  // MARK: Properties
  private val states = StateMachine(initialState = Standing(body, w))

  // MARK: States
  private class Standing(
    val body: Body,
    val w: World): State() {

    override fun update(delta: Float) {
      super.update(delta)

      // apply running movement
      val force = Vector2()
      if (w.controls.pressed(Key.Left)) {
        force.x -= 30.0f
      }

      if (w.controls.pressed(Key.Right)) {
        force.x += 30.0f
      }

      body.applyForceToCenter(force, true)
    }

    private fun canJump(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      val fixture = body.fixtureList.first()
      val contactCount = w.contacts.count(fixture)
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
        return JumpStart(body, w, isShort = frame <= 4)
      }

      return null
    }
  }

  private class JumpStart(
    val body: Body,
    val w: World,
    isShort: Boolean): State() {

    init {
      debug(Tag.World, "$this applying impulse!")
      val center = body.worldCenter
      val magnitude = if (isShort) 15.0f else 30.0f
      body.applyLinearImpulse(0.0f, -magnitude, center.x, center.y, true)
    }

    override fun nextState(): State? {
      val jumpStartFrames = 3
      return if (frame >= jumpStartFrames) Jumping(body, w) else null
    }
  }

  class Jumping(
    val body: Body,
    val w: World): State() {

    override fun update(delta: Float) {
      super.update(delta)

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
      val fixture = body.fixtureList.first()
      val contactCount = w.contacts.count(fixture)
      return contactCount != 0
    }

    override fun nextState(): State? {
      return if (didLand()) JumpEnd(body, w) else null
    }
  }

  class JumpEnd(
    val body: Body,
    val w: World): State() {

    override fun nextState(): State? {
      val jumpEndFrames = 3
      return if (frame >= jumpEndFrames) Standing(body, w) else null
    }
  }
}

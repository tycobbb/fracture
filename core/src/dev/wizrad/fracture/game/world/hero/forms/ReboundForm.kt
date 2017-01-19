package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import dev.wizrad.fracture.game.components.controls.Key
import dev.wizrad.fracture.game.world.core.State
import dev.wizrad.fracture.game.world.core.StateMachine
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class ReboundForm(
  private val body: Body,
  private val w: World): Form {

  // MARK: Form
  override val type = Form.Type.Rebound
  override val behavior = StateMachine(initialState = standing())

  // MARK: States
  private fun standing(): State = object: State() {
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

    override fun nextState(): State? {
      if (w.controls.pressed(Key.Jump) && canJump()) {
        return windup()
      }

      return null
    }

    private fun canJump(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      val fixture = body.fixtureList.first()
      val contactCount = w.contacts.count(fixture)
      return contactCount != 0
    }
  }

  private fun windup(): State = object: State() {
    override fun nextState(): State? {
      val frameLength = 4
      if (frame >= frameLength) {
        return jumpStart(isShort = !w.controls.pressed(Key.Jump))
      }

      return null
    }
  }

  private fun jumpStart(isShort: Boolean): State = object: State() {
    private val magnitude = if (isShort) 5.0f else 10.0f

    override fun start() {
      debug(Tag.World, "$this applying impulse: $magnitude")
      val center = body.worldCenter
      body.applyLinearImpulse(0.0f, -magnitude, center.x, center.y, true)
    }

    override fun nextState(): State? {
      val frameLength = 3
      return if (frame >= frameLength) jumping() else null
    }
  }

  private fun jumping(): State = object: State() {
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

    override fun nextState(): State? {
      if (didLand()) {
        return landing()
      } else if (w.controls.pressed(Key.Jump) && isFalling()) {
        return fastfalling()
      }

      return null
    }

    private fun didLand(): Boolean {
      assert(body.fixtureList.size != 0) { "body have at least one fixture" }
      val fixture = body.fixtureList.first()
      val contactCount = w.contacts.count(fixture)
      return contactCount != 0
    }

    private fun isFalling(): Boolean {
      return body.linearVelocity.y >= 0.0
    }
  }

  private fun fastfalling(): State = object: State() {
    override fun start() {
      debug(Tag.World, "$this applying fastfall impulse")
      val center = body.worldCenter
      val magnitude = 30.0f
      body.applyLinearImpulse(0.0f, magnitude, center.x, center.y, true)
    }

    override fun nextState(): State? {
      return null
    }
  }

  private fun landing(): State = object: State() {
    override fun nextState(): State? {
      val frameLength = 3
      return if (frame >= frameLength) standing() else null
    }
  }
}

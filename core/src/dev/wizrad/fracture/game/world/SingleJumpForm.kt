package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import dev.wizrad.fracture.game.components.controls.Key
import dev.wizrad.fracture.game.world.core.State
import dev.wizrad.fracture.game.world.core.StateMachine
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class SingleJumpForm(val body: Body, val w: World): Form {
  override val type = Form.Type.SingleJump
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

    private fun canJump(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      val fixture = body.fixtureList.first()
      val contactCount = w.contacts.count(fixture)
      return contactCount != 0
    }

    override fun nextState(): State? {
      if (w.controls.pressed(Key.Jump) && canJump()) {
        return windup()
      }

      return null
    }
  }

  private fun windup(): State = object: State() {
    override fun nextState(): State? {
      if (!w.controls.pressed(Key.Jump)) {
        val shortJumpFrameLength = 4
        return jumpStart(isShort = frame <= shortJumpFrameLength)
      }

      return null
    }
  }

  private fun jumpStart(isShort: Boolean): State = object: State() {
    private val magnitude = if (isShort) 15.0f else 30.0f

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

    private fun didLand(): Boolean {
      assert(body.fixtureList.size != 0) { "body have at least one fixture" }
      val fixture = body.fixtureList.first()
      val contactCount = w.contacts.count(fixture)
      return contactCount != 0
    }

    override fun nextState(): State? {
      return if (didLand()) jumpEnd() else null
    }
  }

  fun jumpEnd(): State = object: State() {
    override fun nextState(): State? {
      val frameLength = 3
      return if (frame >= frameLength) standing() else null
    }
  }
}

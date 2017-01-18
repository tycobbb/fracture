package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import dev.wizrad.fracture.game.components.controls.Key
import dev.wizrad.fracture.game.world.core.State
import dev.wizrad.fracture.game.world.core.StateMachine
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class DoubleJumpForm(
  val body: Body,
  val w: World): Form {

  // MARK: Form
  override val type = Form.Type.DoubleJump
  override val behavior = StateMachine(initialState = standing())

  // MARK: Direction
  private enum class Direction { None, Left, Right }

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
    private val magnitude = if (isShort) 10.0f else 20.0f

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
        return windup2()
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

  private fun windup2(): State = object: State() {
    override fun nextState(): State? {
      val frameLength = 4
      if (frame >= frameLength) {
        return jumpStart2(isShort = !w.controls.pressed(Key.Jump), direction = inputDirection())
      }

      return null
    }

    private fun inputDirection(): Direction {
      val leftPressed = w.controls.pressed(Key.Left)
      val rightPressed = w.controls.pressed(Key.Right)

      return when {
        leftPressed && !rightPressed -> Direction.Left
        !leftPressed && rightPressed -> Direction.Right
        else -> Direction.None
      }
    }
  }

  private fun jumpStart2(isShort: Boolean, direction: Direction): State = object: State() {
    override fun start() {
      if (direction != Direction.None && initialDirection() != direction) {
        debug(Tag.World, "$this canceling horizontal momentum")
        val velocity = body.linearVelocity
        body.setLinearVelocity(0.0f, velocity.y)
      }

      debug(Tag.World, "$this applying impulse")
      val center = body.worldCenter
      val magnitude = if (isShort) 20.0f else 30.0f
      body.applyLinearImpulse(0.0f, -magnitude, center.x, center.y, true)
    }

    override fun nextState(): State? {
      val frameLength = 3
      return if (frame >= frameLength) jumping2() else null
    }

    private fun initialDirection(): Direction {
      val velocity = body.linearVelocity

      return when {
        velocity.x < 0.0 -> Direction.Right
        velocity.x > 0.0 -> Direction.Right
        else -> Direction.None
      }
    }
  }

  private fun jumping2(): State = object: State() {
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
      return if (didLand()) landing() else null
    }

    private fun didLand(): Boolean {
      assert(body.fixtureList.size != 0) { "body have at least one fixture" }
      val fixture = body.fixtureList.first()
      val contactCount = w.contacts.count(fixture)
      return contactCount != 0
    }
  }

  private fun landing(): State = object: State() {
    override fun nextState(): State? {
      val frameLength = 3
      return if (frame >= frameLength) standing() else null
    }
  }
}


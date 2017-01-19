package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.components.statemachine.StateMachine
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class SpaceJumpForm(
  private val body: Body,
  private val world: World): Form {

  // MARK: Form
  override val type = Form.Type.SpaceJump
  override val behavior = StateMachine(initialState = standing())

  override fun defineFixtures(size: Vector2) {
    // create fixtures
    val square = PolygonShape()
    square.setAsBox(size.x, size.y)

    val fixture = FixtureDef()
    fixture.shape = square
    fixture.density = 1.0f
    fixture.friction = 0.2f
    fixture.filter.categoryBits = ContactType.Hero.bits

    body.createFixture(fixture)

    // dispose shapes
    square.dispose()
  }

  // MARK: Direction
  private enum class Direction { None, Left, Right }

  // MARK: States
  private fun standing(): State = object: State() {
    override fun update(delta: Float) {
      super.update(delta)

      // apply running movement
      val force = Vector2()
      if (world.controls.left.isPressed) {
        force.x -= 30.0f
      }

      if (world.controls.right.isPressed) {
        force.x += 30.0f
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      if (world.controls.jump.isPressedUnique && canJump()) {
        return windup()
      }

      return null
    }

    private fun canJump(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return world.contact.exists(body.fixtureList.first(), ContactType.Ground)
    }
  }

  private fun windup(): State = object: State() {
    override fun nextState(): State? {
      val frameLength = 4
      if (frame >= frameLength) {
        return jumpStart(isShort = !world.controls.jump.isPressed)
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
    private var canJump: Boolean = false

    override fun update(delta: Float) {
      super.update(delta)

      if (!canJump && isFalling()) {
        canJump = true
        world.controls.jump.requireUniquePress()
      }

      // apply directional influence
      val force = Vector2()
      if (world.controls.left.isPressed) {
        force.x -= 20.0f
      }

      if (world.controls.right.isPressed) {
        force.x += 20.0f
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      if (didLand()) {
        return landing()
      } else if (world.controls.jump.isPressedUnique && canJump) {
        return windup2()
      }

      return null
    }

    private fun didLand(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return world.contact.exists(body.fixtureList.first(), ContactType.Ground)
    }

    private fun isFalling(): Boolean {
      return body.linearVelocity.y >= 0.0
    }
  }

  private fun windup2(): State = object: State() {
    override fun nextState(): State? {
      val frameLength = 4
      if (frame >= frameLength) {
        return jumpStart2(isShort = !world.controls.jump.isPressed, direction = inputDirection())
      }

      return null
    }

    private fun inputDirection(): Direction {
      val leftPressed = world.controls.left.isPressed
      val rightPressed = world.controls.right.isPressed

      return when {
        leftPressed && !rightPressed -> Direction.Left
        !leftPressed && rightPressed -> Direction.Right
        else -> Direction.None
      }
    }
  }

  private fun jumpStart2(isShort: Boolean, direction: Direction): State = object: State() {
    override fun start() {
      // cancel vertical momentum
      val velocity = body.linearVelocity
      velocity.y = 0.0f

      // cancel horizontal momentum if direction is changing
      if (direction != Direction.None && initialDirection() != direction) {
        debug(Tag.World, "$this canceling horizontal momentum")
        velocity.x = 0.0f
      }

      body.linearVelocity = velocity

      // apply the space jump impulse
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
        velocity.x < 0.0 -> Direction.Left
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
      if (world.controls.left.isPressed) {
        force.x -= 20.0f
      }

      if (world.controls.right.isPressed) {
        force.x += 20.0f
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      return if (didLand()) landing() else null
    }

    private fun didLand(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return world.contact.exists(body.fixtureList.first(), ContactType.Ground)
    }
  }

  private fun landing(): State = object: State() {
    override fun start() {
      super.start()
      world.controls.jump.requireUniquePress()
    }

    override fun nextState(): State? {
      val frameLength = 3
      return if (frame >= frameLength) standing() else null
    }
  }
}


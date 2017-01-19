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

class SpearForm(
  private val body: Body,
  private val world: World): Form {

  override val type = Form.Type.Spear
  override val behavior = StateMachine(initialState = standing())

  override fun defineFixtures(size: Vector2) {
    // create fixtures
    val square = PolygonShape()
    square.setAsBox(size.x, size.y)

    val fixture = FixtureDef()
    fixture.shape = square
    fixture.density = 1.0f
    fixture.friction = 1.0f
    fixture.filter.categoryBits = ContactType.Hero.bits

    body.createFixture(fixture)

    // dispose shapes
    square.dispose()
  }

  // MARK: Direction
  private enum class Direction { None, Left, Right }

  // MARK: States
  private fun standing(): State = object: State() {
    override fun start() {
      super.start()
      world.controls.left.requireUniquePress()
      world.controls.right.requireUniquePress()
    }

    override fun nextState(): State? {
      val direction = inputDirection()
      return if (direction != Direction.None) prepare(direction) else null
    }

    private fun inputDirection(): Direction {
      val leftPressed = world.controls.left.isPressedUnique
      val rightPressed = world.controls.right.isPressedUnique

      return when {
        leftPressed && !rightPressed -> Direction.Left
        !leftPressed && rightPressed -> Direction.Right
        else -> Direction.None
      }
    }
  }

  private fun prepare(direction: Direction) = object: State() {
    private var preparedFrames: Int = 0
    private var velocityX = if (direction == Direction.Left) -2.0f else 2.0f

    override fun update(delta: Float) {
      super.update(delta)

      val velocity = body.linearVelocity
      val nextDirection = inputDirection()

      if (nextDirection == direction) {
        preparedFrames++
        velocity.x = velocityX
      } else if (nextDirection != Direction.None) {
        preparedFrames--
        if (preparedFrames >= 0) {
          velocity.x = -velocityX
        }
      } else {
        velocity.x = 0.0f
      }

      body.linearVelocity = velocity
    }

    override fun nextState(): State? {
      val frameLength = 20

      if (preparedFrames >= frameLength) {
        return ready(direction)
      } else if (preparedFrames < 0) {
        return standing()
      } else {
        return null
      }
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

  private fun ready(direction: Direction): State = object: State() {
    override fun start() {
      super.start()

      world.controls.left.requireUniquePress()
      world.controls.right.requireUniquePress()

      // cancel horizontal momentum
      val velocity = body.linearVelocity
      velocity.x = 0.0f
      body.linearVelocity = velocity
    }

    override fun nextState(): State? {
      if (world.controls.jump.isPressedUnique) {
        return windup(direction)
      }

      val nextDirection = inputDirection()
      if (nextDirection != Direction.None && nextDirection != direction) {
        return standing()
      }

      return null
    }

    private fun inputDirection(): Direction {
      val leftPressed = world.controls.left.isPressedUnique
      val rightPressed = world.controls.right.isPressedUnique

      return when {
        leftPressed && !rightPressed -> Direction.Left
        !leftPressed && rightPressed -> Direction.Right
        else -> Direction.None
      }
    }
  }

  private fun windup(direction: Direction): State = object: State() {
    override fun nextState(): State? {
      val frameLength = 4
      if (frame >= frameLength) {
        return jumpStart(direction = direction, isShort = !world.controls.jump.isPressed)
      }

      return null
    }
  }

  private fun jumpStart(direction: Direction, isShort: Boolean): State = object: State() {
    private val magnitude = if (isShort) 15.0f else 20.0f

    override fun start() {
      debug(Tag.World, "$this applying impulse: $magnitude")
      val center = body.worldCenter
      val cartesianDirection = if (direction == Direction.Left) -1.0f else 1.0f
      body.applyLinearImpulse(cartesianDirection * magnitude, -magnitude, center.x, center.y, true)
    }

    override fun nextState(): State? {
      val frameLength = 3
      return if (frame >= frameLength) jumping() else null
    }
  }

  private fun jumping(): State = object: State() {
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

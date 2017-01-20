package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class SpearForm(
  context: State.Context): Form(initialState = Standing(context)) {

  // MARK: Properties
  private val body = context.body

  // MARK: Form
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

  // MARK: Direction / Orientation
  enum class Direction { None, Left, Right }
  enum class Orientation { Bottom, Left, Top, Right }

  // MARK: States
  class Standing(context: Context): State(context) {
    override fun start() {
      super.start()
      world.controls.left.requireUniquePress()
      world.controls.right.requireUniquePress()
    }

    override fun nextState(): State? {
      val direction = inputDirection()

      return when (direction) {
        Direction.None -> null
        else -> Prepare(context, direction)
      }
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

  class Prepare(context: Context, val direction: Direction): State(context) {
    private val frameLength = 20
    private val velocityX = if (direction == Direction.Left) -2.0f else 2.0f

    private var preparedFrames: Int = 0

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
      if (preparedFrames >= frameLength) {
        return Ready(context, direction)
      } else if (preparedFrames < 0) {
        return Standing(context)
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

  class Ready(context: Context, val direction: Direction): State(context) {
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
        return Windup(context, direction)
      }

      val nextDirection = inputDirection()
      if (nextDirection != Direction.None && nextDirection != direction) {
        return Standing(context)
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

  class Windup(context: Context, val direction: Direction): State(context) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(context, direction, isShort = !world.controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(context: Context, direction: Direction, isShort: Boolean): State(context) {
    private val frameLength = 3
    private val magnitude = if (isShort) 15.0f else 20.0f
    private val cartesianDirection = if (direction == Direction.Left) -1.0f else 1.0f

    override fun start() {
      debug(Tag.World, "$this applying impulse: $magnitude")
      val center = body.worldCenter
      body.applyLinearImpulse(cartesianDirection * magnitude, -magnitude, center.x, center.y, true)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): State(context) {
    override fun nextState(): State? {
      return if (landingOrientation() != null) Landing(context) else null
    }

    private fun landingOrientation(): Orientation? {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      val hasContact = world.contact.exists(body.fixtureList.first(), ContactInfo.Bottom)
      return if (hasContact) Orientation.Bottom else null
    }
  }

  class Landing(context: Context): State(context) {
    private val frameLength = 3

    override fun start() {
      super.start()

      world.controls.jump.requireUniquePress()

      // cancel horizontal momentum on land
      val velocity = body.linearVelocity
      velocity.x = 0.0f
      body.linearVelocity = velocity
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Standing(context) else null
    }
  }
}

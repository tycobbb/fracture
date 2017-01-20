package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.support.applyImpulseToCenter
import dev.wizrad.fracture.game.world.support.cancelMomentum
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class SpearForm(context: Context): Form(context) {
  // MARK: Behavior
  override fun start() {
    super.start()
    body.gravityScale = 0.0f
  }

  override fun destroy() {
    body.gravityScale = 1.0f
    super.destroy()
  }

  // MARK: Form
  override fun initialState(): State {
    return Standing(context, Orientation.Top)
  }

  override fun defineFixtures(size: Vector2) {
    // create fixtures
    val width = size.x / 2
    val height = size.y / 2
    val square = PolygonShape()
    square.setAsBox(width, height, Vector2(width, height), 0.0f)

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

  // MARK: States
  class Standing(
    context: Context,
    private val orientation: Orientation): FormState(context) {

    override fun start() {
      super.start()

      controls.left.requireUniquePress()
      controls.right.requireUniquePress()
    }

    override fun nextState(): State? {
      val direction = inputDirection()

      return when (direction) {
        Direction.None -> null
        else -> Prepare(context, orientation, direction)
      }
    }

    private fun inputDirection(): Direction {
      val leftPressed = controls.left.isPressedUnique
      val rightPressed = controls.right.isPressedUnique

      return when {
        leftPressed && !rightPressed -> Direction.Left
        !leftPressed && rightPressed -> Direction.Right
        else -> Direction.None
      }
    }
  }

  class Prepare(
    context: Context,
    private val orientation: Orientation,
    private val direction: Direction): FormState(context) {

    private val frameLength = 20
    private val velocityMagnitude = if (direction == Direction.Left) -2.0f else 2.0f
    private var preparedFrames: Int = 0

    override fun update(delta: Float) {
      super.update(delta)

      var velocity = 0.0f
      val nextDirection = inputDirection()

      // move towards ready state if same direction, backwards otherwise
      if (nextDirection == direction) {
        preparedFrames++
        velocity = velocityMagnitude
      } else if (nextDirection != Direction.None) {
        preparedFrames--
        if (preparedFrames >= 0) {
          velocity = -velocityMagnitude
        }
      }

      // update correct velocity component according to orientation
      when (orientation) {
        Orientation.Bottom, Orientation.Top -> body.setLinearVelocity(velocity, 0.0f)
        Orientation.Left -> body.setLinearVelocity(0.0f, -velocity)
        Orientation.Right -> body.setLinearVelocity(0.0f, velocity)
      }
    }

    override fun nextState(): State? {
      if (preparedFrames >= frameLength) {
        return Ready(context, orientation, direction)
      } else if (preparedFrames < 0) {
        return Standing(context, orientation)
      } else {
        return null
      }
    }

    private fun inputDirection(): Direction {
      val leftPressed = controls.left.isPressed
      val rightPressed = controls.right.isPressed

      return when {
        leftPressed && !rightPressed -> Direction.Left
        !leftPressed && rightPressed -> Direction.Right
        else -> Direction.None
      }
    }
  }

  class Ready(
    context: Context,
    val orientation: Orientation,
    val direction: Direction): FormState(context) {

    override fun start() {
      super.start()

      controls.left.requireUniquePress()
      controls.right.requireUniquePress()

      // cancel all momentum
      body.cancelMomentum()
    }

    override fun nextState(): State? {
      if (controls.jump.isPressedUnique) {
        return Windup(context, orientation, direction)
      }

      inputDirection().let {
        if (it != Direction.None && it != direction) {
          return Standing(context, orientation)
        }
      }

      return null
    }

    private fun inputDirection(): Direction {
      val leftPressed = controls.left.isPressedUnique
      val rightPressed = controls.right.isPressedUnique

      return when {
        leftPressed && !rightPressed -> Direction.Left
        !leftPressed && rightPressed -> Direction.Right
        else -> Direction.None
      }
    }
  }

  class Windup(
    context: Context,
    val orientation: Orientation,
    val direction: Direction): FormState(context) {

    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        val isShort = !controls.jump.isPressed
        return JumpStart(context, orientation, direction, isShort)
      }

      return null
    }
  }

  class JumpStart(
    context: Context,
    val orientation: Orientation,
    direction: Direction,
    isShort: Boolean): FormState(context) {

    private val frameLength = 3
    private val magnitude = if (isShort) 3.75f else 5.0f
    private val cartesianDirection = if (direction == Direction.Left) -1.0f else 1.0f

    override fun start() {
      body.gravityScale = 1.0f

      // apply impulse according to orientation
      debug(Tag.World, "$this applying impulse: $magnitude")
      val directedImpulse = cartesianDirection * magnitude
      when (orientation) {
        Orientation.Top -> body.applyImpulseToCenter(directedImpulse, -magnitude)
        Orientation.Bottom -> body.applyImpulseToCenter(directedImpulse, magnitude)
        Orientation.Left -> body.applyImpulseToCenter(-magnitude, -directedImpulse)
        Orientation.Right -> body.applyImpulseToCenter(magnitude, directedImpulse)
      }
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): FormState(context) {
    override fun nextState(): State? {
      val orientation = landingOrientation()
      return if (orientation != null) Landing(context, orientation) else null
    }

    private fun landingOrientation(): Orientation? {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return contact.first(body.fixtureList.first())?.orientation
    }
  }

  class Landing(context: Context, val orientation: Orientation): FormState(context) {
    private val frameLength = 3

    override fun start() {
      super.start()

      body.gravityScale = 0.0f
      body.cancelMomentum()

      controls.jump.requireUniquePress()
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Standing(context, orientation) else null
    }
  }
}

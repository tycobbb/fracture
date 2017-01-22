package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.support.applyImpulseToCenter
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
    val square = PolygonShape()
    square.setAsBox(size.x / 2, size.y / 2)

    val fixture = FixtureDef()
    fixture.shape = square
    fixture.density = 1.0f
    fixture.friction = 1.0f
    fixture.filter.categoryBits = ContactType.Hero.bits

    body.createFixture(fixture)

    // dispose shapes
    square.dispose()
  }

  // MARK: States
  class Standing(context: Context, orientation: Orientation): FormState(context) {
    private val orientation = orientation

    override fun start() {
      super.start()
      requireUniqueMovement()
    }

    override fun nextState(): State? {
      val direction = inputDirection(isUniqueInput = true)

      return when (direction) {
        Direction.None -> null
        else -> Prepare(context, orientation, direction)
      }
    }
  }

  class Prepare(
    context: Context, orientation: Orientation, direction: Direction): FormState(context) {

    private val orientation = orientation
    private val direction = direction

    private val frameLength = 20
    private val velocityMag = if (direction == Direction.Left) -2.0f else 2.0f
    private var preparedFrames: Int = 0

    override fun step(delta: Float) {
      super.step(delta)

      var velocity = 0.0f
      val nextDirection = inputDirection()

      // move towards ready state if same direction, backwards otherwise
      if (nextDirection == direction) {
        preparedFrames++
        velocity = velocityMag
      } else if (nextDirection != Direction.None) {
        preparedFrames--
        if (preparedFrames >= 0) {
          velocity = -velocityMag
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
  }

  class Ready(
    context: Context, orientation: Orientation, direction: Direction): FormState(context) {

    private val orientation = orientation
    private val direction = direction

    override fun start() {
      super.start()
      requireUniqueMovement()
      cancelMomentum()
    }

    override fun nextState(): State? {
      if (controls.jump.isPressedUnique) {
        return Windup(context, orientation, direction)
      }

      val inputDirection = inputDirection(isUniqueInput =  true)
      if (inputDirection != Direction.None && inputDirection != direction) {
        return Standing(context, orientation)
      }

      return null
    }
  }

  class Windup(
    context: Context, orientation: Orientation, direction: Direction): FormState(context) {

    private val orientation = orientation
    private val direction = direction
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(context, orientation, direction, !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(
    context: Context, orientation: Orientation, direction: Direction, isShort: Boolean): FormState(context) {

    private val orientation = orientation
    private val frameLength = 3
    private val jumpMag = if (isShort) 3.75f else 5.0f
    private val cartesianDirection = if (direction == Direction.Left) -1.0f else 1.0f

    override fun start() {
      super.start()

      body.gravityScale = 1.0f

      // apply impulse according to orientation
      debug(Tag.World, "$this applying impulse: $jumpMag")
      val directedImpulse = cartesianDirection * jumpMag
      when (orientation) {
        Orientation.Top -> body.applyImpulseToCenter(directedImpulse, -jumpMag)
        Orientation.Bottom -> body.applyImpulseToCenter(directedImpulse, jumpMag)
        Orientation.Left -> body.applyImpulseToCenter(-jumpMag, -directedImpulse)
        Orientation.Right -> body.applyImpulseToCenter(jumpMag, directedImpulse)
      }
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): FormState(context) {
    override fun nextState(): State? {
      val orientation = contactOrientation()
      return if (orientation != null) Landing(context, orientation) else null
    }
  }

  class Landing(context: Context, orientation: Orientation): FormState(context) {
    private val orientation = orientation
    private val frameLength = 3

    override fun start() {
      super.start()

      body.gravityScale = 0.0f
      cancelMomentum()
      requireUniqueJump()
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Standing(context, orientation) else null
    }
  }
}

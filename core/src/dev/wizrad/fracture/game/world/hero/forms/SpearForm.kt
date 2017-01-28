package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Direction
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState
import dev.wizrad.fracture.game.world.support.extensions.applyImpulseToCenter
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class SpearForm(hero: Hero): Form(hero) {
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
    return Standing(this, Orientation.Top)
  }

  override fun defineFixtures() {
    val polygon = PolygonShape()

    // create fixtures
    createBox(defineBox(polygon))

    // dispose shapes
    polygon.dispose()
  }

  // MARK: States
  class Standing(
    form: SpearForm, orientation: Orientation): FormState<SpearForm>(form) {

    private val orientation = orientation

    override fun start() {
      super.start()
      requireUniqueMovement()
    }

    override fun nextState(): State? {
      val direction = inputDirection(isUniqueInput = true)
      return when (direction) {
        Direction.None -> null
        else -> Prepare(form, orientation, direction)
      }
    }
  }

  class Prepare(
    form: SpearForm, orientation: Orientation, direction: Direction): FormState<SpearForm>(form) {

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
        return Ready(form, orientation, direction)
      } else if (preparedFrames < 0) {
        return Standing(form, orientation)
      } else {
        return null
      }
    }
  }

  class Ready(
    form: SpearForm, orientation: Orientation, direction: Direction): FormState<SpearForm>(form) {

    private val orientation = orientation
    private val direction = direction

    override fun start() {
      super.start()
      requireUniqueMovement()
      cancelMomentum()
    }

    override fun nextState(): State? {
      if (controls.jump.isPressedUnique) {
        return Windup(form, orientation, direction)
      }

      val inputDirection = inputDirection(isUniqueInput =  true)
      if (inputDirection != Direction.None && inputDirection != direction) {
        return Standing(form, orientation)
      }

      return null
    }
  }

  class Windup(
    form: SpearForm, orientation: Orientation, direction: Direction): FormState<SpearForm>(form) {

    private val orientation = orientation
    private val direction = direction
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(form, orientation, direction, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(
    form: SpearForm, orientation: Orientation, direction: Direction, isShort: Boolean): FormState<SpearForm>(form) {

    private val orientation = orientation
    private val frameLength = 3
    private val jumpMag = if (isShort) 3.75f else 5.0f
    private val cartesianDirection = if (direction == Direction.Left) -1.0f else 1.0f

    override fun start() {
      super.start()
      startGravity()

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
      return if (frame >= frameLength) Jumping(form) else null
    }
  }

  class Jumping(form: SpearForm): FormState<SpearForm>(form) {
    override fun nextState(): State? {
      val orientation = currentContactOrientation()
      return if (orientation != null) Landing(form, orientation) else null
    }
  }

  class Landing(
    form: SpearForm, orientation: Orientation): FormState<SpearForm>(form) {

    private val orientation = orientation
    private val frameLength = 3

    override fun start() {
      super.start()

      stopGravity()
      cancelMomentum()
      requireUniqueJump()
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Standing(form, orientation) else null
    }
  }
}

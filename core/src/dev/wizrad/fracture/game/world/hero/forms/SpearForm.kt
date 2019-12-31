package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Direction
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormContext
import dev.wizrad.fracture.game.world.hero.core.FormState
import dev.wizrad.fracture.game.world.hero.forms.SpearForm.Context
import dev.wizrad.fracture.game.world.support.extensions.applyImpulseToCenter
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class SpearForm(context: Context): Form<Context>(context) {
  class Context(
    override val hero: Hero): FormContext

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

  override fun defineFixtures() {
    val polygon = PolygonShape()

    // create fixtures
    createBox(defineBox(polygon))

    // dispose shapes
    polygon.dispose()
  }

  // MARK: States
  class Standing(
    context: Context, orientation: Orientation): FormState<Context>(context) {

    private val orientation = orientation

    override fun start() {
      super.start()
      requireUniqueMovement()
    }

    override fun nextState() =
      inputDirectionOrNull(isUniqueInput = true)?.let {
        Prepare(context, orientation, it)
      }
  }

  class Prepare(
    context: Context, orientation: Orientation, direction: Direction): FormState<Context>(context) {

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

    override fun nextState() = when {
      preparedFrames >= frameLength ->
        Ready(context, orientation, direction)
      preparedFrames < 0 ->
        Standing(context, orientation)
      else -> null
    }
  }

  class Ready(
    context: Context, orientation: Orientation, direction: Direction): FormState<Context>(context) {

    private val orientation = orientation
    private val direction = direction

    override fun start() {
      super.start()
      requireUniqueMovement()
      cancelMomentum()
    }

    override fun nextState() = when {
      controls.jump.isPressedUnique ->
        Windup(context, orientation, direction)
      else -> inputDirectionOrNull(isUniqueInput = true)?.let {
        if (it != direction) Standing(context, orientation) else null
      }
    }
  }

  class Windup(
    context: Context, orientation: Orientation, direction: Direction): FormState<Context>(context) {

    private val orientation = orientation
    private val direction = direction
    private val frameLength = 4

    override fun nextState() = when {
      frame >= frameLength ->
        JumpStart(context, orientation, direction, isShort = !controls.jump.isPressed)
      else -> null
    }
  }

  class JumpStart(
    context: Context, orientation: Orientation, direction: Direction, isShort: Boolean): FormState<Context>(context) {

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

    override fun nextState() = when {
      frame >= frameLength ->
        Jumping(context)
      else -> null
    }
  }

  class Jumping(context: Context): FormState<Context>(context) {
    override fun nextState() =
      currentContactOrientation()?.let { Landing(context, orientation = it) }
  }

  class Landing(
    context: Context, orientation: Orientation): FormState<Context>(context) {

    private val orientation = orientation
    private val frameLength = 3

    override fun start() {
      super.start()

      stopGravity()
      cancelMomentum()
      requireUniqueJump()
    }

    override fun nextState() = when {
      frame >= frameLength ->
        Standing(context, orientation)
      else -> null
    }
  }
}

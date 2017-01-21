package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.Body
import dev.wizrad.fracture.game.components.controls.Controls
import dev.wizrad.fracture.game.world.components.contact.ContactGraph
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.support.applyImpulseToCenter
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

abstract class FormState(
  protected val context: Context): State() {

  // MARK: Properties
  /** The entity this state is attached to */
  protected val entity: Entity get() = context.parent!!
  /** The body of this state's attached entity */
  protected val body: Body get() = entity.body
  /** A reference to the world's shared controls */
  protected val controls: Controls get() = context.world.controls
  /** A reference to the world's shared contact graph */
  protected val contact: ContactGraph get() = context.world.contact

  // MARK: Direction
  enum class Direction { None, Left, Right }

  // MARK: Helpers- Input
  protected fun requireUniqueJump() {
    controls.jump.requireUniquePress()
  }

  protected fun requireUniqueMovement() {
    controls.left.requireUniquePress()
    controls.right.requireUniquePress()
  }

  protected fun inputDirection(isUniqueInput: Boolean = false): Direction {
    val leftPressed: Boolean
    val rightPressed: Boolean

    if (isUniqueInput) {
      leftPressed = controls.left.isPressedUnique
      rightPressed = controls.right.isPressedUnique
    } else {
      leftPressed = controls.left.isPressed
      rightPressed = controls.right.isPressed
    }

    return when {
      leftPressed && !rightPressed -> Direction.Left
      !leftPressed && rightPressed -> Direction.Right
      else -> Direction.None
    }
  }

  // MARK: Helpers - Checks
  protected fun isOnGround(): Boolean {
    assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
    return contact.oriented(body.fixtureList.first(), ContactInfo.Orientation.Top)
  }

  protected fun contactOrientation(): ContactInfo.Orientation? {
    assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
    return contact.closestSurface(body.fixtureList.first())?.orientation
  }

  protected fun currentDirection(): Direction {
    val velocity = body.linearVelocity
    return when {
      velocity.x < 0.0 -> Direction.Left
      velocity.x > 0.0 -> Direction.Right
      else -> Direction.None
    }
  }

  // MARK: Helpers - Physics
  protected fun applyJumpForce(magnitude: Float) {
    debug(Tag.World, "$this applying jump impulse: $magnitude")
    body.applyImpulseToCenter(0.0f, -magnitude)
  }

  protected fun applyFastfallImpulse(magnitude: Float) {
    debug(Tag.World, "$this applying fall impulse: $magnitude")
    body.applyImpulseToCenter(0.0f, magnitude)
  }

  protected fun applyMovementForce(magnitude: Float) {
    val force = scratch1.setZero()
    if (controls.left.isPressed) {
      force.x -= magnitude
    }

    if (controls.right.isPressed) {
      force.x += magnitude
    }

    body.applyForceToCenter(force, true)
  }

  protected fun cancelMomentum() {
    cancelComponentMomentum(x = true, y = true)
  }

  protected fun cancelComponentMomentum(x: Boolean = false, y: Boolean = false) {
    val velocity = body.linearVelocity

    if (x) {
      velocity.x = 0.0f
    }

    if (y) {
      velocity.y = 0.0f
    }

    body.linearVelocity = velocity
  }
}
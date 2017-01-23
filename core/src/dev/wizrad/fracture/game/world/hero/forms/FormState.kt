package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Fixture
import dev.wizrad.fracture.game.components.controls.Controls
import dev.wizrad.fracture.game.world.components.contact.ContactGraph
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.support.appendage
import dev.wizrad.fracture.game.world.support.applyImpulseToCenter
import dev.wizrad.fracture.game.world.support.hero
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.abs
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.extensions.Polar
import com.badlogic.gdx.physics.box2d.World as PhysicsWorld

abstract class FormState(
  protected val context: Context): State() {

  // MARK: Properties
  /** The entity this state is attached to */
  protected val entity: Entity get() = context.parent!!
  /** The body of this state's attached entity */
  protected val body: Body get() = entity.body
  /** A reference to the world's shared controls */
  protected val controls: Controls get() = context.world.controls
  /** A reference to the world's shared physics world */
  protected val physics: PhysicsWorld get() = context.world.physics
  /** A reference to the world's shared contact graph */
  protected val contact: ContactGraph get() = context.world.contact

  // MARK: Direction
  enum class Direction {
    None, Left, Right;

    val isNone: Boolean get() = this == None
    val isLeft: Boolean get() = this == Left
    val isRight: Boolean get() = this == Right
  }

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
  protected fun hasReachedApex(frameTimeout: Int = 10, velocityPeak: Float = -2.7f): Boolean {
    return frame > frameTimeout && body.linearVelocity.y > velocityPeak
  }

  protected fun isNearStationary(threshold: Float = 1.0f): Boolean {
    return body.linearVelocity.len2() <= threshold
  }

  protected fun isStopping(frameTimeout: Int = 10, threshold: Float = 1.0f): Boolean {
    return frame > frameTimeout && abs(body.linearVelocity.x) <= threshold
  }

  protected fun isOnGround(frameTimeout: Int = 0): Boolean {
    return if (frame < frameTimeout) {
      false
    } else {
      val foot = body.fixtureList.find { it.appendage != null } ?: error("body must have a appendage")
      contact.isOnSurface(fixture = foot, orientation = Orientation.Top)
    }
  }

  protected fun contactOrientation(): Orientation? {
    val hero = findFixture { it.hero != null }
    return contact.nearestSurface(hero)?.orientation
  }

  protected fun wallContactOrientation(): Orientation? {
    val left = findAppendage { it.orientation.isLeft }
    val leftContact = contact.nearestSurface(left) { it.orientation.isRight }

    val right = findAppendage { it.orientation.isRight }
    val rightContact = contact.nearestSurface(right) { it.orientation.isLeft }

    return when {
      leftContact != null && rightContact == null -> leftContact.orientation
      leftContact == null && rightContact != null -> rightContact.orientation
      else -> null
    }
  }

  protected fun currentDirection(vx: Float = body.linearVelocity.x): Direction {
    return when {
      vx < 0.0 -> Direction.Left
      vx > 0.0 -> Direction.Right
      else -> Direction.None
    }
  }

  private fun findFixture(filter: (Fixture) -> Boolean) =
    body.fixtureList.find(filter) ?: error("missing required fixture")

  private fun findAppendage(filter: (ContactInfo.Appendage) -> Boolean) =
    findFixture { fixture ->
      fixture.appendage?.let { filter(it) } ?: false
    }

  // MARK: Helpers - Physics
  protected fun applyJumpImpulse(magnitude: Float) {
    debug(Tag.World, "$this applying jump impulse: $magnitude")
    body.applyImpulseToCenter(0.0f, -magnitude)
  }

  protected fun applyFastfallImpulse(magnitude: Float) {
    debug(Tag.World, "$this applying fall impulse: $magnitude")
    body.applyImpulseToCenter(0.0f, magnitude)
  }

  protected fun applyImpulse(magnitude: Float, angle: Float) {
    debug(Tag.World, "$this applying angled impulse: $magnitude")
    val impulse = Polar.vector(magnitude = magnitude, angle = angle)
    body.applyImpulseToCenter(impulse.x, impulse.y)
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

  protected fun startGravity() {
    body.gravityScale = 1.0f
  }

  protected fun stopGravity() {
    body.gravityScale = 0.0f
  }


  protected fun startDamping(damping: Float) {
    body.linearDamping = damping
  }

  protected fun stopDamping() {
    body.linearDamping = 0.0f
  }
}
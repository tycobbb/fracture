package dev.wizrad.fracture.game.world.hero.core

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Fixture
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.components.contact.and
import dev.wizrad.fracture.game.world.components.contact.neq
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Scene
import dev.wizrad.fracture.game.world.core.SceneAware
import dev.wizrad.fracture.game.world.support.extensions.applyImpulseToCenter
import dev.wizrad.fracture.game.world.support.extensions.foot
import dev.wizrad.fracture.game.world.support.extensions.hero
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.abs
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.extensions.Polar
import com.badlogic.gdx.physics.box2d.World as PhysicsWorld

abstract class FormState<out F: Form>(
  form: F, scene: Scene = Scene.instance): State(), SceneAware {

  // MARK: SceneAware
  override val scene = scene

  // MARK: Properties
  protected val form: F = form
  protected val size: Vector2 = form.size
  protected val body: Body get() = form.body

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

  protected fun inputDirectionOrNull(isUniqueInput: Boolean = false): Direction? {
    val direction = inputDirection(isUniqueInput)
    return if (direction != Direction.None) direction else null
  }

  // MARK: Helpers - Checks
  protected fun hasReachedApex(frameTimeout: Int = 10, velocityPeak: Float = -2.7f): Boolean {
    return frame > frameTimeout && body.linearVelocity.y > velocityPeak
  }

  protected fun isNearStationary(threshold: Float = 1.0f): Boolean {
    return body.linearVelocity.len2() <= threshold
  }

  protected fun isStopping(frameTimeout: Int = 0, threshold: Float = 1.0f): Boolean {
    return frame > frameTimeout && abs(body.linearVelocity.x) <= threshold
  }

  protected fun isOnGround(frameTimeout: Int = 0): Boolean {
    return frame >= frameTimeout && form.hero.isOnGround
  }

  protected fun isAirborne(): Boolean {
    return form.hero.numberOfContacts == 0
  }

  protected fun isFalling(): Boolean {
    return body.linearVelocity.y >= 0.0f
  }

  protected fun canStartRunning(): Boolean {
    val direction = inputDirection()
    val orientation = currentWallContactOrientation()

    return when {
      orientation == null -> true
      direction.isNone -> false
      direction.isLeft && orientation.isRight -> false
      direction.isRight && orientation.isLeft -> false
      else -> true
    }
  }

  protected fun currentDirection(vx: Float = body.linearVelocity.x): Direction {
    return when {
      vx < 0.0 -> Direction.Left
      vx > 0.0 -> Direction.Right
      else -> Direction.None
    }
  }

  // MARK: Collisions
  protected fun currentContactOrientation(): Orientation? {
    val orientations = form.hero.orientations

    // TODO: this needs to be more sophisticated than a precedence ordering
    if (orientations.and(Orientation.Top).neq(0)) {
      return Orientation.Top
    } else if (orientations.and(Orientation.Right).neq(0)) {
      return Orientation.Right
    } else if (orientations.and(Orientation.Left).neq(0)) {
      return Orientation.Left
    } else if (orientations.and(Orientation.Bottom).neq(0)) {
      return Orientation.Bottom
    }

    return null
  }

  protected fun currentWallContactOrientation(): Orientation? {
    val orientations = form.hero.orientations
    val isOnLeftWall = orientations.and(Orientation.Right).neq(0)
    val isOnRightWall = orientations.and(Orientation.Left).neq(0)

    return when {
      isOnLeftWall == isOnRightWall -> null
      isOnLeftWall -> Orientation.Right
      isOnRightWall -> Orientation.Left
      else -> error("this is impossible")
    }
  }

  private fun findFixture(filter: (Fixture) -> Boolean) =
    body.fixtureList.find(filter) ?: error("missing required fixture")

  private fun findHero() =
    findFixture { it.hero != null }

  private fun findFoot() =
    findFixture { it.foot != null }

  // MARK: Helpers - Physics
  protected fun applyJumpImpulse(magnitude: Float) {
    debug(Tag.Hero, "$this applying jump impulse: $magnitude")
    body.applyImpulseToCenter(0.0f, -magnitude)
  }

  protected fun applyFastfallImpulse(magnitude: Float) {
    debug(Tag.Hero, "$this applying fall impulse: $magnitude")
    body.applyImpulseToCenter(0.0f, magnitude)
  }

  protected fun applyMovementImpulse(magnitude: Float, direction: Direction) {
    val impulse = when (direction) {
      Direction.Left -> -magnitude
      Direction.Right -> magnitude
      else -> null
    }

    if (impulse != null) {
      debug(Tag.Hero, "$this applying movement impulse: $impulse")
      body.applyImpulseToCenter(impulse, 0.0f)
    }
  }

  protected fun applyImpulse(magnitude: Float, angle: Float) {
    debug(Tag.Hero, "$this applying angled impulse: $magnitude")
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

  protected fun applyMovementSpeed(magnitude: Float, direction: Direction) {
    when (direction) {
      Direction.Left ->
        body.setLinearVelocity(-magnitude, 0.0f)
      Direction.Right ->
        body.setLinearVelocity(magnitude, 0.0f)
      else -> return
    }
  }

  protected fun applyMaxSpeed(magnitude: Float) {
    val velocity = body.linearVelocity
    if (velocity.x > magnitude) {
      body.setLinearVelocity(magnitude, velocity.y)
    }
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
package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.components.controls.Key
import dev.wizrad.fracture.game.world.components.State
import dev.wizrad.fracture.game.world.components.StateMachine
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class ReboundForm(
  private val body: Body,
  private val world: World): Form {

  // MARK: Form
  override val type = Form.Type.Rebound
  override val behavior = StateMachine(initialState = standing())

  override fun defineFixtures(size: Vector2) {
    // create fixtures
    val square = PolygonShape()
    square.setAsBox(size.x, size.y)

    val fixture = FixtureDef()
    fixture.shape = square
    fixture.density = 1.0f
    fixture.friction = 0.2f
    fixture.restitution = 0.5f

    body.createFixture(fixture)

    // dispose shapes
    square.dispose()
  }

  // MARK: States
  private fun standing(): State = object: State() {
    override fun update(delta: Float) {
      super.update(delta)

      // apply running movement
      val force = Vector2()
      if (world.controls.pressed(Key.Left)) {
        force.x -= 30.0f
      }

      if (world.controls.pressed(Key.Right)) {
        force.x += 30.0f
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      if (world.controls.pressed(Key.Jump) && canJump()) {
        return windup()
      }

      return null
    }

    private fun canJump(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      val fixture = body.fixtureList.first()
      val contactCount = world.contacts.count(fixture)
      return contactCount != 0
    }
  }

  private fun windup(): State = object: State() {
    override fun nextState(): State? {
      val frameLength = 4
      if (frame >= frameLength) {
        return jumpStart(isShort = !world.controls.pressed(Key.Jump))
      }

      return null
    }
  }

  private fun jumpStart(isShort: Boolean): State = object: State() {
    private val magnitude = if (isShort) 5.0f else 10.0f

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
    private var canFastfall = false
    private var restingFrames = 0

    override fun update(delta: Float) {
      super.update(delta)

      // apply directional influence
      val force = Vector2()
      if (world.controls.pressed(Key.Left)) {
        force.x -= 20.0f
      }

      if (world.controls.pressed(Key.Right)) {
        force.x += 20.0f
      }

      body.applyForceToCenter(force, true)

      // allow fastfalling any time after reaching the first jump's peak
      if (!canFastfall && isFalling()) {
        canFastfall = true
      }

      // if in contact with floor, increment resting frame count
      if (isLanding()) {
        restingFrames++
      } else {
        restingFrames = 0
      }
    }

    override fun nextState(): State? {
      // land once we've rested for enough frames (no longer bouncing)
      val restingFrameLength = 2
      if (restingFrames >= restingFrameLength) {
        return landing()
      } else if (world.controls.pressed(Key.Jump) && canFastfall) {
        return fastfalling()
      }

      return null
    }

    private fun isLanding(): Boolean {
      assert(body.fixtureList.size != 0) { "body have at least one fixture" }
      val fixture = body.fixtureList.first()
      val contactCount = world.contacts.count(fixture)
      return contactCount != 0
    }

    private fun isFalling(): Boolean {
      return body.linearVelocity.y >= 0.0
    }
  }

  private fun fastfalling(): State = object: State() {
    override fun start() {
      debug(Tag.World, "$this applying fastfall impulse")
      val center = body.worldCenter
      val magnitude = 50.0f
      body.applyLinearImpulse(0.0f, magnitude, center.x, center.y, true)
    }

    override fun update(delta: Float) {
      super.update(delta)

      // apply directional influence
      val force = Vector2()
      if (world.controls.pressed(Key.Left)) {
        force.x -= 20.0f
      }

      if (world.controls.pressed(Key.Right)) {
        force.x += 20.0f
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      // return to jumping at first contact to allow for re-falling
      return if (didLand()) jumping() else null
    }

    private fun didLand(): Boolean {
      assert(body.fixtureList.size != 0) { "body have at least one fixture" }
      val fixture = body.fixtureList.first()
      val contactCount = world.contacts.count(fixture)
      return contactCount != 0
    }
  }

  private fun landing(): State = object: State() {
    override fun nextState(): State? {
      val frameLength = 3
      return if (frame >= frameLength) standing() else null
    }
  }
}

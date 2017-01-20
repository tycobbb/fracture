package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class SingleJumpForm(
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
    fixture.friction = 0.2f
    fixture.filter.categoryBits = ContactType.Hero.bits

    body.createFixture(fixture)

    // dispose shapes
    square.dispose()
  }

  // MARK: States
  class Standing(context: Context): State(context) {
    private val runMagnitude = 30.0f

    override fun update(delta: Float) {
      super.update(delta)

      // apply running movement
      val force = Vector2()
      if (world.controls.left.isPressed) {
        force.x -= runMagnitude
      }

      if (world.controls.right.isPressed) {
        force.x += runMagnitude
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      if (world.controls.jump.isPressedUnique && canJump()) {
        return Windup(context)
      }

      return null
    }

    private fun canJump(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return world.contact.exists(body.fixtureList.first(), Orientation.Bottom)
    }
  }

  class Windup(context: Context): State(context) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(context, isShort = !world.controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(context: Context, isShort: Boolean): State(context) {
    private val frameLength = 3
    private val magnitude = if (isShort) 15.0f else 30.0f

    override fun start() {
      debug(Tag.World, "$this applying impulse: $magnitude")
      val center = body.worldCenter
      body.applyLinearImpulse(0.0f, -magnitude, center.x, center.y, true)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): State(context) {
    private val driftMagnitude = 20.0f

    override fun update(delta: Float) {
      super.update(delta)

      // apply directional influence
      val force = Vector2()
      if (world.controls.left.isPressed) {
        force.x -= driftMagnitude
      }

      if (world.controls.right.isPressed) {
        force.x += driftMagnitude
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      return if (didLand()) Landing(context) else null
    }

    private fun didLand(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return world.contact.exists(body.fixtureList.first(), Orientation.Bottom)
    }
  }

  class Landing(context: Context): State(context) {
    private val frameLength = 3

    override fun start() {
      super.start()
      world.controls.jump.requireUniquePress()
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Standing(context) else null
    }
  }
}

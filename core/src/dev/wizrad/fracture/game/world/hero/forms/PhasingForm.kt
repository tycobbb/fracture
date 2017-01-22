package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.support.contactInfo
import dev.wizrad.fracture.game.world.support.orientation
import dev.wizrad.fracture.game.world.support.raycast
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.className
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.extensions.Polar
import dev.wizrad.fracture.support.extensions.angleTo
import dev.wizrad.fracture.support.extensions.append
import dev.wizrad.fracture.support.fmt

class PhasingForm(context: Context): Form(context) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(context)
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
    fixture.friction = 0.2f
    fixture.filter.categoryBits = ContactType.Hero.bits

    body.createFixture(fixture)

    // dispose shapes
    square.dispose()
  }

  // MARK: States
  data class Target(
    val fixture: Fixture,
    val point: Vector2,
    val fraction: Float) {

    override fun toString(): String {
      return "[$className fixture=[${fixture.className} o=${fixture.orientation}] p:${point.fmt()}]"
    }
  }

  class Standing(context: Context): FormState(context) {
    private val runMag = 7.5f
    var phaseTarget: Target? = null; private set

    override fun update(delta: Float) {
      super.update(delta)

      if (controls.touch.isActive) {
        val source = body.position
        val destination = controls.touch.location

        debug(Tag.World, "begin raycasting: $source to $destination")

        // cast in reverse so that we can grab the outer edge
        val intersections = mutableListOf<Target>()
        physics.raycast(destination, source) { fixture, point, n, f ->
          val contactInfo = fixture.contactInfo
          if (contactInfo == null || !contactInfo.isPhaseable) {
            return@raycast -1.0f
          } else {
            intersections.append(Target(fixture, point.cpy(), f))
            return@raycast 1.0f
          }
        }

        if (intersections.size != 0) {
          // TODO: handle fixtures attached to multiple bodies
          phaseTarget = intersections.reduce { memo, data ->
            if (data.fraction < memo.fraction) data else memo
          }
        }
      }
    }

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
    }

    override fun nextState(): State? {
      val target = phaseTarget
      if (!controls.touch.isActive && target != null) {
        return Phasing(context, target)
      } else if (controls.jump.isPressedUnique && isOnGround()) {
        return Windup(context)
      }

      return null
    }
  }

  class Windup(context: Context): FormState(context) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(context, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(context: Context, isShort: Boolean): FormState(context) {
    private val frameLength = 3
    private val jumpMag = if (isShort) 2.5f else 5.0f

    override fun start() {
      applyJumpForce(jumpMag)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Phasing(context: Context, target: Target): FormState(context) {
    private val target = target
    private val phaseSpeed = 1.0f
    private val destination = destination()
    private val phaseVelocity = Polar.vector(
      magnitude = phaseSpeed,
      angle = body.position.angleTo(destination)
    )

    override fun start() {
      super.start()
      debug(Tag.World, "$this phasing to $target @ ${destination}")
      body.setTransform(destination, body.angle)
    }

//    override fun step(delta: Float) {
//      super.step(delta)
//    }

    override fun nextState(): State? {
      return Jumping(context)
    }

    private fun destination(): Vector2 {
      val size = entity.size
      val point = target.point.cpy()

      return when (target.fixture.orientation) {
        Orientation.Top -> point.add(0.0f, -size.y / 2)
        Orientation.Bottom -> point.add(0.0f, size.y / 2)
        Orientation.Left -> point.add(-size.x / 2, 0.0f)
        Orientation.Right -> point.add(size.x / 2, 0.0f)
        else -> error("attempted to phase to a target with no orientation")
      }
    }
  }

  class Jumping(context: Context): FormState(context) {
    private val driftMag = 5.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState(): State? {
      return if (isOnGround()) Landing(context) else null
    }
  }

  class Landing(context: Context): FormState(context) {
    private val frameLength = 3

    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Standing(context) else null
    }
  }
}
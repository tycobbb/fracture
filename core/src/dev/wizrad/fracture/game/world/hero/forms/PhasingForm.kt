package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.support.contactInfo
import dev.wizrad.fracture.game.world.support.reduceRaycast
import dev.wizrad.fracture.game.world.support.surface
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.extensions.Polar
import dev.wizrad.fracture.support.extensions.angleTo
import dev.wizrad.fracture.support.pow

class PhasingForm(context: Context): Form(context) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(context)
  }

  override fun defineFixtures(size: Vector2) {
    // create fixtures
    val square = PolygonShape()
    square.setAsBox(size.x / 2, size.y / 2)

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
  class Standing(context: Context): Phaseable(context) {
    private val runMag = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
    }

    override fun nextState(): State? {
      return if (canPhase()) {
        phasing()
      } else if (controls.jump.isPressedUnique && isOnGround()) {
        Windup(context)
      } else null
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

  class Jumping(context: Context): Phaseable(context) {
    private val driftMag = 5.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState(): State? {
      return if (canPhase()) {
        phasing()
      } else if (isOnGround()) {
        Landing(context)
      } else null
    }
  }

  class Phasing(context: Context, target: Target): FormState(context) {
    private val target = target
    private val start = body.position.cpy()
    private val end = destination()

    private val phaseSpeed = 10.0f
    private var phaseElapsed = 0.0f
    private val phaseDuration = start.dst(end) / phaseSpeed
    private val phaseVelocity = Polar.vector(
      magnitude = phaseSpeed,
      angle = start.angleTo(end)
    )

    override fun start() {
      super.start()

      debug(Tag.World, "$this moving to $end")
      body.gravityScale = 0.0f
      body.fixtureList.forEach {
        it.contactInfo = ContactInfo.Hero(true)
      }
    }

    override fun step(delta: Float) {
      super.step(delta)

      val remaining = phaseDuration - phaseElapsed
      phaseElapsed += delta

      // TODO: tween velocity (integral of v(t) == dst)
      val velocity = if (remaining < delta) {
        scratch1.set(phaseVelocity).scl(remaining / delta)
      } else {
        phaseVelocity
      }

      body.linearVelocity = velocity
    }

    override fun nextState(): State? {
      if (phaseElapsed >= phaseDuration) {
        return PhasingEnd(context)
      }

      return null
    }

    private fun destination(): Vector2 {
      val contact = target.fixture.contactInfo
      if (contact !is ContactInfo.Surface) {
        error("attempted to phase to a target that is not a Surface")
      }

      val size = entity.size
      val point = target.point.cpy()

      return when (contact.orientation) {
        Orientation.Top -> point.add(0.0f, -size.y / 2)
        Orientation.Bottom -> point.add(0.0f, size.y / 2)
        Orientation.Left -> point.add(-size.x / 2, 0.0f)
        Orientation.Right -> point.add(size.x / 2, 0.0f)
      }
    }
  }

  class PhasingEnd(context: Context): Phaseable(context) {
    private val frameLength = 3
    private val velocityPercent = 0.2f
    private val velocityScale = pow(velocityPercent, 1.0f / frameLength)

    override fun step(delta: Float) {
      super.step(delta)
      body.linearVelocity = body.linearVelocity.scl(velocityScale)
    }

    override fun destroy() {
      super.destroy()

      body.gravityScale = 1.0f
      body.fixtureList.forEach {
        it.contactInfo = ContactInfo.Hero(false)
      }
    }

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return if (isOnGround()) Landing(context) else Jumping(context)
      }

      return null
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

  // MARK: Phaseable
  data class Target(
    val fixture: Fixture,
    val point: Vector2,
    val fraction: Float
  )

  abstract class Phaseable(context: Context): FormState(context) {
    var target: Target? = null; private set

    override fun update(delta: Float) {
      super.update(delta)

      if (!controls.touch.isActive) {
        return
      }

      val source = body.position
      val dest = controls.touch.location

      // cast in reverse so that we can grab the outer edge
      // TODO: handle fixtures attached to multiple bodies
      var count = 0
      val intersection: Target? = physics.reduceRaycast(dest, source) { memo, fixture, point, n, f ->
        val contact = fixture.surface
        if (contact == null || !contact.isPhasingTarget) {
          return@reduceRaycast (-1.0f).to(memo)
        }

        count++
        if(memo == null || f < memo.fraction) {
          1.0f.to(Target(fixture, point.cpy(), f))
        } else {
          1.0f.to(memo)
        }
      }

      // only target this intersection if there were at least 2 candidates
      target = if (count > 1) intersection else null
    }

    fun canPhase(): Boolean {
      return !controls.touch.isActive && target != null
    }

    fun phasing(): Phasing {
      return Phasing(context, target!!)
    }
  }
}
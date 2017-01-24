package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState
import dev.wizrad.fracture.game.world.support.extensions.contactInfo
import dev.wizrad.fracture.game.world.support.extensions.hero
import dev.wizrad.fracture.game.world.support.extensions.reduceRaycast
import dev.wizrad.fracture.game.world.support.extensions.surface
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.extensions.Polar
import dev.wizrad.fracture.support.extensions.angleTo

class PhasingForm(entity: Entity): Form(entity) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(this, phasesLeft = 3)
  }

  override fun defineFixtures() {
    val polygon = PolygonShape()

    // create fixtures
    createBox(defineBox(polygon))
    createFoot(polygon)

    // dispose shapes
    polygon.dispose()
  }

  // MARK: States
  class Standing(form: PhasingForm, phasesLeft: Int): PhasingState(form, phasesLeft) {
    private val runMag = 7.5f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(runMag)
    }

    override fun nextState(): State? {
      return if(canPhase()) {
        phasingState()
      }else if (!isOnGround()) {
        Jumping(form, phasesLeft)
      } else if (controls.jump.isPressedUnique) {
        Windup(form, phasesLeft)
      } else null
    }
  }

  class Windup(form: PhasingForm, phasesLeft: Int): PhasingState(form, phasesLeft) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(form, phasesLeft, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(form: PhasingForm, phasesLeft: Int, isShort: Boolean): PhasingState(form, phasesLeft) {
    private val frameLength = 3
    private val jumpMag = if (isShort) 2.5f else 5.0f

    override fun start() {
      applyJumpImpulse(jumpMag)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping(form, phasesLeft) else null
    }
  }

  class Jumping(form: PhasingForm, phasesLeft: Int): PhasingState(form, phasesLeft) {
    private val driftMag = 5.0f

    override fun step(delta: Float) {
      super.step(delta)
      applyMovementForce(driftMag)
    }

    override fun nextState(): State? {
      return if (canPhase()) {
        phasingState()
      } else if (isOnGround()) {
        Landing(form, phasesLeft)
      } else null
    }
  }

  class Phasing(form: PhasingForm, phasesLeft: Int, target: Target): PhasingState(form, phasesLeft) {
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

    override fun initiatesPhasing(): Boolean {
      return false
    }

    override fun start() {
      super.start()

      debug(Tag.World, "$this moving to $end")
      stopGravity()
      startPhasing()
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
        return PhasingEnd(form, phasesLeft - 1)
      }

      return null
    }

    private fun destination(): Vector2 {
      val contact = target.fixture.contactInfo
      if (contact !is ContactInfo.Surface) {
        error("attempted to phase to a phasingTarget that is not a Surface")
      }

      val size = size
      val point = target.point.cpy()

      return when (contact.orientation) {
        Orientation.Top -> point.add(0.0f, -size.y / 2)
        Orientation.Bottom -> point.add(0.0f, size.y / 2)
        Orientation.Left -> point.add(-size.x / 2, 0.0f)
        Orientation.Right -> point.add(size.x / 2, 0.0f)
      }
    }
  }

  class PhasingEnd(form: PhasingForm, phasesLeft: Int): PhasingState(form, phasesLeft) {
    private val phasingDamping = 10.0f

    override fun start() {
      super.start()
      startDamping(phasingDamping)
    }

    override fun destroy() {
      super.destroy()

      stopDamping()
      startGravity()
      stopPhasing()
    }

    override fun nextState(): State? {
      if (isNearStationary()) {
        return if (isOnGround()) Landing(form, phasesLeft) else Jumping(form, phasesLeft)
      }

      return null
    }
  }

  class Landing(form: PhasingForm, phasesLeft: Int): PhasingState(form, phasesLeft) {
    private val frameLength = 3

    override fun start() {
      super.start()
      requireUniqueJump()
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Standing(form, phasesLeft) else null
    }
  }

  // MARK: Base States
  data class Target(
    val fixture: Fixture, val point: Vector2, val fraction: Float
  )

  abstract class PhasingState(form: PhasingForm, phasesLeft: Int): FormState<PhasingForm>(form) {
    // MARK: Properties
    val phasesLeft = phasesLeft
    var phasingTarget: Target? = null; private set

    // MARK: Behavior
    override fun update(delta: Float) {
      super.update(delta)

      // look for a phasingTarget if allowed
      if (canInitiatePhasing() && controls.touch.isActive) {
        phasingTarget = findTarget(controls.touch.location)
      }
    }

    // MARK: Phasing
    open fun initiatesPhasing(): Boolean {
      return true
    }

    fun canInitiatePhasing(): Boolean {
      return initiatesPhasing() && phasesLeft > 0
    }

    fun canPhase(): Boolean {
      return !controls.touch.isActive && phasingTarget != null
    }

    fun phasingState(): Phasing {
      return Phasing(form, phasesLeft, phasingTarget!!)
    }

    fun startPhasing() {
      body.fixtureList.forEach { fixture ->
        fixture.hero?.let { it.isPhasing = true }
      }
    }

    fun stopPhasing() {
      body.fixtureList.forEach { fixture ->
        fixture.hero?.let { it.isPhasing = false }
      }
    }

    // TODO: handle fixtures attached to multiple bodies
    private fun findTarget(dest: Vector2): Target? {
      var count = 0
      var intersection: Target? = null

      // cast in reverse so that we can grab the outer edge
      intersection = world.reduceRaycast(dest, body.position) { memo, fixture, point, n, f ->
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

      // only phasingTarget this intersection if there were at least 2 candidates
      return if (count > 1) intersection else null
    }
  }
}

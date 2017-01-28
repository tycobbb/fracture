package dev.wizrad.fracture.game.world.hero

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.Fixture
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.and
import dev.wizrad.fracture.game.world.components.contact.not
import dev.wizrad.fracture.game.world.components.session.Event
import dev.wizrad.fracture.game.world.components.session.toEvent
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.forms.*
import dev.wizrad.fracture.game.world.support.extensions.destroyAllFixtures
import dev.wizrad.fracture.game.world.support.extensions.foot
import dev.wizrad.fracture.game.world.support.extensions.hero
import dev.wizrad.fracture.game.world.support.extensions.surface
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.info

class Hero(
  body: Body, size: Vector2): Entity(body, size) {

  // MARK: Properties
  var form: Form = DebugForm(this); private set

  // MARK: Behavior
  override fun start() {
    super.start()

    // subscribe to events
    subscribe(
      toEvent<Event.LevelStarted> { onLevelStarted(it) },
      toEvent<Event.TransitionStarted> { onTransitionStarted(it) }
    )
  }

  override fun update(delta: Float) {
    super.update(delta)
    form.update(delta)
  }

  override fun step(delta: Float) {
    super.step(delta)
    form.step(delta)
  }

  override fun lateUpdate(delta: Float) {
    super.lateUpdate(delta)
    form.lateUpdate(delta)
  }

  override fun destroy() {
    super.destroy()
    form.destroy()
  }

  // MARK: Contact
  var isOnGround = false; private set
  var orientations: Short = 0; private set
  var numberOfContacts = 0; private set

  override fun onContact(fixture: Fixture, other: Entity, otherFixture: Fixture, didStart: Boolean) {
    super.onContact(fixture, other, otherFixture, didStart)

    if (didStart) {
      numberOfContacts++
    } else {
      numberOfContacts--
    }

    val surface = otherFixture.surface
    if (surface != null) {
      onContactSurface(fixture, surface, didStart)
    }
  }

  private fun onContactSurface(fixture: Fixture, surface: ContactInfo.Surface, didStart: Boolean) {
    if (surface.orientation.isTop && fixture.foot != null) {
      isOnGround = didStart
    } else if(fixture.hero != null) {
      val bit = surface.orientation.bit
      val mask = if (didStart) bit else !bit
      orientations = orientations and mask
    }
  }

  // MARK: Events
  private fun onLevelStarted(event: Event.LevelStarted) {
    randomizeForm()

    val center = event.level.transform(event.start.center)
    body.setTransform(center, body.angle)
    body.setLinearVelocity(0.0f, 0.0f)
  }

  private fun onTransitionStarted(event: Event.TransitionStarted) {
    val target = event.level.transform(event.start.center)
    updateForm(next = TransitionForm(this, target))
  }

  // MARK: Actions
  fun randomizeForm() {
    updateForm(next = sampleForm())
  }

  private fun updateForm(next: Form) {
    info(Tag.World, "$this updating to form: $next")
    body.destroyAllFixtures()
    form.destroy()
    form = next
    form.defineFixtures()
    form.start()
  }

  private fun sampleForm(): Form {
    return when (form) {
      is VanillaForm -> SpaceJumpForm(this)
      is SpaceJumpForm -> ReboundForm(this)
      is ReboundForm -> SpearForm(this)
      is SpearForm -> PhasingForm(this)
      is PhasingForm -> AirDashForm(this)
      is AirDashForm -> FluidForm(this)
      is FluidForm -> FlutterForm(this)
      is FlutterForm -> DebugForm(this)
      else -> VanillaForm(this)
    }
  }

  // MARK: Factory
  companion object: Entity.UnitFactory<Hero>() {
    val size = Vector2(1.0f, 1.0f)

    override fun entity(parent: Entity?, args: Unit)
      = Hero(body(parent, args), size)

    private fun body(parent: Entity?, args: Unit): Body {
      if (parent == null) error("parent required")

      val bodyDef = BodyDef()
      bodyDef.type = BodyType.DynamicBody
      bodyDef.fixedRotation = true

      val body = parent.world.createBody(bodyDef)

      return body
    }
  }
}

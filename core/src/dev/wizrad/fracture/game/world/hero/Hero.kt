package dev.wizrad.fracture.game.world.hero

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import dev.wizrad.fracture.game.world.components.session.Event
import dev.wizrad.fracture.game.world.components.session.toEvent
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.forms.*
import dev.wizrad.fracture.game.world.support.extensions.destroyAllFixtures
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.info

class Hero(
  body: Body, size: Vector2): Entity(body, size) {

  // MARK: Properties
  var form: Form = FlutterForm(this); private set

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
  class Factory(parent: Entity?): Entity.UnitFactory<Hero>(parent) {
    // MARK: Output
    override fun entity(args: Unit) = Hero(body(args), size)

    // MARK: Body
    override fun defineBody(args: Unit): BodyDef {
      val body = super.defineBody(args)
      body.type = BodyType.DynamicBody
      body.fixedRotation = true
      return body
    }
  }

  // MARK: Companion
  companion object {
    val size = Vector2(1.0f, 1.0f)
  }
}

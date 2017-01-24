package dev.wizrad.fracture.game.world.hero

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.forms.*

class Hero(
  body: Body, size: Vector2): Entity(body, size) {

  // MARK: Properties
  var form: Form = DebugForm(this); private set

  // MARK: Behavior
  override fun start() {
    super.start()
    didUpdateForm()
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

  // MARK: Forms
  fun selectForm() {
    willUpdateForm()
    form = createRandomForm()
    didUpdateForm()
  }

  private fun willUpdateForm() {
    val fixtures = body.fixtureList
    while (fixtures.size > 0) {
      body.destroyFixture(fixtures[0])
    }
    form.destroy()
  }

  private fun didUpdateForm() {
    form.defineFixtures()
    form.start()
  }

  private fun createRandomForm(): Form {
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
  data class Args(val center: Vector2)

  class Factory(parent: Entity?): Entity.Factory<Hero, Args>(parent) {
    fun entity(center: Vector2) = entity(Args(center))

    // MARK: Output
    override fun entity(args: Args) = Hero(body(args), size)

    // MARK: Body
    override fun defineBody(args: Args): BodyDef {
      val body = super.defineBody(args)
      body.type = BodyType.DynamicBody
      body.fixedRotation = true
      body.position.set(transform(args.center))
      return body
    }
  }

  // MARK: Companion
  companion object {
    val size = Vector2(1.0f, 1.0f)
  }
}

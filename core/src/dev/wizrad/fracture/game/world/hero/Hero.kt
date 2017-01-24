package dev.wizrad.fracture.game.world.hero

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.forms.*

class Hero(
  context: Context, body: Body, size: Vector2): Entity(context, body, size) {

  // MARK: Properties
  var form: Form = DebugForm(context()); private set

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
      is VanillaForm -> SpaceJumpForm(context())
      is SpaceJumpForm -> ReboundForm(context())
      is ReboundForm -> SpearForm(context())
      is SpearForm -> PhasingForm(context())
      is PhasingForm -> AirDashForm(context())
      is AirDashForm -> FluidForm(context())
      is FluidForm -> FlutterForm(context())
      is FlutterForm -> DebugForm(context())
      else -> VanillaForm(context())
    }
  }

  // MARK: Factory
  data class Args(val center: Vector2)

  class Factory(context: Context): Entity.Factory<Hero, Args>(context) {
    fun entity(center: Vector2) = entity(Args(center))

    // MARK: Output
    override fun entity(args: Args) = Hero(context, body(args), size)

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

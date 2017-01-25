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

  // MARK: Actions
  fun moveTo(position: Vector2) {
    body.setTransform(position, body.angle)
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

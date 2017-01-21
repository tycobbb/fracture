package dev.wizrad.fracture.game.world.hero

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.hero.forms.*

class Hero(
  context: Context, body: Body, size: Vector2): Entity(context, body, size) {

  // MARK: Entity
  override val name = "Hero"

  // MARK: Properties
  var form: Form = PhasingForm(context()); private set

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
    body.fixtureList.forEach { body.destroyFixture(it) }
    form.destroy()
  }

  private fun didUpdateForm() {
    form.defineFixtures(size)
    form.start()
  }

  private fun createRandomForm(): Form {
    return when (form) {
      is SingleJumpForm -> SpaceJumpForm(context())
      is SpaceJumpForm -> ReboundForm(context())
      is ReboundForm -> SpearForm(context())
      is SpearForm -> PhasingForm(context())
      else -> SingleJumpForm(context())
    }
  }

  class Factory(context: Context): Entity.Factory<Factory.Args>(context) {
    data class Args(val center: Vector2)

    // MARK: Output
    fun entity(center: Vector2) = Hero(context, body(Args(center)), size)

    // MARK: Body
    override fun defineBody(options: Args): BodyDef {
      val body = super.defineBody(options)
      body.type = BodyType.DynamicBody
      body.fixedRotation = true
      body.position.set(transform(options.center))
      return body
    }
  }

  companion object {
    val size = Vector2(1.0f, 1.0f)
  }
}

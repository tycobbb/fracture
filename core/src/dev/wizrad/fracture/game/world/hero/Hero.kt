package dev.wizrad.fracture.game.world.hero

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.game.world.hero.forms.*

class Hero(
  parent: Entity, world: World): Entity(parent, world) {

  // MARK: EntityBase
  override val name = "Hero"
  override val size = Vector2(1.0f, 1.0f)

  // MARK: Properties
  lateinit var form: Form

  // MARK: Behavior
  override fun start() {
    super.start()
    transitionToForm(SpearForm(context()))
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
    body.fixtureList.forEach { body.destroyFixture(it) }
    form.destroy()
    transitionToForm(createRandomForm())
  }

  private fun transitionToForm(newForm: Form) {
    form = newForm
    form.defineFixtures(size)
    form.start()
  }

  private fun createRandomForm(): Form {
    return when (form) {
      is SingleJumpForm -> SpaceJumpForm(context())
      is SpaceJumpForm -> ReboundForm(context())
      is ReboundForm -> SpearForm(context())
      else -> SingleJumpForm(context())
    }
  }

  private fun context(): State.Context {
    return State.Context(body, world)
  }

  // MARK: Body
  override fun defineBody(): BodyDef {
    val body = super.defineBody()
    body.type = BodyType.DynamicBody
    body.fixedRotation = true
    body.position.set(transform(
      x = (parent!!.size.x - size.x) / 2,
      y = 5.0f
    ))

    return body
  }
}

package dev.wizrad.fracture.game.world.hero

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.game.world.hero.forms.Form
import dev.wizrad.fracture.game.world.hero.forms.ReboundForm
import dev.wizrad.fracture.game.world.hero.forms.SingleJumpForm
import dev.wizrad.fracture.game.world.hero.forms.SpaceJumpForm

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
    selectForm(SingleJumpForm(body, world))
  }

  override fun update(delta: Float) {
    super.update(delta)
    form.behavior.update(delta)
  }

  override fun step(delta: Float) {
    super.step(delta)
    form.behavior.step(delta)
  }

  override fun destroy() {
    super.destroy()
    form.behavior.destroy()
  }

  // MARK: Forms
  fun selectForm(form: Form? = null) {
    body.fixtureList.forEach { body.destroyFixture(it) }

    this.form = form ?: createRandomForm()
    this.form.defineFixtures(size)
    this.form.behavior.start()
  }

  private fun createRandomForm(): Form {
    return when (form) {
      is SingleJumpForm -> SpaceJumpForm(body, world)
      is SpaceJumpForm -> ReboundForm(body, world)
      else -> SingleJumpForm(body, world)
    }
  }

  // MARK: Body
  override fun defineBody(): BodyDef {
    val body = super.defineBody()
    body.type = BodyType.DynamicBody
    body.fixedRotation = true
    body.position.set(transform(
      x = (parent!!.size.x - size.x) / 2,
      y = 0.0f
    ))

    return body
  }
}

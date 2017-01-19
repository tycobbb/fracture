package dev.wizrad.fracture.game.world.hero

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntityBase
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.game.world.hero.forms.SpaceJumpForm
import dev.wizrad.fracture.game.world.hero.forms.Form
import dev.wizrad.fracture.game.world.hero.forms.ReboundForm
import dev.wizrad.fracture.game.world.hero.forms.SingleJumpForm

class Hero(
  parent: EntityBase, world: World): Entity(parent, world) {

  // MARK: EntityBase
  override val name = "Hero"
  override val size = Vector2(1.0f, 1.0f)

  // MARK: Properties
  lateinit var form: Form

  // MARK: Lifecycle
  override fun initialize() {
    super.initialize()
    selectForm(ReboundForm(body, w))
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
  }

  private fun createRandomForm(): Form {
    return when (form) {
      is SingleJumpForm -> SpaceJumpForm(body, w)
      is SpaceJumpForm -> ReboundForm(body, w)
      else -> SingleJumpForm(body, w)
    }
  }

  // MARK: Body
  override fun defineBody(): BodyDef {
    val body = super.defineBody()
    body.type = BodyType.DynamicBody
    body.position.set(transform(
      x = (parent!!.size.x - size.x) / 2,
      y = 0.0f
    ))

    return body
  }
}

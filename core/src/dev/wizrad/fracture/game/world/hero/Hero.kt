package dev.wizrad.fracture.game.world.hero

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntityBase
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.game.world.hero.forms.DoubleJumpForm
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
    form = ReboundForm(body, w)
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
  fun randomizeForm() {
    form = createRandomForm()
  }

  private fun createRandomForm(): Form {
    return when (form) {
      is SingleJumpForm -> DoubleJumpForm(body, w)
      is DoubleJumpForm -> ReboundForm(body, w)
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

  override fun defineFixtures(body: Body) {
    super.defineFixtures(body)

    // create fixtures
    val square = PolygonShape()
    square.setAsBox(size.x, size.y)

    val fixture = FixtureDef()
    fixture.shape = square
    fixture.density = 1.0f
    fixture.friction = 0.2f

    body.createFixture(fixture)

    // dispose shapes
    square.dispose()
  }
}

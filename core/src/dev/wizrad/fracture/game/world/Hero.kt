package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntityBase
import dev.wizrad.fracture.game.world.core.World

class Hero(
  parent: EntityBase, world: World): Entity(parent, world) {

  // MARK: EntityBase
  override val name = "Hero"
  override val size = Vector2(1.0f, 1.0f)

  // MARK: Properties
  var form: Form? = null

  override fun initialize() {
    super.initialize()
    randomizeForm()
  }

  override fun update(delta: Float) {
    super.update(delta)
    form?.update(delta)
  }

  override fun step(delta: Float) {
    super.step(delta)
    form?.step(delta)
  }

  override fun destroy() {
    super.destroy()
    form?.destroy()
  }

  // MARK: Forms
  private fun randomizeForm() {
    form = createRandomForm()
  }

  private fun createRandomForm(): Form {
    return SingleJumpForm(body = body, w = w)
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
    fixture.restitution = 0.5f
    fixture.friction = 0.2f

    body.createFixture(fixture)

    // dispose shapes
    square.dispose()
  }
}

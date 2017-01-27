package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.components.contact.set
import dev.wizrad.fracture.game.world.core.Entity

class Collapser(
  body: Body, size: Vector2): Entity(body, size) {

  // MARK: Properties
  private val delay = 3.0f
  private var elapsed = 0.0f

  // MARK: Behavior
  override fun step(delta: Float) {
    super.step(delta)

    // start moving after delay
    if (elapsed < delay) {
      elapsed += delta
      if (elapsed > delay) {
        body.setLinearVelocity(0.0f, -0.2f)
      }
    }

    val fixture = body.fixtureList.first()
    contact.filter(fixture, Orientation.Top).forEach {
      it.body.type = BodyDef.BodyType.DynamicBody
    }
  }

  // MARK: Factory
  class Args(val y: Float, val width: Float)
  class Factory(parent: Entity): Entity.Factory<Collapser, Args>(parent) {
    override fun entity(args: Args) = Collapser(body(args), Vector2(args.width, 0.0f))

    override fun defineBody(args: Args): BodyDef {
      val body = super.defineBody(args)
      body.type = BodyDef.BodyType.DynamicBody
      body.gravityScale = 0.0f
      body.position.set(parent.transform(parent.size.x / 2, parent.size.y))
      return body
    }

    override fun defineFixtures(body: Body, args: Args) {
      super.defineFixtures(body, args)

      // create fixtures
      val rect = PolygonShape()
      rect.setAsBox(args.width / 2, 0.0f)

      val sensorDef = FixtureDef()
      sensorDef.shape = rect
      sensorDef.isSensor = true
      sensorDef.filter.set(ContactType.Collapser)

      body.createFixture(sensorDef)

      // dispose shapes
      rect.dispose()
    }
  }
}
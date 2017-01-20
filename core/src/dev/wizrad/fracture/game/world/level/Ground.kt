package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.support.contactInfo

class Ground(
  context: Context, body: Body, size: Vector2): Entity(context, body, size) {

  // MARK: Entity
  override val name = "Ground"

  // MARK: Lifecycle
  class Factory(context: Context): Entity.UnitFactory(context) {
    private val size = Vector2(parent!!.size.x, 4.0f)

    // MARK: Output
    fun entity() = Ground(context, body(), size)

    // MARK: Body
    override fun defineBody(options: Unit): BodyDef {
      val body = super.defineBody(options)
      body.type = BodyType.StaticBody
      body.position.set(transform(
        x = 0.0f,
        y = parent!!.size.y - size.y
      ))

      return body
    }

    override fun defineFixtures(body: Body, options: Unit) {
      super.defineFixtures(body, options)

      // create fixtures
      val rect = PolygonShape()
      val width = size.x / 2
      val height = size.y / 2
      rect.setAsBox(width, height, scratch.set(width, height), 0.0f)

      val fixtureDef = FixtureDef()
      fixtureDef.shape = rect
      fixtureDef.density = 1.0f
      fixtureDef.friction = 0.2f
      fixtureDef.filter.categoryBits = ContactType.Wall.bits

      val fixture = body.createFixture(fixtureDef)
      fixture.contactInfo = ContactInfo(Orientation.Top)

      // dispose shapes
      rect.dispose()
    }
  }
}
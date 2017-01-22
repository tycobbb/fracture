package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.support.contactInfo

class Wall(
  context: Context, body: Body, size: Vector2): Entity(context, body, size) {

  // MARK: Entity
  override val name = "Wall"

  // MARK: Lifecycle
  class Factory(context: Context): Entity.Factory<Factory.Args>(context) {
    data class Args(val center: Vector2)

    // MARK: Output
    fun entity(center: Vector2) = Wall(context, body(Args(center)), size)

    // MARK: Body
    override fun defineBody(options: Args): BodyDef {
      val body = super.defineBody(options)
      body.type = BodyDef.BodyType.StaticBody
      body.position.set(transform(options.center))
      return body
    }

    override fun defineFixtures(body: Body, options: Args) {
      super.defineFixtures(body, options)

      val width = size.x / 2
      val height = size.y / 2
      val rect = PolygonShape()

      // create wall
      rect.setAsBox(width, height)
      val wallDef = defineBox(rect)
      wallDef.density = 1.0f
      wallDef.friction = 0.2f
      body.createFixture(wallDef)

      // create sensors
      val edge = 0.05f

      // create left sensor
      rect.setAsBox(edge, height - edge * 2, scratch.set(edge - width, 0.0f), 0.0f)
      createSensor(body, rect, orientation = Orientation.Left)

      // create right sensor
      rect.setAsBox(edge, height - edge * 2, scratch.set(width - edge, 0.0f), 0.0f)
      createSensor(body, rect, orientation = Orientation.Right)

      // create top sensor
      rect.setAsBox(width, edge, scratch.set(0.0f, edge - height), 0.0f)
      createSensor(body, rect, orientation = Orientation.Top)

      // create bottom sensor
      rect.setAsBox(width, edge, scratch.set(0.0f, height - edge), 0.0f)
      createSensor(body, rect, orientation = Orientation.Bottom)

      // dispose shapes
      rect.dispose()
    }

    private fun createSensor(body: Body, rect: PolygonShape, orientation: Orientation) {
      val sensor = body.createFixture(defineBox(rect, isSensor = true))
      sensor.contactInfo = ContactInfo(orientation, isPhaseable = orientation != Orientation.Bottom)
    }

    private fun defineBox(rect: PolygonShape, isSensor: Boolean = false): FixtureDef {
      val boxDef = FixtureDef()
      boxDef.shape = rect
      boxDef.filter.categoryBits = ContactType.Wall.bits
      boxDef.isSensor = isSensor
      return boxDef
    }
  }

  companion object {
    val size = Vector2(1.0f, 4.0f)
  }
}
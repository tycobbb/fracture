package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.support.extensions.contactInfo

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

      // create edges
      val edge = 0.05f

      // create left edge
      rect.setAsBox(edge, height - edge * 2, scratch.set(edge - width, 0.0f), 0.0f)
      createSurface(body, rect, orientation = Orientation.Left)

      // create right edge
      rect.setAsBox(edge, height - edge * 2, scratch.set(width - edge, 0.0f), 0.0f)
      createSurface(body, rect, orientation = Orientation.Right)

      // create top edge
      rect.setAsBox(width, edge, scratch.set(0.0f, edge - height), 0.0f)
      createSurface(body, rect, orientation = Orientation.Top)

      // create bottom edge
      rect.setAsBox(width, edge, scratch.set(0.0f, height - edge), 0.0f)
      createSurface(body, rect, orientation = Orientation.Bottom)

      // dispose shapes
      rect.dispose()
    }

    private fun createSurface(body: Body, rect: PolygonShape, orientation: Orientation) {
      val surfaceDef = FixtureDef()
      surfaceDef.shape = rect
      surfaceDef.density = 1.0f
      surfaceDef.friction = 0.2f
      surfaceDef.filter.categoryBits = ContactType.Wall.bits

      val surface = body.createFixture(surfaceDef)
      surface.contactInfo = ContactInfo.Surface(
        orientation = orientation,
        isPhasingTarget = orientation != Orientation.Bottom,
        isPhaseable = true
      )
    }
  }

  companion object {
    val size = Vector2(1.0f, 4.0f)
  }
}
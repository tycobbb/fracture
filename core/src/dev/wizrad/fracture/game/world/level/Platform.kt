package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.level.loader.LevelFeatureArgs
import dev.wizrad.fracture.game.world.support.extensions.contactInfo

class Platform(
  body: Body, size: Vector2): Entity(body, size) {

  // MARK: Factory
  class Args: LevelFeatureArgs()

  class Factory(parent: Entity?): Entity.Factory<Platform, Args>(parent) {
    // MARK: Output
    override fun entity(args: Args) = Platform(body(args), args.size)

    // MARK: Body
    override fun defineBody(args: Args): BodyDef {
      val body = super.defineBody(args)
      body.type = BodyDef.BodyType.StaticBody
      body.position.set(transform(args.center))
      return body
    }

    override fun defineFixtures(body: Body, args: Args) {
      super.defineFixtures(body, args)

      val width = args.size.x / 2
      val height = args.size.y / 2
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
}
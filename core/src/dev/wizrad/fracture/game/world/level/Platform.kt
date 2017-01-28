package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.components.contact.set
import dev.wizrad.fracture.game.world.components.loader.LevelData
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.support.extensions.contactInfo

class Platform(
  body: Body, args: Platform.Args): Feature(body, args) {

  // MARK: Factory
  class Args: LevelData.Feature()

  companion object: Factory<Platform, Args>() {
    // MARK: Output
    override fun entity(parent: Entity?, args: Args)
      = Platform(body(parent, args), args)

    private fun body(parent: Entity?, args: Args): Body {
      if (parent == null) error("parent required")

      // create body
      val bodyDef = BodyDef()
      bodyDef.type = BodyDef.BodyType.StaticBody
      bodyDef.gravityScale = 0.1f
      bodyDef.position.set(parent.transform(args.center))

      val body = parent.world.createBody(bodyDef)
      fixtures(body, args)

      return body
    }

    private fun fixtures(body: Body, args: Args) {
      val edge = 0.05f
      val width = args.size.x / 2
      val height = args.size.y / 2
      val rect = PolygonShape()

      // left
      rect.setAsBox(edge, height - edge * 2, scratch.set(edge - width, 0.0f), 0.0f)
      createSurface(body, rect, orientation = Orientation.Left)

      // right
      rect.setAsBox(edge, height - edge * 2, scratch.set(width - edge, 0.0f), 0.0f)
      createSurface(body, rect, orientation = Orientation.Right)

      // top
      rect.setAsBox(width, edge, scratch.set(0.0f, edge - height), 0.0f)
      createSurface(body, rect, orientation = Orientation.Top)

      // bottom
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
      surfaceDef.filter.set(ContactType.Terrain)

      val surface = body.createFixture(surfaceDef)
      surface.contactInfo = ContactInfo.Surface(
        orientation = orientation,
        isPhasingTarget = orientation != Orientation.Bottom,
        isPhaseable = true
      )
    }
  }
}
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

class Wall(
  body: Body, args: Wall.Args): Feature(body, args) {

  // MARK: Factory
  class Args: LevelData.Feature() {
    lateinit var orientation: Orientation
  }

  companion object: Factory<Wall, Args>() {
    override fun entity(parent: Entity?, args: Args)
      = Wall(body(parent, args), args)

    // MARK: Body
    private fun body(parent: Entity?, args: Args): Body {
      if (parent == null) error("parent required")

      val bodyDef = BodyDef()
      bodyDef.type = BodyDef.BodyType.StaticBody
      bodyDef.position.set(parent.transform(args.center))

      val body = parent.world.createBody(bodyDef)
      fixtures(body, args)

      return body
    }

    private fun fixtures(body: Body, args: Args) {
      val rect = PolygonShape()

      // create fixtures
      rect.setAsBox(args.size.x / 2, args.size.y / 2)

      val wallDef = FixtureDef()
      wallDef.shape = rect
      wallDef.density = 1.0f
      wallDef.friction = 0.2f
      wallDef.filter.set(ContactType.Terrain)

      val wall = body.createFixture(wallDef)
      wall.contactInfo = ContactInfo.Surface(
        orientation = args.orientation
      )

      // dispose shapes
      rect.dispose()
    }
  }
}
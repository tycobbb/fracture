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
import dev.wizrad.fracture.game.world.level.loader.LevelFeatureArgs
import dev.wizrad.fracture.game.world.support.extensions.contactInfo

class Wall(
  context: Context, body: Body, size: Vector2) : Entity(context, body, size) {

  // MARK: Lifecycle
  class Args: LevelFeatureArgs() {
    lateinit var orientation: Orientation
  }

  class Factory(context: Context) : Entity.Factory<Wall, Args>(context) {
    // MARK: Output
    override fun entity(args: Args) = Wall(context, body(args), args.size)

    // MARK: Body
    override fun defineBody(args: Args): BodyDef {
      val body = super.defineBody(args)
      body.type = BodyDef.BodyType.StaticBody
      body.position.set(transform(args.center))
      return body
    }

    override fun defineFixtures(body: Body, args: Args) {
      super.defineFixtures(body, args)

      val rect = PolygonShape()
      rect.setAsBox(args.size.x / 2, args.size.y / 2)

      val wallDef = FixtureDef()
      wallDef.shape = rect
      wallDef.density = 1.0f
      wallDef.friction = 0.2f
      wallDef.filter.categoryBits = ContactType.Wall.bits

      val wall = body.createFixture(wallDef)
      wall.contactInfo = ContactInfo.Surface(
        orientation = args.orientation
      )
    }
  }
}
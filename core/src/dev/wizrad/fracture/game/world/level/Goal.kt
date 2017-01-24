package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.level.loader.LevelFeatureArgs
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class Goal(
  body: Body, size: Vector2) : Entity(body, size) {

  override fun lateUpdate(delta: Float) {
    super.lateUpdate(delta)

    if (contact.any(body.fixtureList.first(), type = ContactType.Hero)) {
      debug(Tag.World, "reached goal!")
    }
  }

  // MARK: Factory
  class Args: LevelFeatureArgs()

  class Factory(parent: Entity): Entity.Factory<Goal, Args>(parent) {
    override fun entity(args: Args): Goal = Goal(body(args), args.size)

    override fun defineBody(args: Args): BodyDef {
      val body = super.defineBody(args)
      body.type = BodyDef.BodyType.StaticBody
      body.position.set(transform(args.center))
      return body
    }

    override fun defineFixtures(body: Body, args: Args) {
      super.defineFixtures(body, args)

      // create sensor
      val rect = PolygonShape()
      rect.setAsBox(args.size.x / 2, args.size.y / 2)

      val goalDef = FixtureDef()
      goalDef.shape = rect
      goalDef.isSensor = true
      goalDef.filter.categoryBits = ContactType.Event.bits

      body.createFixture(goalDef)

      // dispose shapes
      rect.dispose()
    }
  }
}
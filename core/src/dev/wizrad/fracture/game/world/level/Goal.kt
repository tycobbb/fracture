package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.set
import dev.wizrad.fracture.game.world.components.loader.LevelData
import dev.wizrad.fracture.game.world.core.Entity

class Goal(
  body: Body, args: Goal.Args): Feature(body, args) {

  override fun lateUpdate(delta: Float) {
    super.lateUpdate(delta)

    val fixture = body.fixtureList.first()
    if (contact.any(fixture, type = ContactType.Hero)) {
      session.finishLevel()
    }
  }

  // MARK: Factory
  class Args: LevelData.Feature()

  companion object: Factory<Goal, Args>() {
    override fun entity(parent: Entity?, args: Args): Goal
      = Goal(body(parent, args), args)

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
      // create sensor
      val rect = PolygonShape()
      rect.setAsBox(args.size.x / 2, args.size.y / 2)

      val goalDef = FixtureDef()
      goalDef.shape = rect
      goalDef.isSensor = true
      goalDef.filter.set(ContactType.Event)

      body.createFixture(goalDef)

      // dispose shapes
      rect.dispose()
    }
  }
}
package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.set
import dev.wizrad.fracture.game.world.components.loader.LevelData
import dev.wizrad.fracture.game.world.core.Entity

class Spikes(
  body: Body, size: Vector2): Entity(body, size) {

  override fun update(delta: Float) {
    super.update(delta)

    if (isTouchingHero()) {
      session.failLevel()
    }
  }

  private fun isTouchingHero(): Boolean {
    return body.fixtureList.any {
      contact.any(it, ContactType.Hero)
    }
  }

  // MARK: Factory
  class Args: LevelData.Feature()

  class Factory(parent: Entity): Entity.Factory<Spikes, Args>(parent) {
    override fun entity(args: Args) = Spikes(body(args), Vector2())

    override fun defineBody(args: Args): BodyDef {
      val body = super.defineBody(args)
      body.type = BodyDef.BodyType.StaticBody
      body.position.set(parent.transform(args.center))
      return body
    }

    override fun defineFixtures(body: Body, args: Args) {
      super.defineFixtures(body, args)

      // create list of vertex arrays
      val height = args.size.y
      val n = args.size.x.toInt()

      val y = args.size.y / 2.0f
      var x = -args.size.x / 2.0f
      val vertices = (0..n-1).map { i ->
        val x2 = x + 1.0f

        val vertexes = FloatArray(6)
        vertexes[0] = x
        vertexes[1] = y
        vertexes[2] = x + 0.5f
        vertexes[3] = -y
        vertexes[4] = x2
        vertexes[5] = y

        x = x2
        vertexes
      }

      // create fixtures
      val triangle = PolygonShape()
      vertices.forEach { vertexes ->
        triangle.set(vertexes)

        val spikeDef = FixtureDef()
        spikeDef.shape = triangle
        spikeDef.isSensor = true
        spikeDef.filter.set(ContactType.Terrain)

        body.createFixture(spikeDef)
      }

      // dispose shapes
      triangle.dispose()
    }
  }
}
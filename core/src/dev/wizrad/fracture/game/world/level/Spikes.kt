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
  body: Body, args: Spikes.Args): Feature(body, args) {

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

  companion object: Factory<Spikes, Args>() {
    override fun entity(parent: Entity?, args: Args)
      = Spikes(body(parent, args), args)

    private fun body(parent: Entity?, args: Args): Body {
      if (parent == null) error("parentRequired")

      val bodyDef = BodyDef()
      bodyDef.type = BodyDef.BodyType.StaticBody
      bodyDef.gravityScale = 0.1f
      bodyDef.position.set(parent.transform(args.center))

      val body = parent.world.createBody(bodyDef)
      fixtures(body, args)

      return body
    }

    private fun fixtures(body: Body, args: Args) {
      val triangle = PolygonShape()

      // define fixtures
      makeVerticies(args.size).forEach { vertexes ->
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

    private fun makeVerticies(size: Vector2): List<FloatArray> {
       // create list of vertex arrays
      val n = size.x.toInt()
      val y = size.y / 2.0f

      var x = -size.x / 2.0f
      return (0..n-1).map { i ->
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
    }
  }
}
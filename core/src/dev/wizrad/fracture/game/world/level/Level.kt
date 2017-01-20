package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntitySequence
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.support.orientation

class Level(
  context: Context, body: Body, size: Vector2): Entity(context, body, size) {

  // MARK: Entity
  override val name = "Level"

  // MARK: Children
  val hero = Hero.Factory(context()).entity()
  val ground = Ground.Factory(context()).entity()

  // MARK: Lifecycle
  override fun children(sequence: EntitySequence) =
    super.children(sequence)
      .then(ground)
      .then(hero)

  class Factory(context: Context): Entity.Factory(context) {
    private val size: Vector2 = Vector2(10.0f, 17.75f)

    // MARK: Output
    fun entity() = Level(context, body(), size)

    // MARK: Body
    override fun defineBody(): BodyDef {
      val body = super.defineBody()
      body.type = BodyDef.BodyType.StaticBody
      body.position.set(size.cpy().scl(0.5f))
      return body
    }

    override fun defineFixtures(body: Body) {
      super.defineFixtures(body)

      val width = size.x / 2
      val height = size.y / 2
      val rect = PolygonShape()

      // create left wall
      rect.setAsBox(0.0f, height, scratch.set(-1.0f, height), 0.0f)
      val leftWall = body.createFixture(defineWall(rect))
      leftWall.orientation = Orientation.Left

      // create right wall
      rect.setAsBox(0.0f, height, scratch.set(size.x, height), 0.0f)
      val rightWall = body.createFixture(defineWall(rect))
      rightWall.orientation = Orientation.Right

      // create ceiling
      rect.setAsBox(width, 0.0f, scratch.set(width, -1.0f), 0.0f)
      val ceiling = body.createFixture(defineWall(rect))
      ceiling.orientation = Orientation.Top

      // dispose shapes
      rect.dispose()
    }

    fun defineWall(rect: PolygonShape): FixtureDef {
      val wallDef = FixtureDef()
      wallDef.shape = rect
      wallDef.density = 1.0f
      wallDef.friction = 0.2f
      wallDef.filter.categoryBits = ContactType.Wall.bits
      return wallDef
    }
  }
}

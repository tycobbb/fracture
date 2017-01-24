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
import dev.wizrad.fracture.game.world.core.EntitySequence
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.level.loader.Loader
import dev.wizrad.fracture.game.world.support.extensions.contactInfo

class Level(
  context: Context, body: Body, size: Vector2): Entity(context, body, size) {

  // MARK: Entity
  override val name = "Level"

  // MARK: Children
  val walls: List<Wall>
  val hero = Hero.Factory(context()).entity(
    center = Vector2(
      (size.x) / 2,
      (size.y - 1.0f /* floor height */ - Hero.size.y / 2)
    )
  )

  init {
    val level = Loader().load()
    val factory = Wall.Factory(context())

    walls = level.walls.map {
      factory.entity(it)
    }
  }

  override fun children(sequence: EntitySequence) =
    super.children(sequence)
      .then(walls)
      .then(hero)

  class Factory(context: Context): Entity.UnitFactory(context) {
    private val size: Vector2 = Vector2(9.0f, 16.0f)

    // MARK: Output
    fun entity() = Level(context, body(), size)

    // MARK: Body
    override fun defineBody(args: Unit): BodyDef {
      val body = super.defineBody(args)
      body.type = BodyDef.BodyType.StaticBody
      body.position.set(size.cpy().scl(0.5f))
      return body
    }

    override fun defineFixtures(body: Body, args: Unit) {
      super.defineFixtures(body, args)

      val width = size.x / 2
      val height = size.y / 2
      val rect = PolygonShape()

      // create left wall
      rect.setAsBox(0.0f, height, scratch.set(0.0f, height), 0.0f)
      val leftWall = body.createFixture(defineWall(rect))
      leftWall.contactInfo = ContactInfo.Surface(
        orientation = Orientation.Right
      )

      // create right wall
      rect.setAsBox(0.0f, height, scratch.set(size.x, height), 0.0f)
      val rightWall = body.createFixture(defineWall(rect))
      rightWall.contactInfo = ContactInfo.Surface(
        orientation = Orientation.Left
      )

      // create ceiling
      rect.setAsBox(width, 0.0f, scratch.set(width, 0.0f), 0.0f)
      val ceiling = body.createFixture(defineWall(rect))
      ceiling.contactInfo = ContactInfo.Surface(
        orientation = Orientation.Bottom
      )

      // dispose shapes
      rect.dispose()
    }

    private fun defineWall(rect: PolygonShape): FixtureDef {
      val wallDef = FixtureDef()
      wallDef.shape = rect
      wallDef.density = 1.0f
      wallDef.friction = 0.2f
      wallDef.filter.categoryBits = ContactType.Wall.bits
      return wallDef
    }
  }
}

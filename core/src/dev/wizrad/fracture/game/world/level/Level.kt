package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntitySequence
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.support.contactInfo

class Level(
  world: World): Entity(parent = null, world = world) {

  // MARK: EntityBase
  override val name = "Level"
  override val size = Vector2(10.0f, 17.75f)

  // MARK: Children
  val hero = Hero(parent = this, world = world)
  val ground = Ground(parent = this, world = world)

  // MARK: Lifecycle
  override fun children(sequence: EntitySequence) =
    super.children(sequence)
      .then(ground)
      .then(hero)

  override fun defineBody(): BodyDef {
    val body = super.defineBody()
    body.type = BodyDef.BodyType.StaticBody
    body.position.set(size.cpy().scl(0.5f))
    return body
  }

  override fun defineFixtures(body: Body) {
    super.defineFixtures(body)

    fun buildWall(rect: PolygonShape, orientation: Orientation) {
      val wallDef = FixtureDef()
      wallDef.shape = rect
      wallDef.density = 1.0f
      wallDef.friction = 0.2f
      wallDef.filter.categoryBits = ContactType.Wall.bits

      val wall = body.createFixture(wallDef)
      wall.contactInfo = ContactInfo(orientation)
    }

    val width = size.x / 2
    val height = size.y / 2
    val rect = PolygonShape()

    // create left wall
    rect.setAsBox(0.0f, height, scratch.set(-1.0f, height), 0.0f)
    buildWall(rect, Orientation.Left)

    // create left wall
    rect.setAsBox(0.0f, height, scratch.set(size.x, height), 0.0f)
    buildWall(rect, Orientation.Right)

    // create ceiling
    rect.setAsBox(width, 0.0f, scratch.set(width, -1.0f), 0.0f)
    buildWall(rect, Orientation.Top)

    // dispose shapes
    rect.dispose()
  }
}
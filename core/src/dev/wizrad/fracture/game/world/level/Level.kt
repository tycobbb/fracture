package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.ContactType
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntitySequence
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.game.world.hero.Hero

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

    val width = size.x / 2
    val height = size.y / 2
    val rect = PolygonShape()

    // create left wall
    rect.setAsBox(0.0f, height, scratch.set(-1.0f, height), 0.0f)

    val leftWall = FixtureDef()
    leftWall.shape = rect
    leftWall.density = 1.0f
    leftWall.friction = 0.2f
    leftWall.filter.categoryBits = ContactType.Wall.bits

    body.createFixture(leftWall)

    // create left wall
    rect.setAsBox(0.0f, height, scratch.set(size.x, height), 0.0f)

    val rightWall = FixtureDef()
    rightWall.shape = rect
    rightWall.density = 1.0f
    rightWall.friction = 0.2f
    rightWall.filter.categoryBits = ContactType.Wall.bits

    body.createFixture(rightWall)

    // create ceiling
    rect.setAsBox(width, 0.0f, scratch.set(width, -1.0f), 0.0f)

    val ceiling = FixtureDef()
    ceiling.shape = rect
    ceiling.density = 1.0f
    ceiling.friction = 0.2f
    ceiling.filter.categoryBits = ContactType.Ceiling.bits

    body.createFixture(ceiling)

    // dispose shapes
    rect.dispose()
  }
}
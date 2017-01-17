package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntityBase
import dev.wizrad.fracture.game.world.core.World

class Ground(
  parent: EntityBase, world: World): Entity(parent, world) {

  // MARK: EntityBase
  override val name = "Ground"
  override val size by lazy {
    Vector2(parent.size.x, 4.0f)
  }

  // MARK: Lifecycle
  override fun defineBody(): BodyDef {
    val body = super.defineBody()
    body.type = BodyType.StaticBody
    body.position.set(transform(
      x = 0.0f,
      y = parent!!.size.y - size.y
    ))

    return body
  }

  override fun defineFixtures(body: Body) {
    super.defineFixtures(body)

    // create fixtures
    val rect = PolygonShape()
    val width = size.x / 2
    val height = size.y / 2
    rect.setAsBox(width, height, scratch.set(width, height), 0.0f)

    val fixture = FixtureDef()
    fixture.shape = rect
    fixture.density = 1.0f
    fixture.friction = 0.2f

    body.createFixture(fixture)

    // dispose shapes
    rect.dispose()
  }
}
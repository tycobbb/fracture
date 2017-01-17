package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.components.controls.Key
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntityBase
import dev.wizrad.fracture.game.world.core.World
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class Hero(
  parent: EntityBase, world: World): Entity(parent, world) {

  // MARK: EntityBase
  override val name = "Hero"
  override val size = Vector2(1.0f, 1.0f)

  // MARK: Lifecycle
  override fun update(delta: Float) {
    super.update(delta)

    val force = Vector2()
    if(w.controls.pressed(Key.Left)) {
      force.x -= 20.0f
    }

    if(w.controls.pressed(Key.Right)) {
      force.x += 20.0f
    }

    body.applyForceToCenter(force, true)

    if(w.controls.pressed(Key.Jump) && canJump()) {
      val center = body.worldCenter
      debug(Tag.Physics, "jumping")
      body.applyLinearImpulse(0.0f, 30.0f, center.x, center.y, true)
    }
  }

  private fun canJump(): Boolean {
    val fixture = body.fixtureList.firstOrNull() ?: return false
    return w.contacts.count(fixture) != 0
  }

  // MARK: Body
  override fun defineBody(): BodyDef {
    val body = super.defineBody()
    body.type = BodyType.DynamicBody
    body.position.set(transform(
      x = (parent!!.size.x - size.x) / 2,
      y = 0.0f
    ))

    return body
  }

  override fun defineFixtures(body: Body) {
    super.defineFixtures(body)

    // create fixtures
    val square = PolygonShape()
    square.setAsBox(size.x, size.y)

    val fixture = FixtureDef()
    fixture.shape = square
    fixture.density = 1.0f
    fixture.restitution = 0.5f
    fixture.friction = 0.2f

    body.createFixture(fixture)

    // dispose shapes
    square.dispose()
  }
}

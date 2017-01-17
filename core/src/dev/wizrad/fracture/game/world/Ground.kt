package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import dev.wizrad.fracture.game.world.core.BaseEntity
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.World

class Ground(
  parent: BaseEntity, world: World): Entity(parent, world) {

  // MARK: BaseEntity
  override val name = "Ground"
  override val size by lazy {
    Vector2(parent.size.x, 200.0f)
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
}
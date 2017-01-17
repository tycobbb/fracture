package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef

abstract class Entity(
  parent: EntityBase, world: World): EntityBase(parent, world) {

  // MARK: Entity
  final override val center: Vector2 get() = body.position

  // MARK: Physics
  lateinit var body: Body

  // MARK: Lifecycle
  override fun initialize() {
    super.initialize()

    val body = w.physics.createBody(defineBody())
    defineFixtures(body)
    this.body = body
  }

  // MARK: Physics
  open protected fun defineBody(): BodyDef {
    return BodyDef()
  }

  open protected fun defineFixtures(body: Body) {
  }
}

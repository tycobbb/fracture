package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.debugPrefix
import dev.wizrad.fracture.support.fmt

abstract class Entity(
  /** The parent entity used to resolve local positions */
  val parent: Entity?,
  /** The world providing access to shared components */
  val world: World): Behavior() {

  // MARK: Properties
  /** A string identifier for this entity */
  abstract val name: String
  /** The physics body attached to this entity */
  lateinit var body: Body; private set
  /** Size in world coords */
  abstract val size: Vector2
  /** Absolute position in world coords */
  val center: Vector2 get() = body.position

  // MARK: Relationships
  /** Cached list for traversing children in prescribed order */
  private val children: Array<Entity> by lazy {
    children(EntitySequence()).toArray()
  }

  open fun children(sequence: EntitySequence): EntitySequence {
    return sequence
  }

  // MARK: Behavior
  override fun start() {
    debug(Tag.World, "initializing $debugPrefix")

    val body = world.physics.createBody(defineBody())
    defineFixtures(body)
    this.body = body

    @Suppress("ConvertLambdaToReference")
    children.forEach { it.start() }
  }

  override fun update(delta: Float) {
    children.forEach { it.update(delta) }
  }

  override fun step(delta: Float) {
    children.forEach { it.step(delta) }
  }

  override fun destroy() {
    @Suppress("ConvertLambdaToReference")
    children.forEach { it.destroy() }
  }

  // MARK: Physics
  open protected fun defineBody(): BodyDef {
    return BodyDef()
  }

  open protected fun defineFixtures(body: Body) {
  }

  // MARK: Geometry
  /** Transforms a vector from the local -> absolute coordinate space */
  protected fun transform(x: Float, y: Float): Vector2 {
    return scratch.set(x, y).add(parent?.center)
  }

  // MARK: Debugging
  override fun toString(): String {
    return "[$debugPrefix c=${center.fmt()}]"
  }

  // MARK: Companion
  companion object {
    val scratch = Vector2(0.0f, 0.0f)
  }
}

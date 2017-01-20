package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import dev.wizrad.fracture.game.components.controls.Controls
import dev.wizrad.fracture.game.world.components.contact.ContactGraph
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.debugPrefix
import dev.wizrad.fracture.support.fmt

abstract class Entity(
  /** World/tree context information for the entity */
  val context: Context,
  /** The physics body attached to this entity */
  val body: Body,
  /** Size in world coords */
  val size: Vector2): Behavior() {

  // MARK: Properties
  /** A string identifier for this entity */
  abstract val name: String
  /** Absolute position in world coords */
  val center: Vector2 get() = body.position

  /** The parent entity used to resolve local positions */
  protected val parent: Entity? get() = context.parent
  /** A reference to the world's shared controls */
  protected val controls: Controls get() = context.world.controls
  /** A reference to the world's shared contact graph */
  protected val contact: ContactGraph get() = context.world.contact

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

  fun context(): Context {
    return Context(world = context.world, parent = this)
  }

  // MARK: Debugging
  override fun toString(): String {
    return "[$debugPrefix c=${center.fmt()}]"
  }

  // MARK: Companion
  companion object {
    val scratch = Vector2(0.0f, 0.0f)
  }

  // MARK: Factory
  abstract class Factory(val context: Context) {
    // MARK: Properties
    val parent: Entity? get() = context.parent

    // MARK: Body
    protected fun body(): Body {
      val body = context.world.physics.createBody(defineBody())
      defineFixtures(body)
      return body
    }

    protected open fun defineBody(): BodyDef {
      return BodyDef()
    }

    protected open fun defineFixtures(body: Body) {
    }

    // MARK: Geometry
    /** Transforms a vector from the local -> absolute coordinate space */
    protected fun transform(x: Float, y: Float): Vector2 {
      return scratch.set(x, y).add(parent?.center)
    }
  }
}

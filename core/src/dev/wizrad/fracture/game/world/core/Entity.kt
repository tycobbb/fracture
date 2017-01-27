package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.debugPrefix
import dev.wizrad.fracture.support.fmt

abstract class Entity(
  /** The scene body attached to this entity */
  val body: Body,
  /** Size in scene coords */
  val size: Vector2,
  /** Reference to the scene */
  override val scene: Scene = Scene.instance): Behavior(), SceneAware {

  // MARK: Properties
  /** Absolute position in scene coords */
  val center: Vector2 get() = body.position
  /** Optional event subscription to be cleaned up on destroy */
  private var unsubscribe: (() -> Unit)? = null

  // MARK: Behavior
  override fun start() {
    super.start()
    debug(Tag.World, "initializing $debugPrefix")

    // compute initial array of children
    invalidateChildren()

    @Suppress("ConvertLambdaToReference")
    children.forEach { it.start() }
  }

  override fun update(delta: Float) {
    super.update(delta)
    children.forEach { it.update(delta) }
  }

  override fun step(delta: Float) {
    super.step(delta)
    children.forEach { it.step(delta) }
  }

  override fun lateUpdate(delta: Float) {
    super.lateUpdate(delta)
    children.forEach { it.lateUpdate(delta) }
  }

  override fun destroy() {
    // destory children
    @Suppress("ConvertLambdaToReference")
    children.forEach { it.destroy() }
    // clean up physics body
    world.destroyBody(body)
    // remove any subscriptions
    unsubscribe?.invoke()

    super.destroy()
  }

  // MARK: Events
  protected fun subscribe(subscriber: Pair<Class<*>, Function1<*, Unit>>) {
    unsubscribe = session.subscribe(subscriber)
  }

  protected fun subscribe(vararg subscribers: Pair<Class<*>, Function1<*, Unit>>) {
    unsubscribe = session.subscribe(subscribers)
  }

  // MARK: Geometry
  fun setPostion(position: Vector2) {
    body.setTransform(position, body.angle)
  }

  /** Transforms a vector from the local -> absolute coordinate space */
  fun transform(point: Vector2): Vector2 {
    return scratch.set(point).add(center)
  }

  /** Transforms a vector from the local -> absolute coordinate space */
  fun transform(x: Float, y: Float): Vector2 {
    return scratch.set(x, y).add(center)
  }

  // MARK: Relationships
  lateinit private var children: Array<Entity>

  /** Defines the sequence of child entities to update */
  open protected fun children(sequence: EntitySequence): EntitySequence {
    return sequence
  }

  /** Recomputes the sequence of child entities to update */
  protected fun invalidateChildren() {
    children = children(EntitySequence()).toArray()
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
  abstract class Factory<out E: Entity, in A> {
    abstract fun entity(parent: Entity?, args: A): E

    fun entities(parent: Entity?, args: Iterable<A>): List<E> {
      return args.map { entity(parent, args = it) }
    }
  }

  abstract class UnitFactory<out E: Entity>: Factory<E, Unit>() {
    fun entity(parent: Entity?): E = entity(parent, args = Unit)
  }
}

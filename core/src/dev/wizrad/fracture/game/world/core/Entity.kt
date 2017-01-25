package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
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

  // MARK: Geometry
  /** Transforms a vector from the local -> absolute coordinate space */
  fun transform(point: Vector2): Vector2 {
    return scratch.set(point).add(center)
  }

  /** Transforms a vector from the local -> absolute coordinate space */
  fun transform(x: Float, y: Float): Vector2 {
    return scratch.set(x, y).add(center)
  }

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
    super.start()

    debug(Tag.World, "initializing $debugPrefix")
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
    @Suppress("ConvertLambdaToReference")
    children.forEach { it.destroy() }

    super.destroy()
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
  abstract class Factory<out E, in A>(
    parent: Entity?,
    private val world: World = Scene.instance.world) {

    // MARK: Properties
    private val _parent = parent
    protected val parent: Entity get() = checkNotNull(_parent) { "$this is missing parent" }

    // MARK: Output
    abstract fun entity(args: A): E

    fun entities(args: Iterable<A>): List<E> {
      return args.map { entity(args = it) }
    }

    // MARK: Body
    protected fun body(args: A): Body {
      val definition = defineBody(args)
      val body = world.createBody(definition)
      defineFixtures(body, args)
      return body
    }

    protected open fun defineBody(args: A): BodyDef {
      return BodyDef()
    }

    protected open fun defineFixtures(body: Body, args: A) {
    }
  }

  abstract class UnitFactory<out E: Entity>(parent: Entity?): Factory<E, Unit>(parent) {
    // MARK: Output
    fun entity(): E {
      return entity(args = Unit)
    }

    // MARK: Body
    protected fun body(): Body {
      return body(args = Unit)
    }
  }
}

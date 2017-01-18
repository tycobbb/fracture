package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.debugPrefix
import dev.wizrad.fracture.support.fmt

abstract class EntityBase(
  val parent: EntityBase?,
  val w: World): Behavior() {

  // MARK: Properties
  /** A string name for this entity */
  abstract val name: String
  /** Local position relative to parent center in w coords */
  abstract val center: Vector2
  /** EntityBase size in w coords */
  abstract val size: Vector2

  // MARK: Relationships
  /** Cached list for traversing children in prescribed order */
  private val children: Array<EntityBase> by lazy {
    children(EntitySequence()).toArray()
  }

  open fun children(sequence: EntitySequence): EntitySequence {
    return sequence
  }

  @Suppress("ConvertLambdaToReference")
  open fun initialize() {
    debug(Tag.World, "initializing $debugPrefix")
    children.forEach { it.initialize() }
  }

  // MARK: Behavior
  override fun update(delta: Float) {
    children.forEach { it.update(delta) }
  }

  override fun step(delta: Float) {
    children.forEach { it.step(delta) }
  }

  override fun destroy() {
    children.forEach { it.destroy() }
  }

  // MARK: Geometry
  /** Transforms a vector from the local -> absolute coordinate space */
  protected fun transform(x: Float, y: Float): Vector2 {
    return scratch.set(x, y).add(parent?.center)
  }

  protected fun transform(vector: Vector2): Vector2 {
    return vector.add(parent?.center)
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

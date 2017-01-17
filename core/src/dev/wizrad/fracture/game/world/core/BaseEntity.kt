package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.core.Updatable
import dev.wizrad.fracture.game.world.support.EntitySequence
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug
import dev.wizrad.fracture.support.debugPrefix
import dev.wizrad.fracture.support.fmt

abstract class BaseEntity(
  val parent: BaseEntity?,
  val world: World): Updatable {

  // MARK: Properties
  /** A string name for this entity */
  abstract val name: String
  /** Local position relative to parent center in world coords */
  abstract val center: Vector2
  /** BaseEntity size in world coords */
  abstract val size: Vector2

  /** Cached list for traversing children in prescribed order */
  private val children: Array<BaseEntity> by lazy { children(EntitySequence()).toArray() }

  // MARK: Geometry
  /** Transforms a vector from the local -> absolute coordinate space */
  protected fun transform(x: Float, y: Float): Vector2 {
    return scratch.set(x, y).add(parent?.center)
  }

  protected fun transform(vector: Vector2): Vector2 {
    return vector.add(parent?.center)
  }

  // MARK: Relationships
  open fun children(sequence: EntitySequence): EntitySequence {
    return sequence
  }

  // MARK: Lifecycle
  @Suppress("ConvertLambdaToReference")
  open fun initialize() {
    debug(Tag.World, "initializing $debugPrefix")
    children.forEach { it.initialize() }
  }

  override fun update(delta: Float) {
    children.forEach { it.update(delta) }
  }

  open fun step(delta: Float) {
    children.forEach { it.step(delta) }
  }

  open fun afterStep(delta: Float) {
    children.forEach { it.afterStep(delta) }
  }

  open fun destroy() {
  }

  // MARK: Debugging
  override fun toString(): String {
    return "[$debugPrefix cen=${center.fmt()}]"
  }

  // MARK: Companion
  companion object {
    val scratch = Vector2(0.0f, 0.0f)
  }
}

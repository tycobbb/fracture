package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import dev.wizrad.fracture.game.world.components.loader.LevelData
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntitySequence

abstract class Feature(
  body: Body, args: LevelData.Feature): Entity(body, args.size) {

  // MARK: Properties
  protected val features = args.features?.let { entities(this, args = it) }
  protected var isFalling = false

  // MARK: Behavior
  // TODO: real hacky, bodies should probably attach their entity as
  // the user data so that this kind of feature so that specific hooks
  // can be called. either that, or emit an event
  override fun update(delta: Float) {
    super.update(delta)

    if (!isFalling && body.type == BodyDef.BodyType.DynamicBody) {
      isFalling = true
      features?.forEach {
        it.body.type = BodyDef.BodyType.DynamicBody
      }
    }
  }

  // MARK: Relationships
  override fun children(sequence: EntitySequence): EntitySequence {
    return super.children(sequence)
      .then(features)
  }

  // MARK: Factory
  companion object {
    fun entities(parent: Entity?, args: List<LevelData.Feature>): List<Feature> {
      return args.map { entity(parent, args = it) }
    }

    fun entity(parent: Entity?, args: LevelData.Feature) = when (args) {
      is Goal.Args -> Goal.entity(parent, args)
      is Wall.Args -> Wall.entity(parent, args)
      is Platform.Args -> Platform.entity(parent, args)
      is Spikes.Args -> Spikes.entity(parent, args)
      else -> error("attempted to create unmapped feature with args: $args")
    }
  }
}

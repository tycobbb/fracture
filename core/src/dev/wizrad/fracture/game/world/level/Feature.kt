package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Fixture
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.components.loader.LevelData
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntitySequence
import dev.wizrad.fracture.game.world.support.extensions.surface

abstract class Feature(
  body: Body, args: LevelData.Feature): Entity(body, args.size) {

  // MARK: Children
  protected val features = args.features?.let { entities(this, args = it) }

  // MARK: Behavior
  private var shouldFall = false

  override fun update(delta: Float) {
    super.update(delta)

    if (shouldFall) {
      shouldFall = false
      startFalling()
    }
  }

  // MARK: Collision
  override fun onContact(fixture: Fixture, other: Entity, otherFixture: Fixture, didStart: Boolean) {
    super.onContact(fixture, other, otherFixture, didStart)

    if (didStart && shouldFall(fixture, otherFixture)) {
      shouldFall = true
    }
  }

  private fun shouldFall(fixture: Fixture, other: Fixture): Boolean {
    return fixture.surface?.orientation == Orientation.Top
        && other.filterData.categoryBits == ContactType.Collapser.category
  }

  // MARK: Actions
  private fun startFalling() {
    body.type = BodyDef.BodyType.DynamicBody
    features?.forEach(Feature::startFalling)
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

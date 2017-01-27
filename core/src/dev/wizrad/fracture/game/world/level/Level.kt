package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.set
import dev.wizrad.fracture.game.world.components.loader.LevelData
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntitySequence

class Level(
  body: Body, size: Vector2, data: LevelData): Entity(body, size) {

  // MARK: Children
  val goal: Goal
  val walls: List<Wall>
  val platforms: List<Platform>
  val spikes: List<Spikes>

  // MARK: Lifecycle
  init {
    goal = Goal.Factory(parent = this)
      .entity(data.hotspots.goal)
    walls = Wall.Factory(parent = this)
      .entities(data.walls)
    platforms = Platform.Factory(parent = this)
      .entities(data.platforms)
    spikes = Spikes.Factory(parent = this)
      .entities(data.spikes)
  }

  override fun update(delta: Float) {
    super.update(delta)

    if (isInBlastzone()) {
      session.failLevel()
    }
  }

  private fun isInBlastzone(): Boolean {
    return body.fixtureList.any {
      contact.any(it, type = ContactType.Hero)
    }
  }

  // MARK: Relationships
  override fun children(sequence: EntitySequence): EntitySequence {
    return super.children(sequence)
      .then(walls)
      .then(platforms)
      .then(goal)
      .then(spikes)
  }

  // MARK: Factory
  data class Args(val data: LevelData, val size: Vector2, val offset: Float)

  class Factory(parent: Entity): Entity.Factory<Level, Args>(parent) {
    // MARK: Output
    override fun entity(args: Args) = Level(body(args), args.size, args.data)

    // MARK: Body
    override fun defineBody(args: Args): BodyDef {
      val body = super.defineBody(args)
      body.type = BodyDef.BodyType.StaticBody
      body.position.set(parent.transform(0.0f, args.offset))
      return body
    }

    override fun defineFixtures(body: Body, args: Args) {
      super.defineFixtures(body, args)

      val width = args.size.x / 2
      val height = args.size.y / 2
      val rect = PolygonShape()

      // create left blast zone
      rect.setAsBox(0.0f, height, scratch.set(0.0f, height), 0.0f)
      createBlastZone(body, rect)

      // create right blast zone
      rect.setAsBox(0.0f, height, scratch.set(args.size.x, height), 0.0f)
      createBlastZone(body, rect)

      // create bottom blast zone
      rect.setAsBox(width, 0.0f, scratch.set(width, args.size.y), 0.0f)
      createBlastZone(body, rect)

      // dispose shapes
      rect.dispose()
    }

    private fun createBlastZone(body: Body, rect: PolygonShape): Fixture {
      val blastZoneDef = FixtureDef()
      blastZoneDef.shape = rect
      blastZoneDef.isSensor = true
      blastZoneDef.filter.set(ContactType.Event)
      return body.createFixture(blastZoneDef)
    }
  }
}

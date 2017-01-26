package dev.wizrad.fracture.game.world.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.Orientation
import dev.wizrad.fracture.game.world.components.contact.set
import dev.wizrad.fracture.game.world.components.loader.LevelData
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.core.EntitySequence
import dev.wizrad.fracture.game.world.support.extensions.contactInfo

class Level(
  body: Body, size: Vector2, data: LevelData): Entity(body, size) {

  // MARK: Children
  val goal: Goal
  val walls: List<Wall>
  val platforms: List<Platform>

  // MARK: Lifecycle
  init {
    walls = Wall.Factory(parent = this)
      .entities(data.walls)
    platforms = Platform.Factory(parent = this)
      .entities(data.platforms)
    goal = Goal.Factory(parent = this)
      .entity(data.hotspots.goal)
  }

  override fun children(sequence: EntitySequence): EntitySequence {
    return super.children(sequence)
      .then(walls)
      .then(platforms)
      .then(goal)
  }

  // MARK: Factory
  data class Args(val data: LevelData, val offset: Float)

  class Factory(parent: Entity): Entity.Factory<Level, Args>(parent) {
    private val size: Vector2 = Vector2(9.0f, 16.0f)

    // MARK: Output
    override fun entity(args: Args) = Level(body(args), size, args.data)

    // MARK: Body
    override fun defineBody(args: Args): BodyDef {
      val body = super.defineBody(args)
      body.type = BodyDef.BodyType.StaticBody
      body.position.set(parent.transform(0.0f, args.offset))
      return body
    }

    override fun defineFixtures(body: Body, args: Args) {
      super.defineFixtures(body, args)

      val width = size.x / 2
      val height = size.y / 2
      val rect = PolygonShape()

      // create left blast zone
      rect.setAsBox(0.0f, height, scratch.set(0.0f, height), 0.0f)
      createBlastZone(body, rect, orientation = Orientation.Right)

      // create right blast zone
      rect.setAsBox(0.0f, height, scratch.set(size.x, height), 0.0f)
      createBlastZone(body, rect, orientation = Orientation.Left)

      // create top blast zone
      rect.setAsBox(width, 0.0f, scratch.set(width, 0.0f), 0.0f)
      createBlastZone(body, rect, orientation = Orientation.Bottom)

      // dispose shapes
      rect.dispose()
    }

    private fun createBlastZone(body: Body, rect: PolygonShape, orientation: Orientation): Fixture {
      val blastZoneDef = FixtureDef()
      blastZoneDef.shape = rect
      blastZoneDef.isSensor = true
      blastZoneDef.filter.set(ContactType.Event)

      val blastZone = body.createFixture(blastZoneDef)
      blastZone.contactInfo = ContactInfo.Surface(orientation = orientation)

      return blastZone
    }
  }
}

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
  val features: List<Entity>
  val walls: List<Wall>
  val collapser: Collapser

  // MARK: Lifecycle
  init {
    features = Feature.entities(parent = this, args = data.features)
    walls = Wall.entities(parent = this, args = data.walls)
    collapser = Collapser.entity(parent = this, args = Collapser.Args(
      y = size.y,
      width = size.x
    ))
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
      .then(collapser)
      .then(features)
      .then(walls)
  }

  // MARK: Factory
  data class Args(val data: LevelData, val size: Vector2, val offset: Float)

  companion object: Entity.Factory<Level, Args>() {
    override fun entity(parent: Entity?, args: Args): Level {
      return Level(body(parent, args), args.size, args.data)
    }

    private fun body(parent: Entity?, args: Args): Body {
      if (parent == null) error("parent required")

      val bodyDef = BodyDef()
      bodyDef.type = BodyDef.BodyType.StaticBody
      bodyDef.position.set(parent.transform(0.0f, args.offset))

      val body = parent.world.createBody(bodyDef)
      fixtures(body, args)

      return body
    }

    private fun fixtures(body: Body, args: Args): Body {
      val width = args.size.x / 2
      val height = args.size.y / 2
      val rect = PolygonShape()

      // left
      rect.setAsBox(0.0f, height, scratch.set(0.0f, height), 0.0f)
      createBlastZone(body, rect)

      // right
      rect.setAsBox(0.0f, height, scratch.set(args.size.x, height), 0.0f)
      createBlastZone(body, rect)

      // bottom
      rect.setAsBox(width, 0.0f, scratch.set(width, args.size.y), 0.0f)
      createBlastZone(body, rect)

      // dispose shapes
      rect.dispose()

      return body
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

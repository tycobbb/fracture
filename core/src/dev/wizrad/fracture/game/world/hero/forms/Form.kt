package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.components.statemachine.StateMachine
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.support.contactInfo

abstract class Form(
  val context: Context): StateMachine() {

  // MARK: Properties
  protected val entity: Entity get() = context.parent!!
  protected val body: Body get() = entity.body

  // MARK: Lifecycle
  protected abstract fun initialState(): State

  // MARK: Hooks
  abstract fun defineFixtures()

  // MARK: Behavior
  override fun start() {
    super.start()
    state = initialState()
  }

  // MARK: Helpers
  protected fun createBox(heroDef: FixtureDef): Fixture {
    val box = body.createFixture(heroDef)
    box.contactInfo = ContactInfo.Hero()
    return box
  }

  protected fun defineBox(polygon: PolygonShape): FixtureDef {
    polygon.setAsBox(entity.size.x / 2, entity.size.y / 2)

    val boxDef = FixtureDef()
    boxDef.shape = polygon
    boxDef.density = 1.0f
    boxDef.friction = 0.6f
    boxDef.filter.categoryBits = ContactType.Hero.bits

    return boxDef
  }

  protected fun createAppendage(polygon: PolygonShape, orientation: Orientation): Fixture {
    val size = scratch1.set(entity.size)
    when (orientation) {
      Orientation.Top, Orientation.Bottom -> size.scl(0.5f, 0.125f)
      Orientation.Left, Orientation.Right -> size.scl(0.125f, 0.5f)
    }

    val offset = scratch2.set(entity.size)
    when (orientation) {
      Orientation.Top -> offset.scl(0.0f, -0.5f)
      Orientation.Bottom -> offset.scl(0.0f, 0.5f)
      Orientation.Left -> offset.scl(-0.5f, 0.0f)
      Orientation.Right -> offset.scl(0.5f, 0.0f)
    }

    polygon.setAsBox(size.x, size.y, offset, 0.0f)

    val appendageDef = FixtureDef()
    appendageDef.isSensor = true
    appendageDef.shape = polygon
    appendageDef.filter.categoryBits = ContactType.Hero.bits

    val appendate = body.createFixture(appendageDef)
    appendate.contactInfo = ContactInfo.Appendage(orientation)

    return appendate
  }
}
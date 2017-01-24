package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
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

  protected fun createFoot(polygon: PolygonShape): Fixture {
    val size = scratch1.set(entity.size).scl(0.49f, 0.17f)
    val offset = scratch2.set(entity.size).scl(0.0f, 0.5f)
    polygon.setAsBox(size.x, size.y, offset, 0.0f)

    val footDef = FixtureDef()
    footDef.isSensor = true
    footDef.shape = polygon
    footDef.filter.categoryBits = ContactType.Hero.bits

    val foot = body.createFixture(footDef)
    foot.contactInfo = ContactInfo.Foot()

    return foot
  }
}
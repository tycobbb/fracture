package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.set
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormContext
import dev.wizrad.fracture.game.world.hero.core.FormState

class DebugForm(hero: Hero): Form(hero), FormContext {
  // MARK: Form
  override fun initialState(): State {
    return Floating(this)
  }

  override fun defineFixtures() {
    val polygon = PolygonShape()

    // create fixtures
    val boxDef = defineBox(polygon)
    boxDef.filter.set(ContactType.None)
    createBox(boxDef)

    // dispose shapes
    polygon.dispose()
  }

  // MARK: States
  class Floating(context: DebugForm): FormState<DebugForm>(context) {
    override fun start() {
      super.start()
      stopGravity()
      cancelMomentum()
    }

    override fun step(delta: Float) {
      super.step(delta)

      if (controls.touch.isActive) {
        body.setTransform(controls.touch.location, body.angle)
      }
    }

    override fun destroy() {
      super.destroy()
      startGravity()
    }

    override fun nextState() = null
  }
}

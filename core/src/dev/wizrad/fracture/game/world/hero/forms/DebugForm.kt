package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState
import dev.wizrad.fracture.game.world.support.extensions.contactInfo

class DebugForm(context: Context): Form(context) {
  // MARK: Form
  override fun initialState(): State {
    return Floating(context)
  }

  override fun defineFixtures() {
    val polygon = PolygonShape()

    // create fixtures
    val box = createBox(defineBox(polygon))
    box.contactInfo = ContactInfo.Hero(isPhasing = true)

    // dispose shapes
    polygon.dispose()
  }

  // MARK: States
  class Floating(context: Context): FormState(context) {
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

    override fun nextState(): State? = null
  }
}

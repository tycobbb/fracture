package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.support.Animation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.set
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormState

class TransitionForm(entity: Entity, target: Vector2): Form(entity) {
  // MARK: Properties
  val target = Vector2(target)

  // MARK: Form
  override fun initialState(): State {
    return Transitioning(this, target)
  }

  override fun defineFixtures() {
    val polygon = PolygonShape()

    val boxDef = defineBox(polygon)
    boxDef.filter.set(ContactType.None)
    createBox(boxDef)

    polygon.dispose()
  }

  // MARK: States
  class Transitioning(form: TransitionForm, target: Vector2): FormState<TransitionForm>(form) {
    val animation = Animation(
      start = body.position,
      end = target,
      duration = 2.0f,
      interpolation = Interpolation.pow2
    )

    override fun start() {
      super.start()
      cancelMomentum()
      stopGravity()
    }

    override fun step(delta: Float) {
      super.step(delta)

      if (!animation.isFinished) {
        body.setTransform(animation.next(delta), body.angle)
      }
    }

    override fun destroy() {
      super.destroy()
      startGravity()
    }

    override fun nextState() = null
  }
}

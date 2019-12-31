package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.support.Animation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.contact.set
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.core.Form
import dev.wizrad.fracture.game.world.hero.core.FormContext
import dev.wizrad.fracture.game.world.hero.core.FormState
import dev.wizrad.fracture.game.world.hero.forms.TransitionForm.Context
import dev.wizrad.fracture.support.Maths

class TransitionForm(context: Context): Form<Context>(context) {
  class Context(
    override val hero: Hero,
    val target: Vector2): FormContext

  // MARK: Form
  override fun initialState(): State {
    return Transitioning(context)
  }

  override fun defineFixtures() {
    val polygon = PolygonShape()

    val boxDef = defineBox(polygon)
    boxDef.filter.set(ContactType.None)
    createBox(boxDef)

    polygon.dispose()
  }

  // MARK: States
  class Transitioning(context: Context): FormState<Context>(context) {
    val translation = Animation.Vector(
      start = body.position,
      end = context.target,
      duration = 2.0f,
      interpolation = Interpolation.pow2
    )

    val rotation = Animation.Value(
      start = body.angle,
      end = Maths.F_PI * 2,
      duration = 2.0f,
      interpolation = Interpolation.swing
    )

    override fun start() {
      super.start()

      cancelMomentum()
      stopGravity()
    }

    override fun step(delta: Float) {
      super.step(delta)

      val point = if (!translation.isFinished) translation.next(delta) else body.position
      val angle = if (!rotation.isFinished) rotation.next(delta) else body.angle
      body.setTransform(point, angle)
    }

    override fun destroy() {
      startGravity()
      body.setTransform(body.position, 0.0f)

      super.destroy()
    }

    override fun nextState() = null
  }
}

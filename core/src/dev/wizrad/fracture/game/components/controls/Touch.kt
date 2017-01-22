package dev.wizrad.fracture.game.components.controls

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.components.projection.Projections
import dev.wizrad.fracture.game.components.projection.project
import dev.wizrad.fracture.game.core.Updatable

class Touch: Updatable {
  // MARK: Properties
  var location = Vector2()
  val isActive: Boolean get() = Gdx.input.isTouched

  // MARK: Updatable
  override fun update(delta: Float) {
    if (Gdx.input.isTouched) {
      val point = scratch.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
      val projected = project(point, from = Projections.touch, to = Projections.world)
      location.set(projected)
    }
  }

  companion object {
    val scratch = Vector2()
  }
}

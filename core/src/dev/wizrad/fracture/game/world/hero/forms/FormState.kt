package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.physics.box2d.Body
import dev.wizrad.fracture.game.components.controls.Controls
import dev.wizrad.fracture.game.world.components.contact.ContactGraph
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.game.world.core.Entity

abstract class FormState(
  protected val context: Context): State() {

  // MARK: Properties
  /** The entity this state is attached to */
  protected val entity: Entity get() = context.parent!!
  /** The body of this state's attached entity */
  protected val body: Body get() = entity.body
  /** A reference to the world's shared controls */
  protected val controls: Controls get() = context.world.controls
  /** A reference to the world's shared contact graph */
  protected val contact: ContactGraph get() = context.world.contact
}
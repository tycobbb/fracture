package dev.wizrad.fracture.game.world.support.extensions

import com.badlogic.gdx.physics.box2d.Body

fun Body.applyImpulseToCenter(x: Float, y: Float) {
  val center = worldCenter
  applyLinearImpulse(x, y, center.x, center.y, true)
}

fun Body.destroyAllFixtures() {
  val fixtures = fixtureList
  while (fixtures.size > 0) {
    destroyFixture(fixtures[0])
  }
}

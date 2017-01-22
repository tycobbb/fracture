package dev.wizrad.fracture.game.world.support

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.World

fun World.raycast(point1: Vector2, point2: Vector2, callback: (fixture: Fixture, point: Vector2, normal: Vector2, fraction: Float) -> Float) {
  rayCast(callback, point1, point2)
}

package dev.wizrad.fracture.support

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration
import com.badlogic.gdx.graphics.GL20
import org.jetbrains.spek.api.DescribeBody
import org.mockito.Mockito.mock

fun DescribeBody.loadGdx() {
  // bootstrap headless app
  val config = HeadlessApplicationConfiguration()
  config.renderInterval = 1.0f / 60.0f
  HeadlessApplication(TestApplication(), config)

  // mock out any necessary static objects
  Gdx.gl = mock(GL20::class.java)
}

fun <T> DescribeBody.until(value: T?, generator: () -> T?) {
  var result: T?
  do {
    result = generator()
  } while(result != value)
}

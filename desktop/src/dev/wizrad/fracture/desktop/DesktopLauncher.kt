package dev.wizrad.fracture.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import dev.wizrad.fracture.Fracture

object DesktopLauncher {
  @JvmStatic fun main(arg: Array<String>) {
    val config = LwjglApplicationConfiguration()
    config.width = 375
    config.height = 667
    LwjglApplication(Fracture(), config)
  }
}
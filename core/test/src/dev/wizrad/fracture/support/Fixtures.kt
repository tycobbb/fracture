package dev.wizrad.fracture.support

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle

fun fixture(path: String): FileHandle {
  return Gdx.files.local(path)
}

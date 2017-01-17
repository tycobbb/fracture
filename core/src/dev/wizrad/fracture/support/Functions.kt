package dev.wizrad.fracture.support

// MARK: Functional
infix inline fun <T, U, V> ((T) -> U).then(crossinline other: (U) -> V): (T) -> V {
  return { other(this(it)) }
}

// MARK: Caching
inline fun <T> cache(crossinline function: () -> T): () -> T {
  var memo: T? = null
  return {
    if(memo == null) {
      memo = function()
    }

    memo ?: memo!!
  }
}

// MARK: Optionals
inline fun <T> T?.bind(crossinline action: (T) -> Unit) {
  if(this != null) {
    action(this)
  }
}

inline fun <T, U> T?.map(crossinline transform: (T) -> U): U? {
  if(this != null) {
    return transform(this)
  } else {
    return null
  }
}

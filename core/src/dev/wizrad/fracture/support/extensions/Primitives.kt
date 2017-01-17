package dev.wizrad.fracture.support.extensions

// MARK: Comparison
fun <E: Comparable<E>> min(left: E, right: E) = if(left <= right) left else right
fun <E: Comparable<E>> max(left: E, right: E) = if(left >= right) left else right

// MARK: Repeat
inline fun Int.repeat(crossinline closure: (Int) -> Unit) {
  for(i in 0..this-1) {
    closure(i)
  }
}

package dev.wizrad.fracture.support.extensions

import dev.wizrad.fracture.support.rand
import java.util.*

// MARK: Append
fun <E, C: MutableCollection<E>> C.append(element: E): C {
  this.add(element)
  return this
}

// MARK: Find
inline fun <E, O> Iterable<E>.findMapped(transform: (E) -> O): O? {
  for(element in this) {
    val result = transform(element)
    if(result != null) {
      return result
    }
  }

  return null
}

inline fun <E, O> Array<out E>.findMapped(transform: (E) -> O): O? {
  for(element in this) {
    val result = transform(element)
    if(result != null) {
      return result
    }
  }

  return null
}

// MARK: Shuffle
/** Randomizes a copy of the list using the Fischer-Yates shuffle */
fun <E> Iterable<E>.shuffle(): List<E> {
  val copy = toMutableList()
  copy.shuffle()
  return copy.toList()
}

/** Randomizes list in-place using the Fischer-Yates shuffle */
fun <E> MutableList<E>.shuffle() {
  if(count() == 0) { return }

  // counting backwards, swap each element with a random element in the list
  for(remaining in (count()..1)) {
    // sample a random index
    val index  = remaining - 1
    val sample = rand.upto(remaining)

    // and swap the current item with the sampled one
    val swap     = this[sample]
    this[sample] = this[index]
    this[index]  = swap
  }
}

// MARK: Replace
/** Replace the elements at index in place with the new `items` */
fun <E> MutableList<E>.replace(index: Int, items: Array<E>) {
  removeAt(index)
  addAll(index, items.asList())
}

// MARK: Flat Map
inline fun <T, R> Iterable<T>.flatMapIndexed(crossinline transform: (Int, T) -> Iterable<R>): List<R> {
  return flatMapIndexedTo(ArrayList<R>(), transform)
}

inline fun <T, R, C: MutableCollection<in R>> Iterable<T>.flatMapIndexedTo(destination: C, crossinline transform: (Int, T) -> Iterable<R>): C {
  forEachIndexed { i, element -> destination.addAll(transform(i, element)) }
  return destination
}

// MARK: Generate
inline fun <T> generateIterable(crossinline generator: (Int) -> T?): Iterable<T> {
  val list = mutableListOf<T>()

  var index   = 0
  var element = generator(index++)

  while(element != null) {
    list.add(element)
    element = generator(index++)
  }

  return list
}

package dev.wizrad.fracture.support.extensions

// MARK: Generic
val <E: Comparable<E>> ClosedRange<E>.end: E get() = endInclusive

fun <E: Comparable<E>> ClosedRange<E>.clamp(value: E): E {
  return min(max(value, start), end)
}

// MARK: Double
val ClosedRange<Double>.length: Double get() = endInclusive - start
val ClosedRange<Double>.center: Double get() = start + length / 2

fun ClosedRange<Double>.without(divisor: ClosedRange<Double>): Array<ClosedRange<Double>> {
  // ensure this is a legal division
  assert(divisor.start >= start && divisor.end <= end) { "range: $this divisor doesn't fit: $divisor" }

  // calculate the remaining ranges
  val left  = if (divisor.start != start) start..divisor.start else null
  val right = if (divisor.end < end) divisor.end..end else null

  // return all non-null ranges
  return arrayOf(left, right).filterNotNull().toTypedArray()
}

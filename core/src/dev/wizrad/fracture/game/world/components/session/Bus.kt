package dev.wizrad.fracture.game.world.components.session

class Bus {
  // MARK: Properties
  private val allSubscribers = mutableMapOf<Any, MutableList<Function1<*, Unit>>>()
  private val defaultStorage = { mutableListOf<Function1<*, Unit>>() }

  // MARK: Pub/Sub
  fun <E: Any> post(event: E) {
    val subscribers = allSubscribers[event.javaClass] ?: return
    subscribers.toList().forEach { subscriber ->
      @Suppress("UNCHECKED_CAST")
      val casted = subscriber as? (E) -> Unit
      casted?.invoke(event)
    }
  }

  fun subscribe(subscriber: Pair<Class<*>, Function1<*, Unit>>): () -> Unit {
    val eventClass = subscriber.first
    val handler = subscriber.second
    val subscribers = allSubscribers.getOrPut(eventClass, defaultStorage)

    subscribers.add(handler)
    return {
      subscribers.remove(handler)
    }
  }

  fun subscribe(subscribers: Array<out Pair<Class<*>, Function1<*, Unit>>>): () -> Unit {
    val unsubscribers = subscribers.map { subscribe(it) }

    return {
      unsubscribers.forEach { unsubscriber ->
        unsubscriber.invoke()
      }
    }
  }
}

inline fun <reified E: Any> toEvent(noinline handler: (E) -> Unit): Pair<Class<*>, Function1<*, Unit>> {
  return E::class.java to handler
}

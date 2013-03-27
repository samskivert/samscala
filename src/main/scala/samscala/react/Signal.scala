//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala.react

/** A signal that emits events of type {@code T}. */
class Signal[T] extends SignalV[T] {

  /** Causes this signal to emit the supplied event to connected slots. */
  def emit (event :T) = notifyEmit(event)
}

/** Helper methods for signals. */
object Signal {

  /** Creates a signal instance. */
  def apply[T] () = new Signal[T]
}

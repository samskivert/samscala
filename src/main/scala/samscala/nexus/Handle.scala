//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala.nexus

/** A marker interface for all entities. */
trait Entity {
}

/** A marker interface for singleton entities. See [[Nexus]]. */
trait Singleton extends Entity {
}

/** A marker interface for keyed entities. See [[Nexus]]. */
trait Keyed extends Entity {
  /** Returns the key that identifies this entity (in conjunction with its concrete class). */
  def key :Comparable[_]
}

/** A handle on an entity via which actions and requests can be invoked.
  *
  * @define SAMECTX `f` will never be invoked immediately, even if the calling thread is currently
  * in this entity's execution context.
  */
trait Handle[E] {
  /** Dispatches `f` on this context's entity (on the appropriate thread). $SAMECTX */
  def invoke (f :E => Unit)

  /** Dispatches `f` on this context's entity (on the appropriate thread) and blocks the calling
    * thread until the response is available. If the calling thread is currently in this entity's
    * execution context, `f` will be invoked directly. However, this is a code smell. You should
    * know that you're in this entity's execution context and simply call methods directly. */
  def request[R] (f :E => R) :R

  /** Dispatches `f` on this context's entity (on the appropriate thread) and makes the result
    * available as a future. $SAMECTX */
  def future[R] (f :E => R) :concurrent.Future[R]
}

//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala {

  import java.util.concurrent.Executor

  /** Provides a lightweight actor-like mechanism wherein ''entities'' have an associated execution
    * context. At most one thread will ever be executing in an entity's execution context, but each
    * entity has its own execution context and thus can, and do, run in parallel.
    *
    * The difference between ''entities'' and ''actors'' is that entities do not use message
    * passing. Instead you submit ''actions'' to be invoked in an entity's context via closures.
    * For example:
    *
    * {{{
    * class Launcher extends Entity {
    *   def launchMissiles () = ...
    * }
    * val fooref = entity(new Launcher)
    * fooref invoke { foo =>
    *   foo.launchMissiles()
    * }
    * }}}
    *
    * These closures must not retain references to mutable data or non-"value" objects. In that
    * regard they are like a message sent to an actor, but they avoid the need for cumbersome
    * encoding of messages as objects and the associated expensive decoding of those messages via a
    * large series of instance tests.
    *
    * You can create handles to entities directly via [[entity]] or you can use [[Nexus]] for an
    * approach where entities are more uniformly managed and which will one day support
    * distribution across networked servers.
    */
  package object nexus {

    /** Returns a handle on the entity lazily created by `thunk`. The thunk will be executed (on the
      * entity's context thread) the first time the entity is invoked.
      * @param exec the executor on which the entity will schedule its actions. */
    def entity[E <: Entity] (thunk : => E)(implicit exec :Executor) :Handle[E] =
      new ExecContext(exec, thunk)

    /** A reference to the entity context executing on this thread (if any). */
    private[nexus] val current = new ThreadLocal[Handle[_]]()
  }
}

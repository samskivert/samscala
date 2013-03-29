//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala.nexus

import java.util.ArrayDeque
import java.util.concurrent.Executor
import scala.concurrent.duration._
import scala.concurrent.{Await, future, promise}

/** Implements the machinery of an entity's execution context. A queue of actions is maintained for
  * each entity's context and when an entity has actions to be executed, it queues itself up on an
  * executor and executes all pending actions on the executor's thread. Though a single execution
  * context may run on many different threads in its lifetime, it will never be running on multiple
  * threads at the same time. Thus the entity can maintain a single-threaded perspective with
  * regard to all of its internal state.
  */
class ExecContext[E <: Entity] (exec :Executor, entity : => E) extends Handle[E] with Runnable {

  override def invoke (op :E => Unit) {
    if (push(op)) exec.execute(this)
  }

  override def request[R] (f :E => R) = {
    if (current.get == this) {
      f(_entity)
    } else {
      // TODO: allow the timeout to be specified
      Await.result(future(f), 30 seconds)
    }
  }

  override def future[R] (f :E => R) = {
    val p = promise[R]
    invoke { e =>
      try {
        p success f(e)
      } catch {
        case t :Throwable => p failure t
      }
    }
    p.future
  }

  override def run () {
    current.set(this)
    var op = pop()
    while (op != null) {
      try op(_entity)
      catch {
        case t :Throwable => reportError(t)
      }
      op = pop()
    }
    current.set(null)
  }

  /** Logs an error that happens during async action invocation.
    * Dumps stack trace to `System.err` by default. */
  protected def reportError (t :Throwable) {
    t.printStackTrace(System.err);
  }

  private[this] def pop () = synchronized {
    val top = _ops.poll
    _active = (top != null)
    top
  }

  private[this] def push (op :E => Unit) = synchronized {
    _ops.offer(op)
    val wasActive = _active
    _active = true
    !wasActive
  }

  private lazy val _entity :E = entity

  // we only manipulate these in push() and pop(), which are synchronized
  private[this] var _active :Boolean = false
  private[this] val _ops = new ArrayDeque[E => Unit]()
}

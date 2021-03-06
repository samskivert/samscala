//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala.react

import scala.collection.mutable.ListBuffer

/** A view of a [[Signal]] on which one may listen but via which one cannot emit value.
  *
  * @define PRIODOC the priority of the connection. Higher priorities are notified first.
  * @define CONDOC an object that can be used to close the connection.
  * @define EXNDOC Throwable if a listener throws an exception it will propagate out of this method.
  * All listeners will be notified regardless of whether any of them throw exceptions. If multiple
  * listeners throw an exception, they are combined into a [[MultiFailureException]].
  */
class SignalV[T] extends Reactor[T => Unit] {
  import Impl._

  /** Maps the output of this signal via `f`. When this signal emits a value, the mapped signal will
    * emit that value as transformed by `f`. The mapped value will retain a connection to this
    * signal for as long as it has connections of its own.
    */
  def map[M] (f :T => M) :SignalV[M] = {
    val outer = this
    new MappedSignal[M]() {
      override def connect () = outer.onValue(v => notifyEmit(f(v)))
    }
  }

  /** Connects the supplied slot (side-effecting function) with priorty zero. When a value is
    * emitted, the slot will be invoked with the value.
    * @return $CONDOC
    */
  def onValue (slot :T => Unit) :Connection = addConnection(0, slot)

  /** Connects the supplied "value agnostic" block of code with priority 0. When a value is emitted,
    * the block will be executed. Useful when you don't care about the value.
    * @return $CONDOC
    */
  def onEmit (block : =>Unit) :Connection = addConnection(0, _ => block)

  /** Connects the supplied slot (side-effecting function) at the specified priority. When a value is
    * emitted, the slot will be invoked with the value.
    * @param prio $PRIODOC
    * @return $CONDOC
    */
  def onValueAt (prio :Int)(slot :T =>Unit) :Connection = addConnection(prio, slot)

  /** Connects the supplied "value agnostic" block of code at the specified priority. When a value is
    * emitted, the block will be executed. Useful when you don't care about the value.
    * @param prio $PRIODOC
    * @return $CONDOC
    */
  def onEmitAt (prio :Int)(block : =>Unit) :Connection = addConnection(prio, _ => block)

  /** Emits the supplied value to all connections. */
  protected def notifyEmit (value :T) {
    val lners = prepareNotify()
    var errs :ListBuffer[Throwable] = null
    try {
      var cons = lners
      while (cons != null) {
        try {
          cons.listener.apply(value)
        } catch {
          case t :Throwable =>
            if (errs == null) errs = ListBuffer[Throwable]()
            errs += t
        }
        if (cons.oneShot) cons.close()
        cons = cons.next
      }
    } finally {
      finishNotify(lners)
    }
    if (errs != null) throwErrors(errs)
  }
}

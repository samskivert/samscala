//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala.react

/** A container for a single value, which may be observed for changes. */
class Value[T] (init :T) extends ValueV[T] {

  /** Updates this instance with the supplied value. Registered listeners are notified only if the
    * value differs from the current value.
    *
    * @return the previous value contained by this instance.
    * @throws $EXNDOC
    */
  def update (value :T) :T = updateAndNotifyIf(value)

  /** Updates this instance with the supplied value. Registered listeners are notified regardless of
    * whether the new value is equal to the old value.
    *
    * @return the previous value contained by this instance.
    * @throws $EXNDOC
    */
  def updateForce (value :T) :T = updateAndNotify(value)

  override def get :T = _value

  override protected def updateLocal (value :T) = {
     val oldValue = _value
     _value = value
    oldValue
  }

  private[this] var _value :T = init
}

/** Helper methods for values. */
object Value {

  /** Creates an instance with the specified starting value. */
  def apply[T] (init :T) = new Value[T](init)
}

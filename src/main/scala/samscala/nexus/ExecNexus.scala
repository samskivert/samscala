//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala.nexus

import java.lang.ThreadLocal
import java.util.ArrayDeque
import java.util.concurrent.{ConcurrentHashMap, Executor}

import scala.reflect.ClassTag

/** An implementation of [[Nexus]] that creates entities on demand (the first time they are
  * referenced) and processes actions on the supplied executor.
  */
abstract class ExecNexus extends Nexus {

  override def entity[E <: Singleton] (implicit tag :ClassTag[E]) :Handle[E] =
    entity(tag.runtimeClass.asInstanceOf[Class[E]])

  /** The executor used to schedule entity actions. */
  protected val exec :Executor

  /** Creates the singleton entity for `clazz`. This may mean simply calling its constructor, or
    * resolving it via a dependency injection system. */
  protected def create[E <: Entity] (clazz :Class[E]) :E

  /** This method is called if an entity action throws an exception. Log it, ignore it, etc. */
  protected def reportError (msg :String, t :Throwable)

  // meh, concurrent hashmap makes us jump through annoying hoops
  private def entity[E <: Entity] (clazz :Class[E]) = (_singletons.get(clazz) match {
    case null =>
      val box = new Box(clazz)
      val ebox = _singletons.putIfAbsent(clazz, box)
      if (ebox == null) box else ebox
    case box => box
  }).ctx.asInstanceOf[ExecContext[E]]

  /** A map of all resolved singleton entities. */
  private[this] val _singletons = new ConcurrentHashMap[Class[_],Box]

  private class Box (clazz :Class[_]) {
    // we rely on the thread-safeness of lazy val resolution
    lazy val ctx = new ExecContext(exec, create(clazz.asInstanceOf[Class[Entity]]))
  }
}

//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala.nexus

import scala.concurrent.Future
import scala.reflect.ClassTag

/** Manages [[Singleton]] and [[Keyed]] entities and makes them available to callers via type-based
  * lookup. For example:
  * {{{
  * val nexus = ...
  * import nexus._
  *
  * // locates FooManager and invokes a method on it
  * invoke[FooManager] { _.doThings() }
  *
  * // locates UserManager, invokes method on it, and blocks until we get its reply
  * val name = "Elvis"
  * val user = request[UserManager,User] { _.findUser(name) }
  *
  * // if manually specifying the return type is too distasteful, you can use one of these
  * val user = entity[UserManager] request { _.findUser(name) }
  * val user = request { um :UserManager => um.findUser(name) }
  * }}}
  *
  * This usage pattern can also be made network transparent, where entities might reside on
  * different servers and actions and requests are serialized and delivered to the server on which
  * the entity in question resides during exceution (and responses are serialized and sent back as
  * needed). The Java framework in which these ideas originated supports this mechanism. The Scala
  * implementation currently does not, but may someday, offer such support.
  */
trait Nexus {

  /** Resolves the context for an entity which can be used to invoke or request of it.  */
  def entity[E <: Singleton] (implicit tag :ClassTag[E]) :Handle[E]

  /** Invokes `f` on the singleton entity ''e'' with type `E` in ''e'''s execution context. See
    * [[Handle.invoke]].
    */
  def invoke[E <: Singleton] (f :E => Unit)(implicit tag :ClassTag[E]) :Unit = entity(tag).invoke(f)

  /** Invokes `f` on the singleton entity ''e'' with type `E` in ''e'''s execution context, and
    * blocks waiting for the response. See [[Handle.request]].
    */
  def request[E <: Singleton, R] (f :E => R)(implicit tag :ClassTag[E]) :R = entity(tag).request(f)

  /** Invokes `f` on the singleton entity ''e'' with type `E` in ''e'''s execution context, and
    * blocks waiting for the response. See [[Handle.request]].
    */
  def future[E <: Singleton, R] (f :E => R)(implicit tag :ClassTag[E]) :Future[R] =
    entity(tag).future(f)
}

//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala.util

import java.util.logging._

/**
 * A `Log` subclass that routes logging to Java's logging API.
 */
class JavaLog (module:String) extends Log(module) {

  override protected def doLog (level :Int, fmsg :String, error :Option[Throwable]) {
    _logger.log(Levels(level), fmsg, error.orNull)
  }

  override protected def isEnabled (level :Int) =
    _logger.getLevel.intValue <= Levels(level).intValue

  private val Levels = Array(Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE)
  private val _logger = Logger.getLogger(module)
}

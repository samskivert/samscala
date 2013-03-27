//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala.util

/** Enables logging in the following useful style:
  * {{{
  * log.info("Frobulator ready", "frobbles", 5);
  * // logs: Frobulator ready [frobbles=5]
  * catch {
  * case e :FizzleException => log.warning("Fizzlewinker explosion", "winking", false, e);
  * // logs: Fizzlewinker explosion [winking=false]
  * // e.printStackTrace(...) follows log message
  * }
  * }}}
  *
  * You can integrate an instance of this class into your project in any number of ways:
  * {{{
  * // if you like to write: log.info("yay!")
  * package object myproject {
  *   val log = new samscala.util.Log("myproject")
  * }
  * // or if you like to write: Log.info("myproject")
  * object Log extends samscala.util.Log("myproject")
  * // or wire it up however you like
  * }}}
  *
  * `Log` sends log output to stdout/stderr by default. See `JavaLog` for an example of how to route
  * logging to your framework of choice.
  */
class Log (module :String) {

  /** Logs a message at the debug level.
    * @param args a series of key/value pairs and an optional final exception. */
  def debug (msg :Any, args :Any*) =
    if (isEnabled(0)) doLog(0, format(msg, args :_*), getError(args :_*))

  /** Logs a message at the info level.
    * @param args a series of key/value pairs and an optional final exception. */
  def info (msg :Any, args :Any*) =
    if (isEnabled(1)) doLog(1, format(msg, args :_*), getError(args :_*))

  /** Logs a message at the warning level.
    * @param args a series of key/value pairs and an optional final exception. */
  def warning (msg :Any, args :Any*) =
    if (isEnabled(2)) doLog(2, format(msg, args :_*), getError(args :_*))

  /** Logs a message at the error level.
    * @param args a series of key/value pairs and an optional final exception. */
  def error (msg :Any, args :Any*) =
    if (isEnabled(3)) doLog(3, format(msg, args :_*), getError(args :_*))

  /** Formats a debug message.
    * @param args key/value pairs, (e.g. "age", someAge, "size", someSize) which will be appended
    * to the log message as [age=someAge, size=someSize]. */
  def format (message :Any, args :Any*) :String = {
    val sb = new StringBuilder().append(message)
    if (args.size > 1) sb.append(" [").append(formatArgs(args)).append("]")
    sb.toString
  }

  protected def formatArgs (args :Seq[Any]) = args.grouped(2).map(_ match {
    case Seq(k, v) => Some(k + "=" + safeToString(v))
    case Seq(a) => a match {
      case ex :Throwable => None
      case arg => Some(arg + "=<odd arg>")
    }
  }).toList.flatten.mkString(", ")

  protected def safeToString (arg :Any) =
    try { String.valueOf(arg) }
    catch { case t :Throwable => "<toString() failure: " + t + ">" }

  protected def getError (args :Any*) =
    if (args.length % 2 == 0) None else args.last match {
      case exn :Throwable => Some(exn)
      case _ => None
    }

  protected def isEnabled (level :Int) = _level <= level

  protected def doLog (level :Int, fmsg :String, error :Option[Throwable]) {
    val sb = new StringBuffer
    _date.setTime(System.currentTimeMillis)
    _format.format(_date, sb, _fpos)
    sb.append(" ").append(LevelNames(level)).append("/").append(module).append(": ")
    sb.append(fmsg)
    val out = if (level > 1) System.err else System.out
    out.println(sb)
    error foreach { _.printStackTrace(out) }
  }

  private var _level = 1
  private val _date = new java.util.Date
  private val _format = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS")
  private val _fpos = new java.text.FieldPosition(java.text.DateFormat.DATE_FIELD)
  private val LevelNames = Array("D", "I", "W", "E")
}

//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala.react

import java.io.{PrintStream, PrintWriter}

/** An exception thrown to communicate multiple listener failures. See [[Signal.emit]] for details
  * on when this is thrown .
  */
class MultiFailureException (val failures :Seq[Throwable]) extends RuntimeException {

  override def getMessage = {
    val buf = new StringBuilder
    for (failure <- failures) {
      if (buf.length > 0) buf.append(", ")
      buf.append(failure.getClass.getName).append(": ").append(failure.getMessage)
    }
    failures.size + " failures: " + buf
  }

  override def printStackTrace (s :PrintStream) {
    failures foreach { _.printStackTrace(s) }
  }

  override def printStackTrace (w :PrintWriter) {
    failures foreach { _.printStackTrace(w) }
  }

  override def fillInStackTrace () :Throwable = this // no stack trace here
}

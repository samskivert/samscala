//
// Samscala - Scala utility code that samskivert uses in his projects
// https://github.com/samskivert/samscala

package samscala.nexus

import java.util.concurrent.{ExecutorService, Executor, Executors, TimeUnit}

import org.junit._
import org.junit.Assert._

class TestSingleton extends Singleton {
  def increment (box :Array[Int]) = box(0) += 1
  def add (a :Int, b :Int) = a + b
}

class NexusTest {
  class TestNexus extends ExecNexus {
    def shutdown = exec match {
      case esvc :ExecutorService =>
        esvc.shutdown()
        esvc.awaitTermination(5, TimeUnit.SECONDS)
    }

    override protected val exec = Executors.newFixedThreadPool(2)
    override protected def create[E] (clazz :Class[E]) = clazz.newInstance.asInstanceOf[E]
    override protected def reportError (msg :String, t :Throwable) {
      println(msg)
      t.printStackTrace(System.out)
    }
  }

  @Test def testEntity {
    implicit val exec = new Executor {
      def execute (r :Runnable) = r.run()
    }
    val ref = entity(new TestSingleton)
    val calls = Array(0)
    ref.invoke { _.increment(calls) }
    ref.invoke { _.increment(calls) }
    assertEquals(2, calls(0))
    assertEquals(4, ref.request { _.add(2, 2) })
  }

  @Test def testInvoke {
    val nexus = new TestNexus
    import nexus._

    val calls = Array(0)
    invoke[TestSingleton] { _.increment(calls) }
    invoke[TestSingleton] { _.increment(calls) }
    shutdown
    assertEquals(2, calls(0))
  }

  @Test def testRequest {
    val nexus = new TestNexus
    import nexus._

    // hrm, I don't like either of these... how to improve?
    assertEquals(4, request { e :TestSingleton => e.add(2, 2) })
    assertEquals(4, request[TestSingleton,Int] { _.add(2, 2) })
    // this could be made tolerable if we could omit the apply...
    assertEquals(4, entity[TestSingleton] request { _.add(2, 2) })
    shutdown
  }
}

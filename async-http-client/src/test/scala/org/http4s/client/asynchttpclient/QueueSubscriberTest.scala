package org.http4s
package client
package asynchttpclient

import org.http4s.compat._
import org.reactivestreams.tck.SubscriberWhiteboxVerification.WhiteboxSubscriberProbe
import org.reactivestreams.tck.{SubscriberWhiteboxVerification, TestEnvironment}
import org.reactivestreams.{Publisher, Subscriber, Subscription}
import org.testng.Assert._
import org.testng.annotations._

import java.util.concurrent.atomic.AtomicInteger

class QueueSubscriberTest extends SubscriberWhiteboxVerification[Integer](new TestEnvironment) {
  private lazy val counter = new AtomicInteger

  override def createSubscriber(theProbe: WhiteboxSubscriberProbe[Integer]): Subscriber[Integer] = {
    val subscriber = new QueueSubscriber[Integer](2) with WhiteboxSubscriber[Integer] {
      override def probe: WhiteboxSubscriberProbe[Integer] = theProbe
    }
    subscriber
  }

  def createSubscriber(): QueueSubscriber[Integer] =
    new QueueSubscriber[Integer](1)

  override def createElement(element: Int): Integer =
    counter.getAndIncrement

  @Test
  def emitsToProcess() = {
    val publisher = createHelperPublisher(10)
    val subscriber = createSubscriber()
    publisher.subscribe(subscriber)
    assertEquals(subscriber.process.runLog.run.size, 10)
  }

  @Test
  def failsProcessOnError() = {
    object SadTrombone extends Exception
    val publisher = new Publisher[Integer] {
      override def subscribe(s: Subscriber[_ >: Integer]): Unit = {
        s.onSubscribe(new Subscription {
          override def cancel(): Unit = {}
          override def request(n: Long): Unit = {}
        })
        s.onError(SadTrombone)
      }
    }
    val subscriber = createSubscriber()
    publisher.subscribe(subscriber)
    assertEquals(subscriber.process.runLog.attemptRun, SadTrombone.left)
  }

  @Test
  def closesQueueOnComplete() = {
    object SadTrombone extends Exception
    val publisher = new Publisher[Integer] {
      override def subscribe(s: Subscriber[_ >: Integer]): Unit = {
        s.onSubscribe(new Subscription {
          override def cancel(): Unit = {}
          override def request(n: Long): Unit = {}
        })
        s.onComplete()
      }
    }
    val subscriber = createSubscriber()
    publisher.subscribe(subscriber)
    assertEquals(subscriber.process.runLog.run, Vector.empty)
  }
}

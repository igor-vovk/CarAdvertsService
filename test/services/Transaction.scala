package services

import scala.concurrent.{ExecutionContext, Future}

trait Transaction[T] {

  def get: Future[T]

  def update(f: T => T): Future[T]

  def updateAndGet[A](f: T => (T, A)): Future[A]

}

class SynchronizedTransactionImpl[T](initial: T)(implicit ec: ExecutionContext) extends Transaction[T] {

  final val mutex = new Object

  private var value: T = initial

  override def get = Future {
    mutex.synchronized {
      value
    }
  }

  override def update(f: T => T): Future[T] = {
    // Implement through updateAndGet
    updateAndGet(v => {
      val a = f(v)

      (a, a)
    })
  }

  override def updateAndGet[A](f: (T) => (T, A)): Future[A] = {
    Future {
      mutex.synchronized {
        val (newVal, res) = f(value)

        value = newVal

        res
      }
    }
  }
}

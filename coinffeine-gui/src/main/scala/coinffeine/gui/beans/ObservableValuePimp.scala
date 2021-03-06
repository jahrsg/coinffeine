package coinffeine.gui.beans

import java.util.concurrent.Callable
import javafx.beans.binding._
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.ObservableList

class ObservableValuePimp[A](val observableValue: ObservableValue[A]) extends AnyVal {

  /** Maps an observable value into a new one.
    *
    * Note: you should either bind the returned value or call {{{dispose()}}} to avoid leaking
    * memory.
    */
  def map[S](f: A => S): ObjectBinding[S] = Bindings.createObjectBinding(
    new Callable[S] {
      override def call() = f(observableValue.getValue)
    },
    observableValue)

  def flatMap[S](f: A => ObservableValue[S]) = Bindings.createObjectBinding(
    new Callable[S] {
      override def call() = f(observableValue.getValue).getValue
    },
    new ObservableBeanProperty[A](observableValue, f))

  def bindToList[B](list: ObservableList[B])(f: A => Seq[B]): Unit = {
    observableValue.addListener(new ChangeListener[A] {
      override def changed(observable: ObservableValue[_ <: A], oldValue: A, newValue: A) = {
        list.setAll(f(newValue): _*)
      }
    })
    list.setAll(f(observableValue.getValue): _*) // ensure last values are set
  }

  def zip[B, S](b: ObservableValue[B])
               (f: (A, B) => S): ObjectBinding[S] = Bindings.createObjectBinding(
    new Callable[S] {
      override def call() = f(observableValue.getValue, b.getValue)
    },
    observableValue, b)

  def zip[B, C, S](b: ObservableValue[B], c: ObservableValue[C])
                  (f: (A, B, C) => S): ObjectBinding[S] = Bindings.createObjectBinding(
    new Callable[S] {
      override def call() = f(observableValue.getValue, b.getValue, c.getValue)
    },
    observableValue, b, c)
}

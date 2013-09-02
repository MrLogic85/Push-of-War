package com.sleepyduck.pushofwar.communication

import scala.collection.mutable.Queue

object Mailbox {
	private var MailBox: Mailbox = New

	def New = {
		MailBox = new Mailbox()
		MailBox
	}

	def Get = MailBox
}

class Mailbox(messages: Queue[(Manifest[_], Message)] = new Queue[(Manifest[_], Message)]()) {

	def pop: Option[Message] = synchronized(if (messages.isEmpty) None else Option apply messages.dequeue._2)

	def add[T <: Message](message: T)(implicit m: Manifest[T]) = synchronized(messages += m -> message)

	def foreach[T](f: Message => T) = while (!messages.isEmpty) pop foreach f

	def addUnique[T <: Message](message: T)(implicit m: Manifest[T]) = {
		removeSimilar[T]
		this add message
	}

	def removeSimilar[T <: Message](implicit m: Manifest[T]) = {
		synchronized(messages.dequeueAll(message => if (message._1 <:< m) true else false))
	}

	override def toString = {
		var out = "Mailbox ["
		synchronized(messages foreach (out += _._2 + " "))
		out += "]"
		out
	}
}
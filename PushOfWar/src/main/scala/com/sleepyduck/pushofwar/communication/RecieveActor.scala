package com.sleepyduck.pushofwar.communication

import com.sleepyduck.pushofwar.PushOfWarTest
import com.sleepyduck.pushofwar.util.OtherStarted
import com.sleepyduck.pushofwar.util.Player2
import akka.actor.Actor
import com.sleepyduck.pushofwar.util.Player1

class RecieveActor(callback: PushOfWarTest) extends Actor {
	def receive = {
		case m: Message => {
			println("Recieved message " + m)
			Mailbox.Get addUnique m
		}
		case _ =>
	}
}
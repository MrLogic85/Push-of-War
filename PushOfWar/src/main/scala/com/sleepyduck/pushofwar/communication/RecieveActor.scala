package com.sleepyduck.pushofwar.communication

import com.sleepyduck.pushofwar.PushOfWarTest
import com.sleepyduck.pushofwar.util.OtherStarted
import com.sleepyduck.pushofwar.util.Player2
import akka.actor.Actor
import com.sleepyduck.pushofwar.util.Player1

class RecieveActor(callback: PushOfWarTest) extends Actor {
	def receive = {
		case d: Done =>
			if (!PushOfWarTest.IsRunning) {
				PushOfWarTest loadPlayer1ToPlayer2From d.data
				Player2.IsDone = true
				if (Player1.IsDone) {
					PushOfWarTest.doLoadFromFile = false
					callback._load
				}
			}
		case Reset =>
			Player2.IsDone = false
			if (PushOfWarTest.IsRunning)
				callback.reset
		case Run =>
			if (!PushOfWarTest.IsRunning) {
				callback.whoStarted = OtherStarted
				callback.quicksave
				callback.start
			}
			PushOfWarTest.IsRunning = true
		case m: Synch =>
			if (PushOfWarTest.IsRunning)
				callback synchFrom m.data
		case m: Message => println("Unhandled message " + m)
		case _ => println("Unhandled message")
	}
}
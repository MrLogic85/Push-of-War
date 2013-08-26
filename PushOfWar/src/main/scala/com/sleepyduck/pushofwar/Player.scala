package com.sleepyduck.pushofwar

class Player {
	var Points = 0

	def TryPay(cost: Int) = {
		if (Points >= cost) {
			Points -= cost
			true
		} else false
	}
	
	def SetPoints(points:Int) = {
		Points = points
	}
	
	def GiverPoints(points:Int) = {
		Points += points
	}
}

object Player extends Player {
	def Get(i: Int) = i match {
		case 1 => Player1
		case 2 => Player2
		case _ => Player
	}
}

object Player1 extends Player {

}

object Player2 extends Player {

}
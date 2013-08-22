package com.sleepyduck.pushofwar.model

trait CollisionNormal {
	def getCollisionGroup(playerId:Int) = playerId match {
			case 1 => CollissionGroupPlayer1
			case 2 => CollissionGroupPlayer2
	}
}

trait CollisionHard extends CollisionNormal {
	override def getCollisionGroup(playerId:Int) = playerId match {
			case 1 => CollissionGroupPlayer1Alt
			case 2 => CollissionGroupPlayer2Alt
	}
}

trait CollisionNone extends CollisionNormal {
	override def getCollisionGroup(playerId:Int) = playerId match {
			case 1 => CollissionGroupPlayer1None
			case 2 => CollissionGroupPlayer2None
	}
}
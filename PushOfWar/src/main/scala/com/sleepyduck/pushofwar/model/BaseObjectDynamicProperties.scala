package com.sleepyduck.pushofwar.model

trait CollisionNormal {
	def getCollisionGroup(playerId: Int) = playerId match {
		case 1 => CollissionGroupPlayer1
		case 2 => CollissionGroupPlayer2
	}
}

trait CollisionHard extends CollisionNormal {
	override def getCollisionGroup(playerId: Int) = playerId match {
		case 1 => CollissionGroupPlayer1Alt
		case 2 => CollissionGroupPlayer2Alt
	}
}

trait CollisionNone extends CollisionNormal {
	override def getCollisionGroup(playerId: Int) = playerId match {
		case 1 => CollissionGroupPlayer1None
		case 2 => CollissionGroupPlayer2None
	}
}

trait Cost {
	def Cost = 0
}

trait Cost0 extends Cost {
	override def Cost = 0
}

trait Cost2 extends Cost {
	override def Cost = 2
}

trait Cost10 extends Cost {
	override def Cost = 10
}

trait Cost20 extends Cost {
	override def Cost = 20
}

trait Cost50 extends Cost {
	override def Cost = 50
}
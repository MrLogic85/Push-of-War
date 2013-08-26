package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Filter
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.ChainShape

class BaseObjectPlayer(pow: PushOfWarTest, x: Float = 0, y: Float = 0, angle: Float = 0)
	extends BaseObjectDynamic(pow, x, y, angle, true) with CollisionHard {

	def copy = null
	override def clusterCopy = null

	def getShape = new PolygonShape {
		setAsBox(3, 3)
	}
	
	override def isPlayerBox = true
}
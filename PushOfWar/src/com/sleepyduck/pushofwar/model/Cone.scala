package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Filter
import org.jbox2d.collision.shapes.PolygonShape

class Cone(pow: PushOfWarTest, collisionGroup: Filter, x: Float = 0, y: Float = 0, base: Float)
	extends BaseObjectDynamic(pow, collisionGroup, x, y) {

	def getShape = new PolygonShape {
		val bottom = -base / 2.0F * (Math.tan(Math.PI / 6) toFloat)
		val top = base / 2.0F * (Math.tan(Math.PI / 3) toFloat) + bottom
		val vertices = new Array[Vec2](3)
		vertices(0) = new Vec2(-base/2,-base/6)
		vertices(1) = new Vec2(base/2,0)
		vertices(2) = new Vec2(-base/2,base/6)
		set(vertices, 3)
	}

	def copy = new Cone(pow, collisionGroup, x, y, base)
}
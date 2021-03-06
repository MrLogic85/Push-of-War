package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Filter
import org.jbox2d.collision.shapes.PolygonShape

object Cone extends AnyRef with Cost10

class Cone(pow: PushOfWarTest, x: Float = 0, y: Float = 0, angle: Float = 0)
	extends BaseObjectDynamic(pow, x, y, angle) with Cost10 {

	def getShape = new PolygonShape {
		val bottom = -3.0F * (Math.tan(Math.PI / 6) toFloat)
		val top = 3.0F * (Math.tan(Math.PI / 3) toFloat) + bottom
		val vertices = new Array[Vec2](3)
		vertices(0) = new Vec2(-3, -1)
		vertices(1) = new Vec2(3, 0)
		vertices(2) = new Vec2(-3, 1)
		set(vertices, 3)
	}

	def copy = new Cone(pow, body.getWorldCenter().x, body.getWorldCenter().y, body.getAngle())
}
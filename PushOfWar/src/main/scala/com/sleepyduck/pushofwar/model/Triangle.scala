package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.dynamics.Filter
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.common.Vec2
import org.jbox2d.collision.shapes.PolygonShape

object Triangle extends AnyRef with Cost10

class Triangle(pow: PushOfWarTest, x: Float = 0, y: Float = 0, angle: Float = 0)
	extends BaseObjectDynamic(pow, x, y, angle) with Cost10 {

	def getShape = new PolygonShape {
		val bottom = -1.5F * (Math.tan(Math.PI / 6) toFloat)
		val top = 1.5F * (Math.tan(Math.PI / 3) toFloat) + bottom
		val vertices = new Array[Vec2](3)
		vertices(0) = new Vec2(-1.5F, bottom)
		vertices(1) = new Vec2(1.5F, bottom)
		vertices(2) = new Vec2(0, top)
		set(vertices, 3)
	}

	def copy = new Triangle(pow, body.getWorldCenter().x, body.getWorldCenter().y, body.getAngle())
}
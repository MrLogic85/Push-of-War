package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Filter
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.ChainShape

case class BarHard(pow: PushOfWarTest, collisionGroup: Filter, x: Float = 0, y: Float = 0, w: Float = 1, h: Float)
	extends BaseObjectDynamic(pow, collisionGroup, x, y) {
	
	body getFixtureList() setDensity 10

	def copy = new BarHard(pow, collisionGroup, x, y, w, h)

	def getShape = new PolygonShape {
		val vertices = new Array[Vec2](8)
		for (i <- 0 until 4) vertices(i) = new Vec2((w - h) / 2 + h / 2 * Math.sin(Math.PI * i / 3).toFloat, h / 2 * Math.cos(Math.PI * i / 3).toFloat)
		for (i <- 0 until 4) vertices(i + 4) = new Vec2((h - w) / 2 - h / 2 * Math.sin(Math.PI * i / 3).toFloat, -h / 2 * Math.cos(Math.PI * i / 3).toFloat)
		set(vertices, 8)
	}
	
	override def getExtraFixture = {
		List(new FixtureDef {
			friction = 0
			density = 0
			shape = new ChainShape {
				val vertices = Array.ofDim[Vec2](2)
				vertices(0) = new Vec2((h-w)/2, 0)
				vertices(1) = new Vec2((w-h)/2,0)
				createChain(vertices, vertices.length)
			}
			filter = new Filter {
				categoryBits = 0
				maskBits = 0
			}
		})
	}
}
package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Filter
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.ChainShape

class BarHard(pow: PushOfWarTest, collisionGroup: Filter, x: Float = 0, y: Float = 0, w: Float = 10, h: Float = 1, copied: Boolean = false)
	extends Bar(pow, collisionGroup, x, y, w, h, copied) {
	
	body getFixtureList() setDensity 10

	override def copy = new BarHard(pow, collisionGroup, x, y, w, h)
	
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
package com.sleepyduck.pushofwar.model

import org.jbox2d.testbed.framework.TestbedSettings
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.PolygonShape
import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.dynamics.Filter
import org.jbox2d.common.Vec2

class StaticBox(pow: PushOfWarTest, collisionGroup: Filter, w: Float = 1, angle: Float = 3)
	extends BaseObject(pow, collisionGroup, 0, 0, 0) {

	def getFixture = new FixtureDef {
		density = 1
		shape = new PolygonShape {
			val slope = Math.tan(Math.PI / 180 * angle).toFloat * w / 4
			val vertices = new Array[Vec2](8)
			vertices(0) = new Vec2(-w / 2, -slope * 6)
			vertices(1) = new Vec2(-w * 5 / 14, -slope * 3)
			vertices(2) = new Vec2(-w * 3 / 14, -slope)
			vertices(3) = new Vec2(-w / 14, 0)
			vertices(4) = new Vec2(w / 14, 0)
			vertices(5) = new Vec2(w * 3 / 14, -slope)
			vertices(6) = new Vec2(w * 5 / 14, -slope * 3)
			vertices(7) = new Vec2(w / 2, -slope * 6)
			set(vertices, vertices.length)
		}
	}

	body setType BodyType.STATIC
}
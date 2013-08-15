package com.sleepyduck.pushofwar.model

import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.dynamics.joints.Joint
import org.jbox2d.common.Transform
import org.jbox2d.dynamics.joints.JointDef
import org.jbox2d.dynamics.joints.JointType
import org.jbox2d.dynamics.joints.PulleyJointDef
import org.jbox2d.dynamics.joints.RevoluteJointDef
import scala.collection.mutable.ArrayBuffer

case class Bar(pow: PushOfWarTest, x: Float = 0, y: Float = 0, w: Float = 1, h: Float, friction: Float = 1F, density: Float = 1F)
	extends BaseObjectDynamic(pow) {

	body = pow getWorld () createBody new BodyDef {
		`type` = BodyType.DYNAMIC
		position set (x, y)
		gravityScale = 0
	}

	body createFixture new FixtureDef {
		friction = Bar.this.friction
		density = Bar.this.density
		isSensor = true
		shape = new PolygonShape {
			val vertices = new Array[Vec2](8)
			for (i <- 0 until 4) vertices(i) = new Vec2((w - h) / 2 + h / 2 * Math.sin(Math.PI * i / 3).toFloat, h / 2 * Math.cos(Math.PI * i / 3).toFloat)
			for (i <- 0 until 4) vertices(i + 4) = new Vec2((h - w) / 2 - h / 2 * Math.sin(Math.PI * i / 3).toFloat, -h / 2 * Math.cos(Math.PI * i / 3).toFloat)
			set(vertices, 8)
		}
	}

	def copy = new Bar(pow, x, y, w, h, friction, density)
}
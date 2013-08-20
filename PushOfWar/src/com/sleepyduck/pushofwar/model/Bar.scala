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
import org.jbox2d.dynamics.Filter
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.xml.XMLElement

class Bar(pow: PushOfWarTest, collisionGroup: Filter = CollissionGroupNone, x: Float = 0, y: Float = 0, w: Float = 10, h: Float = 1, copied: Boolean = false)
	extends BaseObjectDynamic(pow, collisionGroup, x, y, copied) {

	def copy = new Bar(pow, collisionGroup, x, y, w, h)

	def getShape = new PolygonShape {
		val vertices = new Array[Vec2](8)
		for (i <- 0 until 4) vertices(i) = new Vec2((w - h) / 2 + h / 2 * Math.sin(Math.PI * i / 3).toFloat, h / 2 * Math.cos(Math.PI * i / 3).toFloat)
		for (i <- 0 until 4) vertices(i + 4) = new Vec2((h - w) / 2 - h / 2 * Math.sin(Math.PI * i / 3).toFloat, -h / 2 * Math.cos(Math.PI * i / 3).toFloat)
		set(vertices, 8)
	}

	override def putAttributes(element: XMLElement) = {
		super.putAttributes(element)
		element addAttribute ("w", w toString)
		element addAttribute ("h", h toString)
	}
}
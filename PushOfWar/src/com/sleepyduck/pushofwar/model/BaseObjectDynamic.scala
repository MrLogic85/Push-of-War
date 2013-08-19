package com.sleepyduck.pushofwar.model

import org.jbox2d.dynamics.joints.RevoluteJointDef
import org.jbox2d.common.Transform
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.joints.Joint
import scala.collection.mutable.ArrayBuffer
import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.dynamics.Filter
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.dynamics.FixtureDef

abstract class BaseObjectDynamic(pow: PushOfWarTest, collisionGroup: Filter, x: Float, y: Float) extends BaseObject(pow, collisionGroup, x, y) {
	var hasBeenCopied = false

	override def getFixture = new FixtureDef {
		friction = 1
		density = 1
		isSensor = true
		shape = getShape
	}

	override def mouseDown(p: Vec2) = {
		super.mouseDown(p)
		if (!hasBeenCopied) {
			pow addObject (this copy)
			hasBeenCopied = true
			body getFixtureList () setSensor false
		}
	}

	def activate = {
		if (hasBeenCopied) {
			body setGravityScale 1
			body setLinearDamping 0
			body setAngularDamping 0
			body setAwake true
			body setSleepingAllowed false
		}
	}

	def intersectingObjects(p: Vec2) = {
		val p2 = Transform mul (body getTransform (), p)
		pow findObjects (p2) map ((_, p2))
	}

	def stop = {
		if (body.getGravityScale() < 0.1F) {
			body setLinearVelocity new Vec2(0, 0)
			body setAngularVelocity 0
		}
	}

	def contains(p: Vec2) = (Option apply (body getFixtureList ())) map (_ testPoint (p)) getOrElse false

	def copy: BaseObjectDynamic

	def isSpike = false

	def getShape: Shape
}
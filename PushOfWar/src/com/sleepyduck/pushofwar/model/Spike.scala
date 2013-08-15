package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.dynamics.BodyType
import scala.collection.mutable.ArrayBuffer
import org.jbox2d.dynamics.joints.Joint
import org.jbox2d.dynamics.joints.RevoluteJointDef
import org.jbox2d.dynamics.joints.RevoluteJoint
import org.jbox2d.common.Vec2

class Spike(pow: PushOfWarTest, x: Float = 0, y: Float = 0) extends BaseObjectDynamic(pow) {

	val joints: ArrayBuffer[Option[RevoluteJoint]] = ArrayBuffer[Option[RevoluteJoint]]()

	override def mouseDown = {
		super.mouseDown
		clearJoints
	}

	override def mouseUp = {
		super.mouseUp
		clearJoints
		createJoints
	}

	def clearJoints = {
		joints foreach (_ foreach (pow getWorld () destroyJoint _))
		joints clear ()
	}

	def createJoints = {
		joints clear ()
		val objs = pow findObjects (body getWorldCenter ())
		joints ++= (for {
			o1 <- objs
			o2 <- objs
		} yield addJoint(o1, o2))
	}

	def addJoint(o1: BaseObjectDynamic, o2: BaseObjectDynamic) = {
		if (o1 != o2) Option apply (pow getWorld () createJoint new RevoluteJointDef() {
			initialize(o1.body, o2.body, body getWorldCenter ())
			jointDefCreated(this)
		}).asInstanceOf[RevoluteJoint]
		else None
	}

	def jointDefCreated(jointDef: RevoluteJointDef) = {}

	body = pow getWorld () createBody new BodyDef {
		`type` = BodyType.DYNAMIC
		position set (x, y)
		gravityScale = 0
	}

	body createFixture new FixtureDef {
		friction = 1
		density = 1
		shape = new CircleShape
		shape setRadius 0.1F
		isSensor = true
	}

	def copy = new Spike(pow, x, y)
}
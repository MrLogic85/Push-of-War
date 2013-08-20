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
import org.jbox2d.dynamics.Filter

class Spike(pow: PushOfWarTest, collisionGroup: Filter, x: Float = 0, y: Float = 0, copied: Boolean = false)
	extends BaseObjectDynamic(pow, collisionGroup, x, y, copied) {

	val joints: ArrayBuffer[Option[RevoluteJoint]] = ArrayBuffer[Option[RevoluteJoint]]()

	override def mouseDown(p: Vec2) = {
		super.mouseDown(p)
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
		val objs = pow findObjects (body getWorldCenter ()) filter (obj => !(obj.get.isSpike) && obj.get.hasBeenCopied)
		//objs prepend (Option apply this)
		//for (i <- 0 until objs.length - 1) (joints += addJoint(objs(i), objs(i + 1)))
		for (i <- 0 until objs.length) (joints += addJoint(Option apply this, objs(i)))
		for (i <- 0 until objs.length - 1) (joints += addJoint(objs(i), objs(i + 1)))
		jointsCreated
	}

	def jointsCreated = {}

	def addJoint(o1: Option[BaseObjectDynamic], o2: Option[BaseObjectDynamic]) = {
		if (o1 != o2 && (o1 isDefined) && (o2 isDefined)) Option apply (pow getWorld () createJoint new RevoluteJointDef() {
			initialize((o1 get).body, (o2 get).body, body getWorldCenter ())
		}).asInstanceOf[RevoluteJoint]
		else None
	}

	def copy = new Spike(pow, collisionGroup, x, y)

	def getShape = new CircleShape { this setRadius 0.3F }

	override def isSpike = true
}
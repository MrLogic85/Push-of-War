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
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.xml.Attribute
import com.sleepyduck.xml.XMLElement

class Spike(pow: PushOfWarTest, collisionGroup: Filter = CollissionGroupNone, x: Float = 0, y: Float = 0, copied: Boolean = false)
	extends BaseObjectDynamic(pow, collisionGroup, x, y, copied) {

	val objects = ArrayBuffer[BaseObjectDynamic]()
	val joints = ArrayBuffer[RevoluteJoint]()
	var xmlElement:Option[XMLElement] = None

	override def mouseDown(p: Vec2) = {
		super.mouseDown(p)
		clearJoints
	}

	override def mouseUp = {
		super.mouseUp
		createJoints()
	}

	def clearJoints = {
		joints foreach (pow getWorld () destroyJoint _)
		joints clear ()
		objects.clear()
	}

	def createJoints(objectsArg: ArrayBuffer[BaseObjectDynamic] = getObjects) = {
		clearJoints
		objects ++= objectsArg
		for (obj <- objects) (joints += addJoint(this, obj))
		for (i <- 0 until objects.length - 1) (joints += addJoint(objects(i), objects(i + 1)))
	}

	def getObjects = pow findObjects (body getWorldCenter ()) filter (obj => obj != this && !(obj isSpike) && (obj hasBeenCopied))

	def addJoint(o1: BaseObjectDynamic, o2: BaseObjectDynamic) = (pow getWorld () createJoint new RevoluteJointDef() {
		initialize(o1.body, o2.body, body getWorldCenter ())
	}).asInstanceOf[RevoluteJoint]

	def copy = new Spike(pow, collisionGroup, x, y)

	def getShape = new CircleShape { this setRadius 0.3F }
	
	override def initialize(element:XMLElement) = {
		super.initialize(element)
		xmlElement = Option apply element
	}
	
	override def initializeJoints = {
		super.initializeJoints
		val objs = new ArrayBuffer[BaseObjectDynamic]()
		xmlElement map (el => (el getElement "Joints")) map (_.children) foreach (_ foreach (child => pow getObject (child.getAttribute("id").value.toInt) foreach (objs += _)))
		createJoints(objs)
	}

	override def isSpike = true

	override def putAttributes(element: XMLElement) = {
		super.putAttributes(element)
		if (joints.length > 0)
			element addChild (new XMLElement(
				name = "Joints",
				children = (objects map (obj => new XMLElement(
					name = "Joint",
					attributes = ArrayBuffer[Attribute](new Attribute(
						name = "id",
						value = (obj.id.toString))))))))
	}
}
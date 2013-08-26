package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import com.sleepyduck.pushofwar.KeyModifier
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

object Spike extends AnyRef with Cost2

class Spike(pow: PushOfWarTest, x: Float = 0, y: Float = 0, angle: Float = 0, copied: Boolean = false)
	extends BaseObjectDynamic(pow, x, y, angle, copied) with CollisionNone with Cost2 {

	val objects = ArrayBuffer[BaseObjectDynamic]()
	val joints = ArrayBuffer[RevoluteJoint]()
	var xmlElement: Option[XMLElement] = None

	override def mouseDown(p: Vec2) = {
		super.mouseDown(p)
		if (!KeyModifier.Ctrl)
			clearJoints
	}

	override def mouseUp = {
		super.mouseUp
		if (!KeyModifier.Ctrl && !KeyModifier.Shift)
			createJoints()
	}

	def clearJoints = {
		joints foreach (pow getWorld () destroyJoint _)
		joints clear ()
		objects foreach (_ removeObject this)
		objects.clear()
	}

	def createJoints(objectsArg: ArrayBuffer[BaseObjectDynamic] = getObjects) = {
		clearJoints
		objects ++= objectsArg
		for (obj <- objects) (addJoint(this, obj), obj addSpike this)
		if (objects.length > 1)
			objects reduce addJoint
	}

	override def destroy = clearJoints
	
	override def removeObject(obj:BaseObjectDynamic) = {
		super.removeObject(obj)
		joints filter (j => (j.getBodyA() == obj.body) || (j.getBodyB() == obj.body)) foreach (j => (joints -= j, pow getWorld() destroyJoint j))
		objects -= obj
	}

	def getObjects = pow findObjects (body getWorldCenter ()) filter (obj => obj != this && !(obj isSpike) && (obj hasBeenCopied))

	def addJoint(left: BaseObjectDynamic, right: BaseObjectDynamic) = {
		joints += (pow getWorld () createJoint new RevoluteJointDef() {
			initialize(left.body, right.body, body getWorldCenter ())
		}).asInstanceOf[RevoluteJoint]
		right
	}

	def copy = new Spike(pow, body.getWorldCenter().x, body.getWorldCenter().y, body.getAngle())

	def getShape = new CircleShape { this setRadius 0.3F }

	override def initialize(element: XMLElement) = {
		super.initialize(element)
		xmlElement = Option apply element
	}

	override def initializeJoints = {
		super.initializeJoints
		val objs = new ArrayBuffer[BaseObjectDynamic]()
		xmlElement map (_ getElement "Joints") filter (_ != null) map (_.children) foreach (_ foreach (child => pow getObject (child.getAttribute("id").value.toInt) foreach (objs += _)))
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

	override def isConnectedTo(obj: BaseObjectDynamic) = {
		objects find (_ == obj) isDefined
	}

	override def getConnectedObjects: ArrayBuffer[BaseObjectDynamic] = spikes ++ objects

	override def copyJoints(mappedObjs: ArrayBuffer[(BaseObjectDynamic, BaseObjectDynamic)]) = {
		def findOldObject(obj: BaseObjectDynamic) = (mappedObjs find (objPair => objPair._2 == obj) getOrElse (null, null))._1
		def findNewObject(obj: BaseObjectDynamic) = (mappedObjs find (objPair => objPair._1 == obj) getOrElse (null, null))._2
		createJoints(findOldObject(this).asInstanceOf[Spike].objects map (findNewObject _) filter (_ != null))
	}
}
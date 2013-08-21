package com.sleepyduck.pushofwar.model

import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Transform
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Filter
import org.jbox2d.dynamics.FixtureDef
import com.sleepyduck.pushofwar.PushOfWarTest
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.xml.XMLParsable
import com.sleepyduck.pushofwar.KeyModifier
import scala.collection.mutable.ArrayBuffer

abstract class BaseObjectDynamic(pow: PushOfWarTest, collisionGroup: Filter, x: Float, y: Float, angle: Float = 0, copied: Boolean = false)
	extends BaseObject(pow, collisionGroup, x, y, angle) with XMLParsable {

	val spikes = ArrayBuffer[BaseObjectDynamic]()
	val copiedObjects = ArrayBuffer[BaseObjectDynamic]()

	var hasBeenCopied = false

	if (copied) setCopied

	override def getFixture = new FixtureDef {
		friction = 2
		density = 1
		isSensor = true
		shape = getShape
	}

	override def mouseUp = {
		super.mouseUp
		copiedObjects foreach (_.body getFixtureList () setSensor false)
		copiedObjects clear
	}

	def getCopyOrThis = {
		if (!hasBeenCopied || KeyModifier.Ctrl) {
			var obj: BaseObjectDynamic = null
			if (KeyModifier.Ctrl)
				obj = clusterCopy
			else
				obj = copy
			pow addObject obj
			obj.setCopied
			obj
		} else this
	}

	def setCopied = {
		hasBeenCopied = true
		body getFixtureList () setSensor false
		body setAwake true
		body setSleepingAllowed false
	}

	def activate = {
		if (hasBeenCopied) {
			body setGravityScale 1
			body setLinearDamping 0
			body setAngularDamping 0
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

	def putAttributes(element: XMLElement) = {
		element addAttribute ("id", id toString)
		element addAttribute ("x", (body getPosition ()).x toString)
		element addAttribute ("y", (body getPosition ()).y toString)
		element addAttribute ("angle", body.getAngle().toString)
		element addAttribute ("collisionGroup", collisionGroup getClass () getSimpleName ())
	}

	def initialize(element: XMLElement) = {
		id = element.getAttribute("id").value.toInt
		setCopied
	}

	def initializeJoints = {}

	def contains(p: Vec2) = (Option apply (body getFixtureList ())) map (_ testPoint (p)) getOrElse false

	def clusterCopy = {
		val connectedObjectsProcessed = ArrayBuffer[BaseObjectDynamic](this)
		val connectedObjects = getConnectedObjects
		while (connectedObjects.length > 0) {
			val obj = connectedObjects.head
			connectedObjects -= obj
			if (connectedObjectsProcessed find (_ == obj) isEmpty) {
				connectedObjectsProcessed += obj
				connectedObjects ++= obj.getConnectedObjects
			}
		}
		val mappedObjects = connectedObjectsProcessed map (obj => (obj, obj copy))
		val thisCopy = (mappedObjects find (objPair => objPair._1 == this) head)._2
		mappedObjects foreach (p => (p._1.body getFixtureList () setSensor true,
			thisCopy.copiedObjects += p._1,
			pow addObject p._2,
			p._2 setCopied,
			p._2 copyJoints mappedObjects))
		thisCopy
	}

	def getConnectedObjects: ArrayBuffer[BaseObjectDynamic] = spikes

	def isConnectedTo(obj: BaseObjectDynamic) = false

	def copy: BaseObjectDynamic

	def isSpike = false

	def addSpike(spike: BaseObjectDynamic) = spikes += spike

	def getShape: Shape

	def copyJoints(map: ArrayBuffer[(BaseObjectDynamic, BaseObjectDynamic)]) = {}
}
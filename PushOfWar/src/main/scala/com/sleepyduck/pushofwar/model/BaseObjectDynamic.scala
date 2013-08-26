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
import com.sleepyduck.pushofwar.Player

abstract class BaseObjectDynamic(pow: PushOfWarTest, x: Float = 0, y: Float = 0, angle: Float = 0, copied: Boolean = false)
	extends BaseObject(pow, x, y, angle) with XMLParsable with CollisionNormal with Cost0 {

	var playerId = 0

	val spikes = ArrayBuffer[BaseObjectDynamic]()
	val copiedObjects = ArrayBuffer[BaseObjectDynamic]()

	var hasBeenCopied = false

	if (copied) setCopied

	override def getFixture = new FixtureDef {
		friction = 2
		density = 1
		isSensor = true
		shape = getShape
		userData = ColorChooser
	}

	override def mouseUp = {
		super.mouseUp
		copiedObjects foreach (_.body getFixtureList () setSensor false)
		copiedObjects clear
	}

	def getCopyOrThis = {
		if (!hasBeenCopied || KeyModifier.Ctrl) {
			var obj: BaseObjectDynamic = null
			if (KeyModifier.Ctrl && !(body getFixtureList () isSensor ())) {
				obj = clusterCopy
			} else {
				if (Player.Get(playerId).TryPay(Cost)) {
					obj = copy
					pow addObject obj
					obj setPlayerId playerId
					obj.setCopied
				} else {
					obj = null
				}
			}
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
		element addAttribute ("playerId", playerId toString)
	}

	def initialize(element: XMLElement) = {
		id = element.getAttribute("id").value.toInt
		setPlayerId(element.getAttribute("playerId").value.toInt)
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
		val cost = connectedObjectsProcessed map (_.Cost) reduce (_ + _)
		if (Player Get (playerId) TryPay cost) {
			val mappedObjects = connectedObjectsProcessed map (obj => (obj, obj copy)) filter (p => p._2 != null)
			val thisCopy = (mappedObjects find (objPair => objPair._1 == this) head)._2
			if (thisCopy != null) {
				mappedObjects foreach (p => p._1.body getFixtureList () setSensor true)
				mappedObjects foreach (p => thisCopy.copiedObjects += p._1)
				mappedObjects foreach (p => p._2 setPlayerId p._1.playerId)
				mappedObjects foreach (p => pow addObject p._2)
				mappedObjects foreach (p => p._2 setCopied)
				mappedObjects foreach (p => p._2 copyJoints mappedObjects)
			}
			thisCopy
		} else null
	}

	def getConnectedObjects: ArrayBuffer[BaseObjectDynamic] = spikes

	def isConnectedTo(obj: BaseObjectDynamic) = false

	def copy: BaseObjectDynamic

	def destroy = {}

	def isSpike = false
	def isPlayerBox = false

	def addSpike(spike: BaseObjectDynamic) = spikes += spike

	def removeObject(obj: BaseObjectDynamic):Unit = spikes -= obj

	def getShape: Shape

	def copyJoints(map: ArrayBuffer[(BaseObjectDynamic, BaseObjectDynamic)]) = {}

	def setPlayerId(id: Int) = {
		playerId = id
		body getFixtureList () setFilterData getCollisionGroup(playerId)
	}

	override def toString = {
		(getClass() getSimpleName ()) + "[id = \"" + id + "\", playerId = \"" + playerId + "\"]"
	}
}
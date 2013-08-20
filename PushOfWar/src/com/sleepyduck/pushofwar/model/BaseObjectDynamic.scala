package com.sleepyduck.pushofwar.model

import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Transform
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Filter
import org.jbox2d.dynamics.FixtureDef

import com.sleepyduck.pushofwar.PushOfWarTest
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.xml.XMLParsable

abstract class BaseObjectDynamic(pow: PushOfWarTest, collisionGroup: Filter, x: Float, y: Float, copied: Boolean = false)
	extends BaseObject(pow, collisionGroup, x, y) with XMLParsable {
	var hasBeenCopied = false

	if (copied) setCopied

	override def getFixture = new FixtureDef {
		friction = 2
		density = 1
		isSensor = true
		shape = getShape
	}

	override def mouseDown(p: Vec2) = {
		super.mouseDown(p)
		if (!hasBeenCopied) {
			pow addObject copy
			setCopied
		}
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
		element addAttribute ("velX", body.getLinearVelocity().x.toString)
		element addAttribute ("velY", body.getLinearVelocity().y.toString)
		element addAttribute ("velAng", body.getAngularVelocity().toString)
		element addAttribute ("collissionCategory", (body getFixtureList () getFilterData).categoryBits toString)
		element addAttribute ("collissionMask", (body getFixtureList () getFilterData).maskBits toString)
	}

	def initialize(element: XMLElement) = {
		id = element.getAttribute("id").value.toInt
		body setTransform(new Vec2(element.getAttribute("x").value.toFloat, element.getAttribute("y").value.toFloat), element.getAttribute("angle").value.toFloat)
		body setLinearVelocity(new Vec2(element.getAttribute("velX").value.toFloat, element.getAttribute("velY").value.toFloat))
		body setAngularVelocity(element.getAttribute("velAng").value.toFloat)
		(body getFixtureList() getFilterData()).categoryBits = element.getAttribute("collissionCategory").value.toInt
		(body getFixtureList() getFilterData()).maskBits = element.getAttribute("collissionMask").value.toInt
		setCopied
	}
	
	def initializeJoints = {}

	def contains(p: Vec2) = (Option apply (body getFixtureList ())) map (_ testPoint (p)) getOrElse false

	def copy: BaseObjectDynamic

	def isSpike = false

	def getShape: Shape
}
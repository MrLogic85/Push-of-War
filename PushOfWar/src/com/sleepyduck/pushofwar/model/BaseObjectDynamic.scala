package com.sleepyduck.pushofwar.model

import org.jbox2d.dynamics.joints.RevoluteJointDef
import org.jbox2d.common.Transform
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.joints.Joint
import scala.collection.mutable.ArrayBuffer
import com.sleepyduck.pushofwar.PushOfWarTest

abstract class BaseObjectDynamic(pow: PushOfWarTest) extends BaseObject(pow) {
	var hasBeenCopied = false

	override def mouseDown = {
		if (!hasBeenCopied) {
			pow addObject (this copy)
			hasBeenCopied = true
		}
		super.mouseDown
	}

	def activate = {
		if (hasBeenCopied) {
			body setGravityScale 1
			body getFixtureList () setSensor false
		}
	}

	def intersectingObjects(p: Vec2) = {
		val p2 = Transform mul (body getTransform (), p)
		pow findObjects (p2) map ((_, p2))
	}

	def stop = {
		body setLinearVelocity new Vec2(0, 0)
		body setAngularVelocity 0
	}

	def contains(p: Vec2) = (Option apply (body getFixtureList ())) map (_ getShape () testPoint (body getTransform (), p)) getOrElse false

	def copy: BaseObjectDynamic
}
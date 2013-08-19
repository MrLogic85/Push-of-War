package com.sleepyduck.pushofwar

import scala.collection.mutable.ArrayBuffer
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.joints.MouseJoint
import org.jbox2d.dynamics.joints.MouseJointDef
import org.jbox2d.testbed.framework.TestbedSettings
import org.jbox2d.testbed.framework.TestbedTest
import com.sleepyduck.pushofwar.model.Bar
import com.sleepyduck.pushofwar.model.BaseObjectDynamic
import com.sleepyduck.pushofwar.model.CollissionGroupPlayer1
import com.sleepyduck.pushofwar.model.CollissionGroupPlayer1Alt
import com.sleepyduck.pushofwar.model.CollissionGroupPlayer2
import com.sleepyduck.pushofwar.model.CollissionGroupPlayer2Alt
import com.sleepyduck.pushofwar.model.CollissionGroupStatic
import com.sleepyduck.pushofwar.model.Cone
import com.sleepyduck.pushofwar.model.RotationEnum.Clockwise
import com.sleepyduck.pushofwar.model.RotationEnum.CounterClockwise
import com.sleepyduck.pushofwar.model.Spike
import com.sleepyduck.pushofwar.model.StaticBox
import com.sleepyduck.pushofwar.model.Triangle
import com.sleepyduck.pushofwar.model.Wheel
import com.sleepyduck.pushofwar.model.BarHard

class PushOfWarTest extends TestbedTest {
	override def getTestName = "Push of War Test"

	val objects = new ArrayBuffer[Option[BaseObjectDynamic]]
	var clickObject: Option[BaseObjectDynamic] = None
	var mouseJoint: Option[MouseJoint] = None

	override def initTest(argDeserialized: Boolean) = {
		setTitle("Couple of Things Test")

		getWorld().setGravity(new Vec2(0, -45))

		objects clear ()
		clickObject = None

		val width = 70
		// Stage
		new StaticBox(
			pow = this,
			collisionGroup = CollissionGroupStatic(),
			x = 0,
			y = -80,
			w = width,
			h = 80)

		val collisionGroups = List(List(CollissionGroupPlayer1(), CollissionGroupPlayer1Alt()), List(CollissionGroupPlayer2(), CollissionGroupPlayer2Alt()))
		for (i <- 0 to 1) {
			val sign = -1 + 2*i
			// Wheels
			objects += Option apply (new Wheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * 75, y = 20, rotation = Clockwise, torque = 800))
			objects += Option apply (new Wheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * 75, y = 15, rotation = CounterClockwise, torque = 800))

			// Objects
			objects += Option apply (new BarHard(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * 75, y = 12, w = 10, h = 1))
			objects += Option apply (new Bar(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * 75, y = 10, w = 10, h = 1))
			objects += Option apply (new Triangle(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * 72, y = 5, base = 3))
			objects += Option apply (new Cone(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * 78, y = 5, base = 6))

			// Spikes
			objects += Option apply (new Spike(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * 75, y = 0))
		}
	}

	override def step(settings: TestbedSettings) = {
		super.step(settings)
		objects foreach (o => o map (_ step settings))
	}

	override def mouseDown(p: Vec2) = {
		//super.mouseDown(p)
		clickObject = takeOne(findObjects(p))
		clickObject foreach (_ mouseDown p)
		objects -= clickObject
		objects prepend clickObject

		if (clickObject isDefined) {
			val body = clickObject.get body
			val mouseDef = new MouseJointDef() {
				bodyA = groundBody
				bodyB = body
				target set p
				maxForce = 1000f * (body getMass)
			}
			mouseJoint = Option apply (getWorld() createJoint (mouseDef)).asInstanceOf[MouseJoint]
			body setAwake true
		}

	}

	override def mouseUp(p: Vec2) = {
		super.mouseUp(p)
		clickObject foreach (_ mouseUp)
		clickObject foreach (_ click)
		clickObject = None
		objects foreach (o => o map (_ stop))

		mouseJoint foreach (getWorld() destroyJoint _)
		mouseJoint = None
	}

	override def mouseMove(p: Vec2) = {
		super.mouseMove(p)
		mouseJoint foreach (_ setTarget p)
	}

	override def keyPressed(keyChar: Char, keyCode: Int) = keyChar toUpper match {
		case 'S' => start
		case _ =>
	}

	def takeOne(objs: ArrayBuffer[Option[BaseObjectDynamic]]): Option[BaseObjectDynamic] = {
		if (objs isEmpty) None
		else {
			val newObjs = objs filter (_ map (_ isSpike) getOrElse false)
			if (newObjs isEmpty) (objs headOption) getOrElse None
			else (newObjs headOption) getOrElse None
		}
	}

	def findObjects(p: Vec2) = objects filter (_ map (_ contains p) getOrElse false)

	def addObject(obj: BaseObjectDynamic) = objects += Option apply obj

	def start = objects filter (_ map (_ hasBeenCopied) getOrElse false) foreach (o => o foreach (_ activate))
}
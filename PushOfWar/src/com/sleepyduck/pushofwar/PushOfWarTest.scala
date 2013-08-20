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
import com.sleepyduck.pushofwar.model.RotationEnum.NoEngine
import com.sleepyduck.pushofwar.model.Spike
import com.sleepyduck.pushofwar.model.StaticBox
import com.sleepyduck.pushofwar.model.Triangle
import com.sleepyduck.pushofwar.model.Wheel
import com.sleepyduck.pushofwar.model.SteamWheel
import com.sleepyduck.pushofwar.model.BarHard
import com.sleepyduck.pushofwar.model.CollissionGroupNone

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
		val width = 150

		// Stage
		new StaticBox(pow = this, collisionGroup = CollissionGroupStatic(), w = width * 2)

		val collisionGroups = List(List(CollissionGroupPlayer1(), CollissionGroupPlayer1Alt()), List(CollissionGroupPlayer2(), CollissionGroupPlayer2Alt()))
		for (i <- 0 to 1) {
			val sign = -1 + 2 * i
			// Wheels
			this addObject new Wheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 7), y = 15, rotation = Clockwise)
			this addObject new Wheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 2), y = 15, rotation = CounterClockwise)
			this addObject new Wheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 12), y = 15, rotation = NoEngine, torque = 0)
			this addObject new SteamWheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 9), y = 22, rotation = Clockwise)
			this addObject new SteamWheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 1), y = 22, rotation = CounterClockwise)
			this addObject new SteamWheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 17), y = 22, rotation = NoEngine, torque = 0)

			// Objects
			this addObject new BarHard(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 5), y = 12)
			this addObject new Bar(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * (width + 5), y = 10)
			this addObject new Triangle(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * (width + 2), y = 5, base = 3)
			this addObject new Cone(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * (width + 8), y = 5, base = 6)

			// Spikes
			this addObject new Spike(pow = this, collisionGroup = CollissionGroupNone(), x = sign * (width + 5), y = 0)

			// Give base vehicle
			def body = new Bar(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * (width - 5), y = -6, copied = true)
			val wheel1 = new Wheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width - 0.5F), y = -6, rotation = sign match { case -1 => CounterClockwise case _ => Clockwise }, copied = true)
			val wheel2 = new Wheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width - 9.5F), y = -6, rotation = sign match { case -1 => CounterClockwise case _ => Clockwise }, copied = true)

			this addObject body
			this addObject wheel1
			this addObject wheel2
			wheel1 createJoints;
			wheel2 createJoints;
		}
	}

	override def step(settings: TestbedSettings) = {
		super.step(settings)
		objects foreach (o => o map (_ step settings))
	}

	override def mouseDown(p: Vec2) = {
		//super.mouseDown(p) Do not call super!
		System.out.println("Mouse down at: (" + p.x.toInt + ", " + p.y.toInt + ")")
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

	override def keyPressed(keyChar: Char, keyCode: Int) = {
		System.out.println("Key pressed: " + keyChar + " (" + keyCode + ")")
		keyCode match {
			case 83 => spawnSpike(this getWorldMouse)
			case 10 => start
			case 116 => save
			case 117 => load
			case _ =>
		}
	}

	override def _save = {
		System.out.println("Save")
	}

	override def _load = {
		System.out.println("Load")
	}

	def spawnSpike(p: Vec2) = {
		val spike = new Spike(pow = this, collisionGroup = CollissionGroupNone(), x = getWorldMouse().x, y = getWorldMouse().y, copied = true)
		spike createJoints;
		this addObject spike
	}

	def takeOne(objs: ArrayBuffer[Option[BaseObjectDynamic]]): Option[BaseObjectDynamic] = {
		if (objs isEmpty) None
		else {
			val newObjs = objs filter (_ map (_ isSpike) getOrElse false)
			if (!(newObjs isEmpty)) (newObjs headOption) getOrElse None
			else (objs headOption) getOrElse None
		}
	}

	def findObjects(p: Vec2) = objects filter (_ map (_ contains p) getOrElse false)

	def addObject(obj: BaseObjectDynamic) = objects += Option apply obj

	def start = objects filter (_ map (_ hasBeenCopied) getOrElse false) foreach (o => o foreach (_ activate))
}
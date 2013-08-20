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
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D
import java.awt.event.MouseWheelEvent
import java.util.Calendar
import org.jbox2d.testbed.framework.TestbedModel
import com.sleepyduck.xml.XMLElement

object PushOfWarTest {
	val quicksave = new XMLElement("PushOfWar")
}

class PushOfWarTest extends WrappedTestbedTest {
	override def getTestName = "Push of War Test"

	val objects = new ArrayBuffer[BaseObjectDynamic]
	var clickObject: Option[BaseObjectDynamic] = None
	var mouseJoint: Option[MouseJoint] = None

	override def initTest(argDeserialized: Boolean) = {
		setTitle("Couple of Things Test")

		getWorld().setGravity(new Vec2(0, -45))

		objects clear ()
		clickObject = None
		val width = 150

		// Stage
		new StaticBox(pow = this, collisionGroup = CollissionGroupStatic, w = width * 2)

		val collisionGroups = List(List(CollissionGroupPlayer1, CollissionGroupPlayer1Alt), List(CollissionGroupPlayer2, CollissionGroupPlayer2Alt))
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
			this addObject new Triangle(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * (width + 2), y = 5)
			this addObject new Cone(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * (width + 8), y = 5)

			// Spikes
			this addObject new Spike(pow = this, x = sign * (width + 5), y = 0)
		}
		
		load
	}

	override def step(settings: TestbedSettings) = {
		super.step(settings)
		objects foreach (_ step settings)
	}

	override def mouseDown(p: Vec2) = {
		//super.mouseDown(p) Do not call super!
		System.out.println("Mouse down at: (" + p.x.toInt + ", " + p.y.toInt + ")")
		clickObject = takeOne(findObjects(p))
		clickObject foreach (_ mouseDown p)
		clickObject foreach (objects -= _)
		clickObject foreach (objects prepend _)

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
		objects foreach (_ stop)

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
			case 117 => reset
			case _ =>
		}
	}

	override def _save = {
		PushOfWarTest.quicksave.children.clear
		objects filter (_.hasBeenCopied) foreach (obj => PushOfWarTest.quicksave addChild (obj toXMLElement))
		System.out.println("Save")
		System.out.println(PushOfWarTest.quicksave.toString)
	}

	override def _load = {
		PushOfWarTest.quicksave.children foreach createObject
		objects filter (_ hasBeenCopied) map (_ initializeJoints)
		System.out.println("Load")
	}
	
	def createObject(xmlElement:XMLElement) = {
		val obj = xmlElement.name match {
			case "Bar" => Option apply (new Bar(this))
			case "BarHard" => Option apply (new BarHard(this))
			case "Cone" => Option apply (new Cone(this))
			case "Spike" => Option apply (new Spike(this))
			case "SteamWheel" => Option apply (new SteamWheel(this))
			case "Triangle" => Option apply (new Triangle(this))
			case "Wheel" => Option apply (new Wheel(this))
			case _ => 
				System.out.println("Failed to load " + xmlElement.name)
				None
		}
		obj foreach (_ initialize xmlElement)
		obj foreach (this addObject _)
	}

	def spawnSpike(p: Vec2) = {
		val spike = new Spike(pow = this, x = getWorldMouse().x, y = getWorldMouse().y, copied = true)
		spike createJoints()
		this addObject spike
	}

	def takeOne(objs: ArrayBuffer[BaseObjectDynamic]): Option[BaseObjectDynamic] = {
		if (objs isEmpty) None
		else {
			val newObjs = objs filter (_ isSpike)
			if (!(newObjs isEmpty)) newObjs headOption
			else objs headOption
		}
	}

	def findObjects(p: Vec2) = objects filter (_ contains p)

	def addObject(obj: BaseObjectDynamic) = objects += obj

	def start = objects filter (_ hasBeenCopied) foreach (_ activate)
	
	def getObject(id:Int) = objects filter (_.id == id) headOption
}
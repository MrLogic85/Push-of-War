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
import java.io.PrintWriter
import java.io.File
import scala.io.Source
import com.sleepyduck.xml.XMLElementFactory
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.pushofwar.model.CollisionGroup
import com.sleepyduck.pushofwar.model.BaseObjectDynamic
import com.sleepyduck.pushofwar.model.BaseObjectDynamic
import java.io.FileNotFoundException
import com.sleepyduck.pushofwar.model.CollissionGroupPlayer1None
import com.sleepyduck.pushofwar.model.CollissionGroupPlayer2None

object PushOfWarTest {
	val QuickSave = new XMLElement("PushOfWar")
	var doLoadFromFile = false

	def saveToFile = {
		val writer = new PrintWriter(new File("SaveFile.txt"))
		writer.write(QuickSave toString)
		writer.close()
		println("Save to file")
	}

	def loadFromFile = {
		try {
			def saveText = Source.fromFile("SaveFile.txt") getLines () reduce (_ + _)
			println(saveText)
			def headElement = (XMLElementFactory BuildFromXMLString saveText).headOption getOrElse (new XMLElement)
			QuickSave.children clear ()
			QuickSave.children ++= headElement.children
			doLoadFromFile = false
		} catch {
			case e: FileNotFoundException =>
		}
		println("Load from file")
	}
}

object KeyModifier {
	var Ctrl = false
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

		val collisionGroups = List(List(CollissionGroupPlayer1, CollissionGroupPlayer1Alt, CollissionGroupPlayer1None), List(CollissionGroupPlayer2, CollissionGroupPlayer2Alt, CollissionGroupPlayer2None))
		for (i <- 0 to 1) {
			val sign = -1 + 2 * i
			// Wheels
			this addObject new Wheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 7), y = 15, rotation = Clockwise)
			this addObject new Wheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 2), y = 15, rotation = CounterClockwise)
			this addObject new Wheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 12), y = 15, rotation = NoEngine)
			this addObject new SteamWheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 9), y = 22, rotationSW = Clockwise)
			this addObject new SteamWheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 1), y = 22, rotationSW = CounterClockwise)
			this addObject new SteamWheel(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 17), y = 22, rotationSW = NoEngine)

			// Objects
			this addObject new BarHard(pow = this, collisionGroup = collisionGroups(i)(1), x = sign * (width + 5), y = 12)
			this addObject new Bar(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * (width + 5), y = 10)
			this addObject new Triangle(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * (width + 2), y = 5)
			this addObject new Cone(pow = this, collisionGroup = collisionGroups(i)(0), x = sign * (width + 8), y = 5)

			// Spikes
			this addObject new Spike(pow = this,  collisionGroup = collisionGroups(i)(2), x = sign * (width + 5), y = 0)
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
		var wasCopied = false
		clickObject = takeOne(findObjects(p))
		clickObject = clickObject map (_ getCopyOrThis)
		clickObject foreach (o => wasCopied = o.hasBeenCopied)
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
			case 83 => // s
				spawnSpike(this getWorldMouse)
			case 10 => // enter
				quicksave
				start
			case 116 => // F5
				saveToFile
			case 117 => // F6
				PushOfWarTest.doLoadFromFile = true
				reset
			case 67 => // c
				PushOfWarTest.QuickSave.children.clear
				reset
			case 17 => // Ctrl
				KeyModifier.Ctrl = true
			case _ =>
		}
	}

	override def keyReleased(keyChar: Char, keyCode: Int) = keyCode match {
		case 17 => // Ctrl
			KeyModifier.Ctrl = false
		case _ =>
	}

	def quicksave = {
		PushOfWarTest.QuickSave.children.clear
		objects filter (_.hasBeenCopied) foreach (obj => PushOfWarTest.QuickSave addChild (obj toXMLElement))
		println("Save")
		println(PushOfWarTest.QuickSave.toString)
	}

	def saveToFile = {
		quicksave
		PushOfWarTest saveToFile
	}

	override def _load = {
		if (PushOfWarTest.doLoadFromFile == true)
			PushOfWarTest loadFromFile

		PushOfWarTest.QuickSave.children foreach createObject
		objects filter (_ hasBeenCopied) map (_ initializeJoints)
		println("Load")
	}

	def createObject(xmlElement: XMLElement) = {
		def collisionGroup = CollisionGroup.GetByName(xmlElement.getAttribute("collisionGroup").value)
		def x = xmlElement.getAttribute("x").value.toFloat
		def y = xmlElement.getAttribute("y").value.toFloat
		def angle = xmlElement.getAttribute("angle").value.toFloat
		val obj = xmlElement.name match {
			case "Bar" => Option apply (new Bar(this, collisionGroup, x, y, angle))
			case "BarHard" => Option apply (new BarHard(this, collisionGroup, x, y, angle))
			case "Cone" => Option apply (new Cone(this, collisionGroup, x, y, angle))
			case "Spike" => Option apply (new Spike(this, collisionGroup, x, y, angle))
			case "SteamWheel" => Option apply (new SteamWheel(this, collisionGroup, x, y, angle))
			case "Triangle" => Option apply (new Triangle(this, collisionGroup, x, y, angle))
			case "Wheel" => Option apply (new Wheel(this, collisionGroup, x, y, angle))
			case _ =>
				System.out.println("Failed to load " + xmlElement.name)
				None
		}
		obj foreach (_ initialize xmlElement)
		obj foreach (this addObject _)
	}

	def spawnSpike(p: Vec2) = {
		val spike = new Spike(pow = this, x = getWorldMouse().x, y = getWorldMouse().y, copied = true)
		spike createJoints ()
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

	def getObject(id: Int) = objects filter (_.id == id) headOption

	def getUniqueId:Int = {
		val id = (Math.random * Int.MaxValue).toInt
		if (getObject(id) isEmpty)
			id
		else
			getUniqueId
	}
}
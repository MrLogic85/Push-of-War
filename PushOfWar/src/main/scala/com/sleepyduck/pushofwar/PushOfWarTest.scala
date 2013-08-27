package com.sleepyduck.pushofwar

import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import org.jbox2d.common.Color3f
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.joints.MouseJoint
import org.jbox2d.dynamics.joints.MouseJointDef
import org.jbox2d.testbed.framework.TestbedSettings
import com.sleepyduck.pushofwar.model.Bar
import com.sleepyduck.pushofwar.model.BarHard
import com.sleepyduck.pushofwar.model.BaseObjectDynamic
import com.sleepyduck.pushofwar.model.BaseObjectDynamic
import com.sleepyduck.pushofwar.model.BaseObjectDynamic
import com.sleepyduck.pushofwar.model.BaseObjectDynamic
import com.sleepyduck.pushofwar.model.BaseObjectPlayer
import com.sleepyduck.pushofwar.model.BaseObjectPlayer
import com.sleepyduck.pushofwar.model.BaseObjectPlayer
import com.sleepyduck.pushofwar.model.Cone
import com.sleepyduck.pushofwar.model.RotationEnum.Clockwise
import com.sleepyduck.pushofwar.model.RotationEnum.CounterClockwise
import com.sleepyduck.pushofwar.model.RotationEnum.NoEngine
import com.sleepyduck.pushofwar.model.Spike
import com.sleepyduck.pushofwar.model.StaticBox
import com.sleepyduck.pushofwar.model.SteamWheel
import com.sleepyduck.pushofwar.model.Triangle
import com.sleepyduck.pushofwar.model.Wheel
import com.sleepyduck.xml.Attribute
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.xml.XMLElementFactory
import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Identify
import akka.actor.ActorIdentity
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import akka.actor.ReceiveTimeout
import com.sleepyduck.xml.XMLElement

object PushOfWarTest {
	val QuickSave = new XMLElement("PushOfWar")

	Reset

	def Reset = {
		PushOfWarTest.QuickSave.children.clear
		PushOfWarTest.QuickSave.addAllChildren(List(new XMLElement("Objects"), new XMLElement("Player1"), new XMLElement("Player2")))
		PushOfWarTest.QuickSave.getElement("Player1").addAttribute("points", "100")
		PushOfWarTest.QuickSave.getElement("Player2").addAttribute("points", "100")
	}

	var doLoadFromFile = false
	var running = false

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

	def loadPlayer1ToPlayer2From(element: XMLElement) = {
		PushOfWarTest.QuickSave.getElement("Player2").getAttribute("points").value = element.getElement("Player1").getAttribute("points").value
		val player2Objs = element.getElement("Objects").children filter (_.getAttribute("playerId").value.toInt == 1)
		player2Objs foreach (_.getAttribute("playerId").value = 2.toString)
		player2Objs foreach (el => el.getAttribute("x").value = (-1.0F * el.getAttribute("x").value.toFloat).toString)
		
		val player1Objs = PushOfWarTest.QuickSave.getElement("Objects").children filter (_.getAttribute("playerId").value.toInt == 1)
		
		PushOfWarTest.QuickSave.getElement("Objects").children.clear
		PushOfWarTest.QuickSave.getElement("Objects").children ++= player1Objs
		PushOfWarTest.QuickSave.getElement("Objects").children ++= player2Objs 
	}
}

object KeyModifier {
	var Shift = false
	var Ctrl = false
	var Alt = false
}

class PushOfWarTest extends WrappedTestbedTest {
	override def getTestName = "Push of War Test"

	val objects = new ArrayBuffer[BaseObjectDynamic]
	var clickObject: Option[BaseObjectDynamic] = None
	var mouseJoint: Option[MouseJoint] = None
	var frame: WrappedTestbedFrame = null
	var timer = 0L
	val oneSecond = 1000L
	val previousDistance = Array(0, 0)
	var remoteAddress = "127.0.0.1"

	override def initTest(argDeserialized: Boolean) = {
		setTitle("Push of War")

		textList.clear()
		addTextLine("Click and drag the left mouse button to move objects.")
		addTextLine("Click and drag the right mouse button to move the screen.")
		addTextLine("Zoom in and out using the scroll wheel.")
		addTextLine("Wheels and spikes can be conected to other objects by releasing")
		addTextLine("them when the center of the wheel is intersecting with another item.")
		addTextLine("Ctrl-click to copy connected objects.")
		addTextLine("Shift click to remove a object.")
		addTextLine("Press 'S' to quickly spawn a spike where the mouse is.")
		addTextLine("Press Enter to start the fight.")
		addTextLine("Press 'R' to reset the building process to the same state as before the fight started.")
		addTextLine("Press 'C' to clear the game.")
		addTextLine("Press F5 save the game to disk.")
		addTextLine("Press F6 load the game from disk.")
		addTextLine("Press 'H' to toggle the help.")
		addTextLine("")

		addTextLine("Player 1 is on the left.")
		addTextLine("Player 2 is on the right.")
		addTextLine("Large wheels cost " + SteamWheel.Cost + " points.")
		addTextLine("Wheels cost " + Wheel.Cost + " points.")
		addTextLine("Bars and triangles cost " + Bar.Cost + " points.")
		addTextLine("Spikes cost " + Spike.Cost + " points.")

		getWorld().setGravity(new Vec2(0, -45))

		objects clear ()
		clickObject = None
		val width = 150

		if (PushOfWarTest.running) {
			PushOfWarTest.QuickSave.getElement("Player1").getAttribute("points").value = Player1.Points.toString
			PushOfWarTest.QuickSave.getElement("Player2").getAttribute("points").value = Player2.Points.toString
		}
		PushOfWarTest.running = false

		// Stage
		new StaticBox(pow = this, w = width * 2)

		_load
		val playerObjCount = objects filter (_.isPlayerBox) length

		for (i <- 0 to 1) {
			val sign = -1 + 2 * i
			val start = objects.length

			//Body
			if (playerObjCount == 0) {
				val playerObj = new BaseObjectPlayer(pow = this, x = sign * (width - 10), y = 2)
				this addObject playerObj
			}

			// Wheels
			this addObject new Wheel(pow = this, x = sign * (width + 8), y = 15, rotation = Clockwise)
			this addObject new Wheel(pow = this, x = sign * (width + 2), y = 15, rotation = CounterClockwise)
			this addObject new SteamWheel(pow = this, x = sign * (width + 9), y = 22, rotationSW = Clockwise)
			this addObject new SteamWheel(pow = this, x = sign * (width + 1), y = 22, rotationSW = CounterClockwise)

			// Objects
			this addObject new BarHard(pow = this, x = sign * (width + 5), y = 12)
			this addObject new Bar(pow = this, x = sign * (width + 5), y = 10)
			this addObject new Triangle(pow = this, x = sign * (width + 2), y = 5)
			this addObject new Cone(pow = this, x = sign * (width + 7), y = 5)

			// Spikes
			this addObject new Spike(pow = this, x = sign * (width + 5), y = 0)

			for (j <- start until objects.length) objects(j) setPlayerId (i + 1)
		}
	}

	override def step(settings: TestbedSettings) = {
		super.step(settings)
		objects foreach (_ step settings)

		// Draw Game Info

		var textLine = 40
		getModel() getDebugDraw () drawString (frame.getBounds().getWidth().toFloat / 2.0F - 40, textLine, "Points", new Color3f(.6f, .61f, 1))
		textLine += 15
		getModel() getDebugDraw () drawString (frame.getBounds().getWidth().toFloat / 2.0F - 200, textLine, ("Player 1 has " + Player1.Points + " points"), Color3f.WHITE)
		getModel() getDebugDraw () drawString (frame.getBounds().getWidth().toFloat / 2.0F + 20, textLine, ("Player 2 has " + Player2.Points + " points"), Color3f.WHITE)

		// Check to give point
		if (PushOfWarTest.running && timer < System.currentTimeMillis() && timer != 0) {
			timer = System.currentTimeMillis() + oneSecond
			givePoints
		}
	}

	override def mouseDown(p: Vec2) = {
		//super.mouseDown(p) Do not call super!
		//System.out.println("Mouse down at: (" + p.x.toInt + ", " + p.y.toInt + ")")
		if (KeyModifier.Shift) mouseDownShift(p)
		else {
			var wasCopied = false
			clickObject = takeOne(findObjects(p))
			clickObject = clickObject map (_ getCopyOrThis) filter (_ != null)
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
	}

	def mouseDownShift(p: Vec2) = {
		val obj = takeOne(findObjects(p))
		obj foreach (o => Player Get (o.playerId) GiverPoints o.Cost)
		obj foreach (obj => if (obj.hasBeenCopied && !obj.isPlayerBox) this removeObject obj)
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
			case 67 => // c
				PushOfWarTest.running = false
				PushOfWarTest.Reset
				reset
			case 68 => // d
				if (!PushOfWarTest.running) {
					quicksave
					senderActor ! PushOfWarTest.QuickSave.toString
				}
			case 72 => // h
				setShowInfo(!getShowInfo())
			case 83 => // s
				spawnSpike(this getWorldMouse)
			case 10 => // enter
				if (!PushOfWarTest.running) {
					quicksave
					start
				}
				PushOfWarTest.running = true
			case 116 => // F5
				saveToFile
			case 117 => // F6
				PushOfWarTest.running = false
				PushOfWarTest.doLoadFromFile = true
				reset
			case 16 => // Shift
				KeyModifier.Shift = true
			case 17 => // Ctrl
				KeyModifier.Ctrl = true
			case 18 => // Alt
				KeyModifier.Alt = true
			case _ =>
		}
	}

	override def keyReleased(keyChar: Char, keyCode: Int) = keyCode match {
		case 16 => // Shift
			KeyModifier.Shift = false
		case 17 => // Ctrl
			KeyModifier.Ctrl = false
		case 18 => // Alt
			KeyModifier.Alt = false
		case _ =>
	}

	def quicksave = {
		PushOfWarTest.QuickSave.children.clear
		PushOfWarTest.QuickSave.addAllChildren(List(new XMLElement("Objects"), new XMLElement("Player1"), new XMLElement("Player2")))
		objects filter (_.hasBeenCopied) foreach (obj => PushOfWarTest.QuickSave.getElement("Objects") addChild (obj toXMLElement))

		PushOfWarTest.QuickSave.getElement("Player1").addAttribute("points", Player1.Points.toString)
		PushOfWarTest.QuickSave.getElement("Player2").addAttribute("points", Player2.Points.toString)

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

		PushOfWarTest.QuickSave.getElement("Objects").children foreach createObject
		objects filter (_ hasBeenCopied) map (_ initializeJoints)

		Player1.SetPoints(PushOfWarTest.QuickSave.getElement("Player1").getAttribute("points").value.toInt)
		Player2.SetPoints(PushOfWarTest.QuickSave.getElement("Player2").getAttribute("points").value.toInt)
		println("Load")
	}

	def createObject(xmlElement: XMLElement) = {
		def x = xmlElement.getAttribute("x").value.toFloat
		def y = xmlElement.getAttribute("y").value.toFloat
		def angle = xmlElement.getAttribute("angle").value.toFloat
		val obj = xmlElement.name match {
			case "Bar" => Option apply (new Bar(this, x, y, angle))
			case "BarHard" => Option apply (new BarHard(this, x, y, angle))
			case "BaseObjectPlayer" => Option apply (new BaseObjectPlayer(this, x, y, angle))
			case "Cone" => Option apply (new Cone(this, x, y, angle))
			case "Spike" => Option apply (new Spike(this, x, y, angle))
			case "SteamWheel" => Option apply (new SteamWheel(this, x, y, angle))
			case "Triangle" => Option apply (new Triangle(this, x, y, angle))
			case "Wheel" => Option apply (new Wheel(this, x, y, angle))
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
		val connectedId = ((spike getConnectedObjects) map (_.playerId)).headOption
		connectedId foreach (spike setPlayerId _)
		this addObject spike
		if (!(connectedId map (Player.Get(_).TryPay(Spike.Cost)) getOrElse false))
			this removeObject spike

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

	def addObject(obj: BaseObjectDynamic) = {
		objects += obj
		println("Created " + obj.toString() + " at (" + obj.body.getPosition().x.toInt + "," + obj.body.getPosition().y.toInt + ")")
	}

	def getObject(id: Int) = objects filter (_.id == id) headOption

	def removeObject(obj: BaseObjectDynamic) = {
		objects -= obj
		obj.getConnectedObjects foreach (_ removeObject obj)
		obj.destroy
		getWorld() destroyBody obj.body
		println("Destroyed " + obj.toString())
	}

	def start = {
		objects filter (_ hasBeenCopied) foreach (_ activate)
		timer = System.currentTimeMillis() + oneSecond
		previousDistance(0) = 0
		previousDistance(1) = 0
	}

	def getUniqueId: Int = {
		val id = (Math.random * Int.MaxValue).toInt
		if (getObject(id) isEmpty)
			id
		else
			getUniqueId
	}

	override def spawnBomb(p: Vec2) = {}

	def setFrame(frame: WrappedTestbedFrame) = this.frame = frame

	def givePoints = {
		val playerBoxes = objects filter (_.isPlayerBox)
		for (playerBox <- playerBoxes) {
			val sign = ((playerBox.playerId - 1) * 2 - 1)
			val startPoint = sign * 140
			val distance = -sign * (playerBox.body.getPosition().x - startPoint)
			if (distance > 280) {
				Player Get (playerBox.playerId) GiverPoints (10)
				timer = 0
			}
			while (distance - previousDistance(playerBox.playerId - 1) > 28 && previousDistance(playerBox.playerId - 1) < 280) {
				Player Get (playerBox.playerId) GiverPoints (1)
				previousDistance(playerBox.playerId - 1) += 56
			}
			println("Distance Player " + playerBox.playerId + " " + distance)
		}
	}

	def recieverCallback(msg: String) = {}

	//#setup
	val systemRecieve = ActorSystem("PushOfWar", ConfigFactory.load.getConfig("reciever"))
	val recieveActor = systemRecieve.actorOf(Props(classOf[Reciever], this), "reciever")

	val systemRemote = ActorSystem("PushOfWar", ConfigFactory.load.getConfig("remotelookup"))
	val remotePath = s"akka.tcp://PushOfWar@$remoteAddress:2552/user/reciever"
	val senderActor = systemRemote.actorOf(Props(classOf[Sender], remotePath), "sender")

}

class Reciever(callback: PushOfWarTest) extends Actor {
	def receive = {
		case msg: String =>
			callback.quicksave
			PushOfWarTest loadPlayer1ToPlayer2From XMLElementFactory.BuildFromXMLString(msg).head
			callback.reset
	}
}

class Sender(path: String) extends Actor {

	context.setReceiveTimeout(3.seconds)
	sendIdentifyRequest()

	def sendIdentifyRequest(): Unit =
		context.actorSelection(path) ! Identify(path)

	def receive = {
		case ActorIdentity(`path`, Some(actor)) =>
			context.setReceiveTimeout(Duration.Undefined)
			context.become(active(actor))
		case ActorIdentity(`path`, None) => println(s"Remote actor not availible: $path")
		case ReceiveTimeout => sendIdentifyRequest()
		case _ => println("Not ready yet")
	}

	def active(actor: ActorRef): Actor.Receive = {
		case str: String => actor ! str
	}
}
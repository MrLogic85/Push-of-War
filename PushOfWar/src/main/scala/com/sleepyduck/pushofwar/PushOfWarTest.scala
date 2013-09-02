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
import com.sleepyduck.pushofwar.communication.Connected
import com.sleepyduck.pushofwar.communication.Done
import com.sleepyduck.pushofwar.communication.Mailbox
import com.sleepyduck.pushofwar.communication.Message
import com.sleepyduck.pushofwar.communication.RecieveActor
import com.sleepyduck.pushofwar.communication.Reset
import com.sleepyduck.pushofwar.communication.Run
import com.sleepyduck.pushofwar.communication.Synch
import com.sleepyduck.pushofwar.model.Bar
import com.sleepyduck.pushofwar.model.BarHard
import com.sleepyduck.pushofwar.model.BaseObjectDynamic
import com.sleepyduck.pushofwar.model.BaseObjectPlayer
import com.sleepyduck.pushofwar.model.Cone
import com.sleepyduck.pushofwar.model.RotationEnum
import com.sleepyduck.pushofwar.model.RotationEnum.Clockwise
import com.sleepyduck.pushofwar.model.RotationEnum.CounterClockwise
import com.sleepyduck.pushofwar.model.Spike
import com.sleepyduck.pushofwar.model.SpikeHard
import com.sleepyduck.pushofwar.model.StaticBox
import com.sleepyduck.pushofwar.model.SteamWheel
import com.sleepyduck.pushofwar.model.Triangle
import com.sleepyduck.pushofwar.model.Wheel
import com.sleepyduck.pushofwar.util.HelpText
import com.sleepyduck.pushofwar.util.IStarted
import com.sleepyduck.pushofwar.util.KeyModifier
import com.sleepyduck.pushofwar.util.NooneStarted
import com.sleepyduck.pushofwar.util.OtherStarted
import com.sleepyduck.pushofwar.util.Player
import com.sleepyduck.pushofwar.util.Player1
import com.sleepyduck.pushofwar.util.Player2
import com.sleepyduck.pushofwar.util.WhoStarted
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.xml.XMLElementFactory
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSelection.toScala
import akka.actor.ActorSystem
import akka.actor.Props
import com.sleepyduck.pushofwar.communication.KeyPressed
import com.sleepyduck.pushofwar.communication.KeyPressed
import com.sleepyduck.pushofwar.communication.MouseUp
import com.sleepyduck.pushofwar.communication.MouseDown
import com.sleepyduck.pushofwar.communication.Reset
import com.sleepyduck.pushofwar.model.BaseObjectDynamic

object PushOfWarTest {
	val QuickSave = new XMLElement("PushOfWar")

	Reset

	def Reset = {
		PushOfWarTest.QuickSave.children.clear
		PushOfWarTest.QuickSave.addAllChildren(List(new XMLElement("Objects"), new XMLElement("Player1"), new XMLElement("Player2")))
		PushOfWarTest.QuickSave.getElement("Player1").addAttribute("points", "100")
		PushOfWarTest.QuickSave.getElement("Player2").addAttribute("points", "100")
	}

	var doLoadFromFile = true
	var IsRunning = false

	def saveToFile(name: String) = {
		val writer = new PrintWriter(new File(s"$name.xml"))
		writer.write(QuickSave toString)
		writer.close()
		println("Save to file")
	}

	def loadFromFile(name: String) = {
		try {
			val saveText = Source.fromFile(s"$name.xml") getLines () reduce (_ + _)
			val headElement = (XMLElementFactory BuildFromXMLString saveText).headOption getOrElse (new XMLElement)
			QuickSave.children clear ()
			QuickSave.children ++= headElement.children
			doLoadFromFile = false
		} catch {
			case e: FileNotFoundException =>
		}
		println("Load from file")
	}

	def loadPlayer1ToPlayer2From(element: XMLElement) = {
		// Set the points
		Player2.Points = element.getElement("Player1").getAttribute("points").value.toInt
		PushOfWarTest.QuickSave.getElement("Player2").getAttribute("points").value = Player2.Points.toString

		val player2Objs = element.getElement("Objects").children
		player2Objs foreach (_.getAttribute("playerId").value = 2.toString)
		player2Objs foreach (el => el.getAttribute("x").value = (-1.0F * el.getAttribute("x").value.toFloat).toString)
		player2Objs foreach (el => el.getAttribute("angle").value = (Math.PI.toFloat - el.getAttribute("angle").value.toFloat).toString)
		val wheels = player2Objs filter (el => el.getAttribute("rotation") != null)
		wheels foreach (el => el.getAttribute("rotation").value = RotationEnum.turnAround(RotationEnum.fromString(el.getAttribute("rotation").value)).toString())

		val player1Objs = PushOfWarTest.QuickSave.getElement("Objects").children filter (_.getAttribute("playerId").value.toInt == 1)

		PushOfWarTest.QuickSave.getElement("Objects").children.clear
		PushOfWarTest.QuickSave.getElement("Objects").children ++= player1Objs
		PushOfWarTest.QuickSave.getElement("Objects").children ++= player2Objs
	}
}

class PushOfWarTest(remoteAddress: String = "127.0.0.1", localAddress: String = "127.0.0.1", remotePort: String = "2552", localPort: String = "2552", saveFile: String = "SaveFile")
	extends WrappedTestbedTest {
	override def getTestName = "Push of War Test"

	val systemServer = ActorSystem("PushOfWar", ConfigFactory.parseString(
		s"""
			akka {
				actor {
					provider = "akka.remote.RemoteActorRefProvider"
				}
				remote {
					enabled-transports = ["akka.remote.netty.tcp"]
					netty.tcp {
						hostname = "$localAddress"
						port = $localPort
					}
				}
			}
		"""))
	val recieveActor = systemServer.actorOf(Props(new RecieveActor(this)), name = "server")
	val path = s"akka.tcp://PushOfWar@$remoteAddress:$remotePort/user/server"
	val sendActor = systemServer.actorSelection(path)

	val objects = new ArrayBuffer[BaseObjectDynamic]
	var clickObject: Option[BaseObjectDynamic] = None
	var mouseJoint: Option[MouseJoint] = None
	var frame: WrappedTestbedFrame = null
	var checkForPoints = false
	val previousDistance = Array(0, 0)
	val numPlayers = if (localAddress == remoteAddress && localPort == remotePort) 2 else 1
	var whoStarted: WhoStarted = NooneStarted
	val raceInfoText = Array("", "", "", "", "")
	var raceInfoTextCounter = 0

	override def initTest(argDeserialized: Boolean) = {
		setTitle("Push of War")
		Mailbox.New

		textList.clear()
		HelpText.Init(this)
		for (i <- 0 until raceInfoText.length) raceInfoText(i) = ""
		raceInfoTextCounter = 0

		getWorld().setGravity(new Vec2(0, -45))

		objects clear ()
		this sendMessage Reset()
		clickObject = None
		val width = 150

		if (PushOfWarTest.IsRunning) {
			PushOfWarTest.QuickSave.getElement("Player1").getAttribute("points").value = Player1.Points.toString
			PushOfWarTest.QuickSave.getElement("Player2").getAttribute("points").value = Player2.Points.toString
		}
		PushOfWarTest.IsRunning = false

		// Stage
		new StaticBox(pow = this, w = width * 2)

		_load
		val playerObjCount = Array[Int](0, 0)
		playerObjCount.update(0, (objects filter (_.isPlayerBox) filter (_.playerId == 1) length))
		playerObjCount.update(1, (objects filter (_.isPlayerBox) filter (_.playerId == 2) length))

		for (i <- 0 to numPlayers - 1) {
			val sign = -1 + 2 * i
			val start = objects.length

			//Body
			if (playerObjCount(i) == 0) {
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
			this addObject new Spike(pow = this, x = sign * (width + 6), y = 0)
			this addObject new SpikeHard(pow = this, x = sign * (width + 4), y = 0)

			for (j <- start until objects.length) objects(j) setPlayerId (i + 1)
		}
	}

	def addRaceInfoText(text: String) = {
		for (i <- 1 until raceInfoText.length) raceInfoText(i - 1) = raceInfoText(i)
		raceInfoTextCounter += 1
		raceInfoText(raceInfoText.length - 1) = s"$raceInfoTextCounter: $text"
	}

	override def step(settings: TestbedSettings) = {
		super.step(settings)
		objects foreach (_ step settings)

		// Draw Game Info
		var textLine = 40
		getModel() getDebugDraw () drawString (frame.getBounds().getWidth().toFloat / 2.0F - 40, textLine, "Points", new Color3f(.6f, .61f, 1))
		textLine += 15
		getModel() getDebugDraw () drawString (frame.getBounds().getWidth().toFloat / 2.0F - 200, textLine, (s"Player 1 has ${Player1.Points} points ${if (Player1.IsDone) "(Done)" else ""}"), Color3f.WHITE)
		getModel() getDebugDraw () drawString (frame.getBounds().getWidth().toFloat / 2.0F + 20, textLine, (s"Player 2 has ${Player2.Points} points ${if (Player2.IsDone) "(Done)" else ""}"), Color3f.WHITE)
		textLine += 15
		raceInfoText foreach (text =>
			(textLine += 15, getModel() getDebugDraw () drawString (frame.getBounds().getWidth().toFloat / 2.0F - 90, textLine, text, Color3f.WHITE)))

		// Check to give point
		if (PushOfWarTest.IsRunning && checkForPoints) givePoints

		//If multiplayer synch simulation
		if (PushOfWarTest.IsRunning && whoStarted == IStarted) this sendMessage new Synch(this getObjectsData)

		// Check mailbox
		Mailbox.Get foreach handleMail
	}

	def handleMail(message: Message) = message match {
		case d: Done =>
			println(s"New mail: $message")
			if (!PushOfWarTest.IsRunning) {
				saveToFile
				PushOfWarTest loadPlayer1ToPlayer2From d.data
				Player2.IsDone = true
				if (Player1.IsDone) {
					PushOfWarTest.doLoadFromFile = false
					_load
				}
			}
		case r: Reset =>
			println(s"New mail: $message")
			Player2.IsDone = false
			if (PushOfWarTest.IsRunning)
				reset
		case r: Run =>
			println(s"New mail: $message")
			if (!PushOfWarTest.IsRunning) {
				whoStarted = OtherStarted
				saveToFile
				start
			}
			PushOfWarTest.IsRunning = true
		case m: Synch =>
			if (PushOfWarTest.IsRunning)
				this synchFrom m.data
		case KeyPressed(i) => i match {
			case 67 => // c
				println(s"New mail: $message")
				PushOfWarTest.IsRunning = false
				var objs = objects clone () filter (_.hasBeenCopied)
				if (numPlayers == 1) objs = objs filter (_.playerId == 1)
				objs foreach (o => (
					Player Get (o.playerId) GiverPoints o.Cost,
					this removeObject o))
				saveToFile
				reset
			case 68 => // d
				println(s"New mail: $message")
				if (!PushOfWarTest.IsRunning && numPlayers == 1 && !Player1.IsDone) {
					Player1.IsDone = true
					saveToFile
					this sendMessage new Done(this filterPlayer1 PushOfWarTest.QuickSave)
					if (Player2.IsDone) {
						PushOfWarTest.doLoadFromFile = false
						_load
					}
				}
			case 72 => // h
				println(s"New mail: $message")
				setShowInfo(!getShowInfo())
			case 83 => // s
				println(s"New mail: $message")
				if (!Player1.IsDone)
					spawnSpike(this getWorldMouse)
			case 10 => // enter
				println(s"New mail: $message")
				if (!PushOfWarTest.IsRunning && (numPlayers == 2 || (Player1.IsDone && Player2.IsDone))) {
					saveToFile
					start
					whoStarted = IStarted
					this sendMessage Run()
					PushOfWarTest.IsRunning = true
				}
			case 116 => // F5
				println(s"New mail: $message")
				if (!PushOfWarTest.IsRunning)
					saveToFile
			case 117 => // F6
				println(s"New mail: $message")
				if (!PushOfWarTest.IsRunning && (numPlayers == 2 || (!Player1.IsDone && !Player2.IsDone))) {
					PushOfWarTest.IsRunning = false
					PushOfWarTest.doLoadFromFile = true
					reset
				}
			case _ =>
		}
		case MouseUp(p) =>
			if (!Player1.IsDone) {
				clickObject foreach (_ mouseUp)
				clickObject foreach (_ click)
				clickObject = None
				objects foreach (_ stop)

				mouseJoint foreach (getWorld() destroyJoint _)
				mouseJoint = None
			}
		case MouseDown(p) =>
			if (!Player1.IsDone) {
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
		case m: Message => println("Unhandled message " + m)
		case _ => println("Unhandled message")
	}

	def getObjectsData = {
		val element = new XMLElement("Objects")
		objects foreach (obj => element addChild (obj getPosAsXMLElement))
		element
	}

	override def mouseDown(p: Vec2) = {
		//super.mouseDown(p) Do not call super!
		Mailbox.Get add new MouseDown(p)
	}

	def mouseDownShift(p: Vec2) = {
		val obj = takeOne(findObjects(p))
		obj foreach (o => Player Get (o.playerId) GiverPoints o.Cost)
		obj foreach (obj => if (obj.hasBeenCopied && !obj.isPlayerBox) this removeObject obj)
	}

	override def mouseUp(p: Vec2) = {
		super.mouseUp(p)
		Mailbox.Get add new MouseUp(p)
	}

	override def mouseMove(p: Vec2) = {
		super.mouseMove(p)
		mouseJoint foreach (_ setTarget p)
	}

	override def keyPressed(keyChar: Char, keyCode: Int) = {
		keyCode match {
			case 16 => // Shift
				KeyModifier.Shift = true
			case 17 => // Ctrl
				KeyModifier.Ctrl = true
			case 18 => // Alt
				KeyModifier.Alt = true
			case i: Int => Mailbox.Get add new KeyPressed(i)
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
		numPlayers match {
			case 1 =>
				val player2Objs = PushOfWarTest.QuickSave.getElement("Objects").children filter (_.getAttribute("playerId").value.toInt == 2)
				PushOfWarTest.QuickSave.children.clear
				PushOfWarTest.QuickSave.addAllChildren(List(new XMLElement("Objects"), new XMLElement("Player1"), new XMLElement("Player2")))
				objects filter (obj => obj.hasBeenCopied && obj.playerId == 1) foreach (obj => PushOfWarTest.QuickSave.getElement("Objects") addChild (obj toXMLElement))
				PushOfWarTest.QuickSave.getElement("Objects").children ++= player2Objs
			case 2 =>
				PushOfWarTest.QuickSave.children.clear
				PushOfWarTest.QuickSave.addAllChildren(List(new XMLElement("Objects"), new XMLElement("Player1"), new XMLElement("Player2")))
				objects filter (_.hasBeenCopied) foreach (obj => PushOfWarTest.QuickSave.getElement("Objects") addChild (obj toXMLElement))
		}
		PushOfWarTest.QuickSave.getElement("Player1").addAttribute("points", Player1.Points.toString)
		PushOfWarTest.QuickSave.getElement("Player2").addAttribute("points", Player2.Points.toString)

		println("Save")
	}

	def saveToFile = {
		quicksave
		PushOfWarTest saveToFile saveFile
	}

	override def reset = {
		Player1.IsDone = false
		mouseJoint = None
		whoStarted = NooneStarted
		this sendMessage Reset()
		super.reset
	}

	override def _load = {
		if (PushOfWarTest.doLoadFromFile == true)
			PushOfWarTest loadFromFile saveFile

		objects clone () foreach (this removeObject _)
		objects clear

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
			case "SpikeHard" => Option apply (new SpikeHard(this, x, y, angle))
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
		//println("Created " + obj.toString() + " at (" + obj.body.getPosition().x.toInt + "," + obj.body.getPosition().y.toInt + ")")
	}

	def getObject(id: Int) = objects filter (_.id == id) headOption

	def removeObject(obj: BaseObjectDynamic) = {
		objects -= obj
		obj.getConnectedObjects foreach (_ removeObject obj)
		obj.destroy
		getWorld destroyBody obj.body
		//println("Destroyed " + obj.toString())
	}

	def start = {
		objects filter (_ hasBeenCopied) foreach (_ activate)
		checkForPoints = true
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
			while (distance - previousDistance(playerBox.playerId - 1) > 28 && previousDistance(playerBox.playerId - 1) < 280) {
				Player Get (playerBox.playerId) GiverPoints (1)
				previousDistance(playerBox.playerId - 1) += 28
				addRaceInfoText(s"Player${playerBox.playerId} recieved 1 point")
			}
			if (distance > 280) {
				Player Get (playerBox.playerId) GiverPoints (10)
				checkForPoints = false
				addRaceInfoText(s"Player ${playerBox.playerId} won the race and recieved 10 points")
			}
		}
	}

	override def exit = {
		super.exit
		systemServer.shutdown
		println("Exit")
	}

	def filterPlayer1(element: XMLElement) = {
		// Copy
		val newEl = XMLElementFactory BuildFromXMLString (element.toString) head

		val playerObjs = newEl.getElement("Objects").children filter (_.getAttribute("playerId").value.toInt == 1)
		newEl.getElement("Objects").children.clear
		newEl.getElement("Objects").children ++= playerObjs
		newEl.children -= newEl getElement "Player2"

		newEl
	}

	def synchFrom(element: XMLElement) = {
		element.children foreach (el => el.getAttribute("x").value = (-1.0F * el.getAttribute("x").value.toFloat).toString)
		element.children foreach (el => el.getAttribute("angle").value = (Math.PI.toFloat - el.getAttribute("angle").value.toFloat).toString)
		element.children foreach (el => el.getAttribute("xVel").value = (-1.0F * el.getAttribute("xVel").value.toFloat).toString)
		element.children foreach (el => el.getAttribute("angleVel").value = (-1.0F * el.getAttribute("angleVel").value.toFloat).toString)

		element.children foreach (el => (this getObject (el.getAttribute("id").value.toInt)) foreach (obj => obj loadPos el))
	}

	def sendMessage(msg: Message) = if (numPlayers == 1) sendActor ! msg
}
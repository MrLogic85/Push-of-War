package com.sleepyduck.pushofwar

import org.jbox2d.testbed.framework.TestbedTest
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.testbed.framework.TestbedSettings
import org.jbox2d.dynamics.Body
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.dynamics.FixtureDef
import com.sleepyduck.pushofwar.model.Wheel
import com.sleepyduck.pushofwar.model.Wheel
import scala.collection.mutable.ArrayBuffer
import com.sleepyduck.pushofwar.model.BaseObject
import com.sleepyduck.pushofwar.model.StaticBox
import com.sleepyduck.pushofwar.model.Bar
import com.sleepyduck.pushofwar.model.BaseObjectDynamic
import com.sleepyduck.pushofwar.model.BaseObjectDynamic
import com.sleepyduck.pushofwar.model.Spike

class PushOfWarTest extends TestbedTest {
	override def getTestName = "Push of War Test"

	val objects = new ArrayBuffer[BaseObjectDynamic]
	var clickObjects = new ArrayBuffer[BaseObjectDynamic]

	override def initTest(argDeserialized: Boolean) = {
		setTitle("Couple of Things Test")

		getWorld().setGravity(new Vec2(0, -45))

		objects clear ()
		clickObjects clear ()

		val width = 70
		// Stage
		new StaticBox(
			pow = this,
			x = 0,
			y = -80,
			w = width,
			h = 80)

		// Wheels
		objects append new Wheel(pow = this, x = -15, y = 30/*, clockwise = true*/)
		objects append new Wheel(pow = this, x = -10, y = 30/*, clockwise = false*/)

		// Bars
		objects append new Bar(pow = this, x = 0, y = 30, w = 10, h = 1)
		
		// Spikes
		objects append new Spike(pow = this, x = 10, y = 30)
	}

	override def step(settings: TestbedSettings) {
		super.step(settings)
		objects foreach (o => o step settings)
	}

	override def mouseDown(p: Vec2) {
		clickObjects = objects filter (_ contains p)
		objects clone() foreach (o => if (o contains p) o mouseDown)
		super.mouseDown(p)
	}

	override def mouseUp(p: Vec2) {
		objects foreach (o => if ((o contains p) && !(clickObjects contains o)) o mouseUp)
		clickObjects foreach (o => o mouseUp)
		clickObjects filter (_ contains p) foreach (o => o click)
		objects foreach (o => o stop)
		super.mouseUp(p)
	}

	override def keyPressed(keyChar: Char, keyCode: Int) = keyChar toUpper match {
		case 'S' => start
		case _ =>
	}
	
	def destroyObject(o:BaseObjectDynamic) = {
		objects -= o
		getWorld() destroyBody o.body
	}

	def findObjects(p: Vec2) = objects filter (_ contains p)

	def addObject(obj: BaseObjectDynamic) = objects append obj

	def start = {
		objects filter (_ hasBeenCopied) foreach (o => o activate)
	}
}
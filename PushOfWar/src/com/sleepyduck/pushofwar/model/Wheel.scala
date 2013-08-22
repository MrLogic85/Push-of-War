package com.sleepyduck.pushofwar.model

import scala.collection.mutable.ArrayBuffer
import org.jbox2d.collision.shapes.ChainShape
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.Filter
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.joints.Joint
import org.jbox2d.dynamics.joints.RevoluteJoint
import com.sleepyduck.pushofwar.PushOfWarTest
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.pushofwar.WrappedWorld
import com.sleepyduck.pushofwar.model.RotationEnum._

class Wheel(pow: PushOfWarTest, x: Float = 0, y: Float = 0, angle: Float = 0, radius: Float = 2,
	var torque: Float = 1200, speed: Float = Float.MaxValue, var rotation: Rotation = NoEngine, copied: Boolean = false)
	extends Spike(pow, x, y, angle, copied) with CollisionHard {
	
	body getFixtureList () setUserData ColorChooserAlt

	def motorJoints = joints filter (_.getBodyA() == Wheel.this.body)

	override def getExtraFixture = {
		List(new FixtureDef {
			friction = 0
			density = 0
			shape = new ChainShape { createChain(arrowVertices, arrowVertices.length) }
			filter = new Filter {
				categoryBits = 0
				maskBits = 0
			}
		})
	}

	override def activate = {
		super.activate
		if (hasBeenCopied) {
			val jts = motorJoints
			jts foreach (j => (j setMaxMotorTorque (getTorque / jts.length), j enableMotor true, j setMotorSpeed rotationSpeed))
		}
	}

	def getTorque = rotation match { case NoEngine => 0 case _ => torque }

	override def isSpike = false

	override def copy = new Wheel(pow, body.getWorldCenter().x, body.getWorldCenter().y, body.getAngle(), radius, torque, speed, rotation)

	override def getShape = new CircleShape { this setRadius (Wheel.this.radius) }

	def rotationSpeed = speed * rotationSign

	def rotationSign = rotation match { case Clockwise => -1 case CounterClockwise => 1 case NoEngine => 0 }

	override def putAttributes(element: XMLElement) = {
		super.putAttributes(element)
		element addAttribute ("rotation", rotation toString)
	}

	override def initialize(element: XMLElement) = {
		super.initialize(element)
		rotation = RotationEnum.fromString(element.getAttribute("rotation").value)
		rotation match {
			case NoEngine =>
			case _ => val fixture = (body getFixtureList () getNext () getShape ()).asInstanceOf[ChainShape] createChain (arrowVertices, arrowVertices.length)
		}
	}

	def arrowVertices = {
		rotation match {
			case NoEngine =>
				val vertices = Array.ofDim[Vec2](5)
				vertices(0) = new Vec2(radius * 0.9F, 0)
				vertices(1) = new Vec2(-radius * 0.9F, 0)
				vertices(2) = new Vec2(0, 0)
				vertices(3) = new Vec2(0, radius * 0.9F)
				vertices(4) = new Vec2(0, -radius * 0.9F)
				vertices
			case _ =>
				val vertices = Array.ofDim[Vec2](9)
				for (i <- 0 until 5) (vertices(i) = new Vec2(-rotationSign * Math.cos(Math.PI * i / 8 + Math.PI / 4).toFloat, Math.sin(Math.PI * i / 8 + Math.PI / 4).toFloat) mul (radius / 2))
				vertices(5) = new Vec2(-rotationSign * Math.cos(Math.PI * 5 / 8).toFloat, Math.sin(Math.PI * 5 / 8).toFloat) mul (radius / 2) mul (1.5F)
				vertices(6) = new Vec2(-rotationSign * Math.cos(Math.PI * 3 / 4).toFloat, Math.sin(Math.PI * 3 / 4).toFloat) mul (radius / 2)
				vertices(7) = new Vec2(-rotationSign * Math.cos(Math.PI * 5 / 8).toFloat, Math.sin(Math.PI * 5 / 8).toFloat) mul (radius / 2) mul (0.5F)
				vertices(8) = new Vec2(-rotationSign * Math.cos(Math.PI * 3 / 4).toFloat, Math.sin(Math.PI * 3 / 4).toFloat) mul (radius / 2)
				vertices
		}
	}
}
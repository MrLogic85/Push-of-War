package com.sleepyduck.pushofwar.model

import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.common.Vec2
import org.jbox2d.testbed.framework.TestbedSettings
import com.sleepyduck.pushofwar.PushOfWarTest
import scala.collection.mutable.ArrayBuffer
import org.jbox2d.dynamics.joints.RevoluteJointDef
import org.jbox2d.dynamics.joints.Joint
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.joints.RevoluteJoint
import org.jbox2d.dynamics.Filter
import org.jbox2d.collision.shapes.ChainShape

object RotationEnum extends Enumeration {
	type Rotation = Value
	val Clockwise, CounterClockwise = Value
}
import RotationEnum._

class Wheel(pow: PushOfWarTest, collisionGroup: Filter, x: Float = 0, y: Float = 0, radius: Float = 2, torque: Float = 400,
	rotation: Rotation = Clockwise)
	extends Spike(pow, collisionGroup, x, y) {

	def motorJoint = (joints filter (_ map (_.getBodyA() == Wheel.this.body) getOrElse false) headOption) getOrElse None

	override def getExtraFixture = {
		List(new FixtureDef {
			friction = 0
			density = 0
			shape = new ChainShape {
				val vertices = Array.ofDim[Vec2](9)
				for (i <- 0 until 5) (vertices(i) = new Vec2(-rotationSign * Math.cos(Math.PI * i / 8 + Math.PI / 4).toFloat, Math.sin(Math.PI * i / 8 + Math.PI / 4).toFloat) mul (radius / 2))
				vertices(5) = new Vec2(-rotationSign * Math.cos(Math.PI * 5 / 8).toFloat, Math.sin(Math.PI * 5 / 8).toFloat) mul (radius / 2) mul (1.5F)
				vertices(6) = new Vec2(-rotationSign * Math.cos(Math.PI * 3 / 4).toFloat, Math.sin(Math.PI * 3 / 4).toFloat) mul (radius / 2)
				vertices(7) = new Vec2(-rotationSign * Math.cos(Math.PI * 5 / 8).toFloat, Math.sin(Math.PI * 5 / 8).toFloat) mul (radius / 2) mul (0.5F)
				vertices(8) = new Vec2(-rotationSign * Math.cos(Math.PI * 3 / 4).toFloat, Math.sin(Math.PI * 3 / 4).toFloat) mul (radius / 2)
				createChain(vertices, vertices.length)
			}
			filter = new Filter {
				categoryBits = 0
				maskBits = 0
			}
		})
	}

	override def activate = {
		super.activate
		if (hasBeenCopied) motorJoint foreach (j => (j setMaxMotorTorque torque, j enableMotor true, j setMotorSpeed rotationForce/2))
	}

	override def copy = new Wheel(pow, collisionGroup, x, y, radius, torque, rotation)

	override def getShape = new CircleShape { this setRadius (Wheel.this.radius) }

	def rotationForce = torque * rotationSign

	def rotationSign = rotation match { case Clockwise => -1 case CounterClockwise => 1 }
}
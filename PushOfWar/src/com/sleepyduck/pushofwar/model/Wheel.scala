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

object RotationEnum extends Enumeration {
	type Rotation = Value
	val Clockwise, CounterClockwise = Value
}
import RotationEnum._

class Wheel(pow: PushOfWarTest, x: Float = 0, y: Float = 0, radius: Float = 1, var torque: Float = 40,
	rotation: Rotation = Clockwise, friction: Float = 1F, density: Float = 1F)
	extends BaseObjectDynamic(pow) {

	body = pow getWorld () createBody new BodyDef {
		`type` = BodyType.DYNAMIC
		position set (x, y)
		gravityScale = 0
	}

	body createFixture new FixtureDef {
		friction = Wheel.this.friction
		density = Wheel.this.density
		shape = new CircleShape
		shape setRadius Wheel.this.radius
		isSensor = true
	}
	
	body.m_flags

	val spike: Option[Spike] = Option apply new Spike(pow, body getPosition () x, body getPosition () y) {
		hasBeenCopied = true

		override def jointDefCreated(jointDef: RevoluteJointDef) = {
			if (jointDef.bodyB == Wheel.this.body && (spike map (jointDef.bodyA == _.body) getOrElse false)) {
				jointDef enableMotor = true
				jointDef maxMotorTorque = rotation match {
					case Clockwise => torque
					case CounterClockwise => -torque
				}
			}
		}
	}
	spike foreach (s => s addJoint (s,this))

	override def mouseUp = {
		super.mouseUp
		spike foreach (_ mouseUp)
	}

	override def mouseDown = {
		super.mouseDown
		spike foreach (_ mouseDown)
	}

	def copy = new Wheel(pow, x, y, radius, torque, rotation, friction, density)
}
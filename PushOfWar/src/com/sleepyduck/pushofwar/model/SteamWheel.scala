package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import com.sleepyduck.pushofwar.model.RotationEnum._
import org.jbox2d.dynamics.Filter

class SteamWheel(pow: PushOfWarTest, collisionGroup: Filter, x: Float = 0, y: Float = 0, torque: Float = 10000000,
	rotation: Rotation = Clockwise, copied: Boolean = false)
	extends Wheel(pow, collisionGroup, x, y, 4, torque, 2, rotation, copied) {

	override def copy = new SteamWheel(pow, collisionGroup, x, y, torque, rotation, copied)
}
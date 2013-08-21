package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import com.sleepyduck.pushofwar.model.RotationEnum._
import org.jbox2d.dynamics.Filter

class SteamWheel(pow: PushOfWarTest, collisionGroup: Filter = CollissionGroupNone, x: Float = 0, y: Float = 0, angle:Float = 0,
	rotation: Rotation = Clockwise, copied: Boolean = false)
	extends Wheel(pow, collisionGroup, x, y, angle, 4, 6000, 2, rotation, copied) {

	override def copy = new SteamWheel(pow, collisionGroup, body.getWorldCenter().x, body.getWorldCenter().y, body.getAngle(), rotation, copied)
}
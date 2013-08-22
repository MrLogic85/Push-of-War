package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import com.sleepyduck.pushofwar.model.RotationEnum._
import org.jbox2d.dynamics.Filter

class SteamWheel(pow: PushOfWarTest, x: Float = 0, y: Float = 0, angle:Float = 0,
	rotationSW: Rotation = NoEngine, copied: Boolean = false)
	extends Wheel(pow, x, y, angle, 4, 40000, 2, rotationSW, copied) {

	override def copy = new SteamWheel(pow, body.getWorldCenter().x, body.getWorldCenter().y, body.getAngle(), rotation, copied)
}
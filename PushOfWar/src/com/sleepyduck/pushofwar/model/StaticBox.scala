package com.sleepyduck.pushofwar.model

import org.jbox2d.testbed.framework.TestbedSettings
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.PolygonShape
import com.sleepyduck.pushofwar.PushOfWarTest

class StaticBox(pow: PushOfWarTest, x: Float = 0, y: Float = 0, w: Float = 1, h:Float =1)
	extends BaseObject(pow) {

	body = pow getWorld() createBody new BodyDef {
		`type` = BodyType.STATIC
		position set (x, y)
	}

	body createFixture new FixtureDef {
		density = 1
		shape = new PolygonShape {
			setAsBox(w, h)
		}
	}
}
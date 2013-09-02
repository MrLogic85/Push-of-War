package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Filter
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.ChainShape
import com.sleepyduck.pushofwar.util.ColorChooserAlt

object BarHard extends AnyRef with Cost10

class BarHard(pow: PushOfWarTest, x: Float = 0, y: Float = 0, angle: Float = 0, w: Float = 10, h: Float = 1, copied: Boolean = false)
	extends Bar(pow, x, y, angle, w, h, copied) with CollisionHard with Cost10 {

	body getFixtureList () setUserData ColorChooserAlt

	override def copy = new BarHard(pow, body.getWorldCenter().x, body.getWorldCenter().y, body.getAngle(), w, h)
}
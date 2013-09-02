package com.sleepyduck.pushofwar.model

import com.sleepyduck.pushofwar.PushOfWarTest
import com.sleepyduck.pushofwar.util.KeyModifier
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.dynamics.BodyType
import scala.collection.mutable.ArrayBuffer
import org.jbox2d.dynamics.joints.Joint
import org.jbox2d.dynamics.joints.RevoluteJointDef
import org.jbox2d.dynamics.joints.RevoluteJoint
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Filter
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.xml.Attribute
import com.sleepyduck.xml.XMLElement
import com.sleepyduck.pushofwar.util.ColorChooserAlt

object SpikeHard extends AnyRef with Cost2

class SpikeHard(pow: PushOfWarTest, x: Float = 0, y: Float = 0, angle: Float = 0, copied: Boolean = false)
	extends Spike(pow, x, y, angle, copied) with CollisionHard {
	
	body getFixtureList () setUserData ColorChooserAlt

	override def copy = new SpikeHard(pow, body.getWorldCenter().x, body.getWorldCenter().y, body.getAngle())
}
package com.sleepyduck.pushofwar.model

import org.jbox2d.testbed.framework.TestbedSettings
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import com.sleepyduck.pushofwar.PushOfWarTest
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.Filter

case class CollissionGroupStatic extends Filter { categoryBits = 0x1; maskBits = 0xFF & ~0x1 }
case class CollissionGroupPlayer1 extends Filter { categoryBits = 0x2; maskBits = 0xFF & ~0x2 & ~0x4 }
case class CollissionGroupPlayer1Alt extends Filter { categoryBits = 0x4; maskBits = 0xFF & ~0x2 }
case class CollissionGroupPlayer2 extends Filter { categoryBits = 0x8; maskBits = 0xFF & ~0x8 & ~0x10 }
case class CollissionGroupPlayer2Alt extends Filter { categoryBits = 0x10; maskBits = 0xFF & ~0x8 }

object CollissionGroupStatic {}
object CollissionGroupPlayer1 {}
object CollissionGroupPlayer1Alt {}
object CollissionGroupPlayer2 {}
object CollissionGroupPlayer2Alt {}

abstract class BaseObject(pow: PushOfWarTest, collisionGroup: Filter, x: Float, y: Float) {

	val body: Body = pow getWorld () createBody new BodyDef {
		`type` = BodyType.DYNAMIC
		position set (x, y)
		gravityScale = 0
		linearDamping = 10
		angularDamping = 10
	}

	getExtraFixture foreach (body createFixture _)
	body createFixture getFixture

	body getFixtureList () setFilterData collisionGroup

	def getFixture: FixtureDef
	def getExtraFixture: List[FixtureDef] = List()

	def step(settings: TestbedSettings) = {}

	def move(distance: Vec2) = {
		body setTransform ((body getPosition) add distance, body getAngle)
	}

	def click = {}

	def mouseDown(p: Vec2) = {}

	def mouseUp = {}

	override def equals(arg: Any): Boolean = {
		arg match {
			case x: AnyRef => this eq x
			case _ => super.equals(arg)
		}
	}
}
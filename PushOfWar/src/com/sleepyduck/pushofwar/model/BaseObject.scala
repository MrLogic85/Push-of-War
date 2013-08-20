package com.sleepyduck.pushofwar.model

import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.Filter
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.testbed.framework.TestbedSettings

import com.sleepyduck.pushofwar.PushOfWarTest

object CollissionGroupNone extends Filter { categoryBits = 0x80; maskBits = 0x1 }
object CollissionGroupStatic extends Filter { categoryBits = 0x1; maskBits = 0xFF & ~0x1 }
object CollissionGroupPlayer1 extends Filter { categoryBits = 0x2; maskBits = 0xFF & ~0x2 & ~0x4 }
object CollissionGroupPlayer1Alt extends Filter { categoryBits = 0x4; maskBits = 0xFF & ~0x2 }
object CollissionGroupPlayer2 extends Filter { categoryBits = 0x8; maskBits = 0xFF & ~0x8 & ~0x10 }
object CollissionGroupPlayer2Alt extends Filter { categoryBits = 0x10; maskBits = 0xFF & ~0x8 }

abstract class BaseObject(pow: PushOfWarTest, collisionGroup: Filter, x: Float, y: Float) {

	var id = (Math.random() * Int.MaxValue).toInt

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
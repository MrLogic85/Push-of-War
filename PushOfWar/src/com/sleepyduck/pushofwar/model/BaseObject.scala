package com.sleepyduck.pushofwar.model

import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.Filter
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.testbed.framework.TestbedSettings

import com.sleepyduck.pushofwar.PushOfWarTest

object CollisionGroup {
	def GetByName(name: String) = name match {
		case "CollissionGroupStatic$" => CollissionGroupStatic
		case "CollissionGroupPlayer1$" => CollissionGroupPlayer1
		case "CollissionGroupPlayer1Alt$" => CollissionGroupPlayer1Alt
		case "CollissionGroupPlayer1None$" => CollissionGroupPlayer1None
		case "CollissionGroupPlayer2$" => CollissionGroupPlayer2
		case "CollissionGroupPlayer2Alt$" => CollissionGroupPlayer2Alt
		case "CollissionGroupPlayer2None$" => CollissionGroupPlayer2None
	}
}

object CollissionGroupStatic extends Filter { categoryBits = 0x1; maskBits = 0xFF & ~0x1 }
object CollissionGroupPlayer1 extends Filter { categoryBits = 0x2; maskBits = 0xFF & ~0x2 & ~0x4 }
object CollissionGroupPlayer1Alt extends Filter { categoryBits = 0x4; maskBits = 0xFF & ~0x2 }
object CollissionGroupPlayer1None extends Filter { categoryBits = 0x80; maskBits = 0x1 }
object CollissionGroupPlayer2 extends Filter { categoryBits = 0x8; maskBits = 0xFF & ~0x8 & ~0x10 }
object CollissionGroupPlayer2Alt extends Filter { categoryBits = 0x10; maskBits = 0xFF & ~0x8 }
object CollissionGroupPlayer2None extends Filter { categoryBits = 0x80; maskBits = 0x1 }

class CollissionGroupStatic extends Filter
class CollissionGroupPlayer1 extends Filter
class CollissionGroupPlayer1Alt extends Filter
class CollissionGroupPlayer1None extends Filter
class CollissionGroupPlayer2 extends Filter
class CollissionGroupPlayer2Alt extends Filter
class CollissionGroupPlayer2None extends Filter

abstract class BaseObject(pow: PushOfWarTest, x: Float, y: Float, angle: Float = 0) {

	var id = pow getUniqueId

	val body: Body = pow getWorld () createBody new BodyDef {
		`type` = BodyType.DYNAMIC
		position set (x, y)
		angle = BaseObject.this.angle
		gravityScale = 0
		linearDamping = 10
		angularDamping = 10
		awake = false
	}

	getExtraFixture foreach (body createFixture _)
	body createFixture getFixture

	body getFixtureList () setFilterData CollissionGroupStatic

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
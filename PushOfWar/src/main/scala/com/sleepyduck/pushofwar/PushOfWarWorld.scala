package com.sleepyduck.pushofwar

import org.jbox2d.dynamics.World
import org.jbox2d.common.Vec2
import org.jbox2d.pooling.IWorldPool
import org.jbox2d.collision.broadphase.BroadPhaseStrategy
import org.jbox2d.callbacks.DebugDraw
import org.jbox2d.dynamics.Body
import org.jbox2d.common.Transform
import org.jbox2d.dynamics.Fixture
import org.jbox2d.common.Color3f
import org.jbox2d.dynamics.BodyType

class PushOfWarWorld(gravity: Vec2)
	extends WrappedWorld(gravity) {
}
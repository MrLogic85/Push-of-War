package com.sleepyduck.pushofwar.model

import org.jbox2d.testbed.framework.TestbedSettings
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import com.sleepyduck.pushofwar.PushOfWarTest

abstract class BaseObject(pow: PushOfWarTest) {
	var body:Body = null
	
	def step(settings: TestbedSettings) = {}
	
	def click = {}
	def mouseDown = {}
	def mouseUp = {}
}
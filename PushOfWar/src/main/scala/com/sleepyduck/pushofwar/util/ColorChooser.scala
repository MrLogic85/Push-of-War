package com.sleepyduck.pushofwar.util

import org.jbox2d.common.Color3f
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyType

object ColorChooser extends ColorChooser {
	override def sleepyColor = new Color3f(0.15F,0.35F,0.15F)
	override def awakeColor = new Color3f(0.3F,0.7F,0.3F)
}
object ColorChooserAlt extends ColorChooser {
	override def sleepyColor = new Color3f(0.15F,0.15F,0.35F)
	override def awakeColor = new Color3f(0.3F,0.3F,0.7F)
}

object ColorChooserInvisible extends ColorChooser {
	override def sleepyColor = new Color3f(0,0,0)
	override def awakeColor = new Color3f(0,0,0)
}

class ColorChooser() {

	def setColor(body: Body, color: Color3f) = {
		if (body.isActive() == false) color set inactiveColor
		else if (body.getType() == BodyType.STATIC) color set staticColor
		else if (body.getType() == BodyType.KINEMATIC) color set kinematicColor
		else if (body.isAwake() == false) color set sleepyColor
		else color set awakeColor
	}

	def inactiveColor: Color3f = new Color3f(0.5f, 0.5f, 0.3f)
	def staticColor: Color3f = new Color3f(0.5f, 0.9f, 0.3f)
	def kinematicColor: Color3f = new Color3f(0.5f, 0.5f, 0.9f)
	def sleepyColor: Color3f = new Color3f(0.5f, 0.5f, 0.5f)
	def awakeColor: Color3f = new Color3f(0.9f, 0.7f, 0.7f)
}
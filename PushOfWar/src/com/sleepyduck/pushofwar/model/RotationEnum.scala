package com.sleepyduck.pushofwar.model

object RotationEnum extends Enumeration {
	type Rotation = Value
	val Clockwise, CounterClockwise, NoEngine = Value
	def fromString(str: String) = str match {
		case "Clockwise" => Clockwise
		case "CounterClockwise" => CounterClockwise
		case "NoEngine" => NoEngine
	}
}
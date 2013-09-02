package com.sleepyduck.pushofwar.communication

import com.sleepyduck.xml.XMLElement
import org.jbox2d.common.Vec2

class Message

case object Reset
case object Run
case object Connected

case class Reset extends Message
case class Run extends Message

case class Synch(val data: XMLElement) extends Message  {
	override def toString = s"${getClass().getSimpleName()}(XMLElement(${data.name}))"
}
case class Done(val data: XMLElement) extends Message  {
	override def toString = s"${getClass().getSimpleName()}(XMLElement(${data.name}))"
}

case class KeyPressed(keyCode: Int) extends Message
case class MouseUp(p: Vec2) extends Message
case class MouseDown(p: Vec2) extends Message
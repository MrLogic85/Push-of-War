package com.sleepyduck.pushofwar.communication

import com.sleepyduck.xml.XMLElement

class Message
case object Reset extends Message
case object Run extends Message
case class Synch(val data: XMLElement) extends Message
case class Done(val data: XMLElement) extends Message
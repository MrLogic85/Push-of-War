package com.sleepyduck.pushofwar.util

import com.sleepyduck.pushofwar.PushOfWarTest
import com.sleepyduck.pushofwar.model.Bar
import com.sleepyduck.pushofwar.model.Spike
import com.sleepyduck.pushofwar.model.SteamWheel
import com.sleepyduck.pushofwar.model.Wheel

object HelpText {
	def Init(test: PushOfWarTest) = {
		test addTextLine "Click and drag the left mouse button to move objects."
		test addTextLine "Click and drag the right mouse button to move the screen."
		test addTextLine "Zoom in and out using the scroll wheel."
		test addTextLine "Wheels and spikes can be conected to other objects by releasing"
		test addTextLine "them when the center of the wheel is intersecting with another item."
		test addTextLine "Ctrl-click to copy connected objects."
		test addTextLine "Shift click to remove a object."
		test addTextLine "Press 'S' to quickly spawn a spike where the mouse is."
		test addTextLine "Press Enter to start the fight."
		test addTextLine "Press 'R' to reset the building process to the same state as before the fight started."
		test addTextLine "Press 'C' to clear the building process."
		test addTextLine "Press F5 save the game to disk."
		test addTextLine "Press F6 load the game from disk."
		test addTextLine "Press 'H' to toggle the help."
		test addTextLine ""
		test addTextLine "If you are playing multiplayer"
		test addTextLine "Press 'D' to tell the orther player you are ready to fight."
		test addTextLine "When both players are done, any one of you can press Enter to start the fight."
		test addTextLine ""
		test addTextLine "Player 1 is on the left."
		test addTextLine "Player 2 is on the right."
		test addTextLine s"Large wheels cost ${SteamWheel.Cost} points."
		test addTextLine s"Wheels cost ${Wheel.Cost} points."
		test addTextLine s"Bars and triangles cost ${Bar.Cost} points."
		test addTextLine s"Spikes cost ${Spike.Cost} points."
	}
}
package com.sleepyduck.pushofwar

import java.awt.Dimension
import java.net.InetAddress

import org.jbox2d.common.Vec2
import org.jbox2d.testbed.framework.TestbedSettings
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D

import javax.swing.JFrame
import javax.swing.UIManager

object Main extends App {
	override def main(args: Array[String]) {
		UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		val model = new PushOfWarModel();
		val panel = new TestPanelJ2D(model);
		panel.setPreferredSize(new Dimension(1500, 1000));

		model.getSettings().getSetting(TestbedSettings.DrawHelp).enabled = false

		model.addCategory("Push of War Tests"); // add a category
		val networking = this parse args
		val test = new PushOfWarTest(localAddress = networking(0), remoteAddress = networking(1), localPort = networking(2), remotePort = networking(3), saveFile = networking(4))
		test.setShowInfo(false)
		model.addTest(test); // add our test

		val testbed = new WrappedTestbedFrame(model, panel);
		test.setFrame(testbed)
		testbed.setVisible(true);
		test.setCamera(new Vec2(0, 0), 6F)
		testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	def parse(args: Array[String]) = {
		val networking = Array[String](InetAddress.getLocalHost.getHostAddress, "127.0.0.1", "2552", "2552", "SaveFile")
		for (i <- 0 until args.length) args(i) match {
			case "-la" => networking(0) = args(i + 1)
			case "-ra" => networking(1) = args(i + 1)
			case "-lp" => networking(2) = args(i + 1)
			case "-rp" => networking(3) = args(i + 1)
			case "-save" => networking(4) = args(i + 1)
			case _ =>
		}
		if (networking(1) == "127.0.0.1")
			networking(0) = "127.0.0.1"
		networking
	}
}
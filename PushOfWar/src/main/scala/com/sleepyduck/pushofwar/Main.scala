package com.sleepyduck.pushofwar

import org.jbox2d.testbed.framework._
import org.jbox2d.testbed.framework.TestList
import org.jbox2d.testbed.framework.TestbedModel
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D
import javax.swing.JFrame
import javax.swing.UIManager
import org.jbox2d.common.Vec2
import java.awt.Dimension

object Main extends App {
	override def main(args: Array[String]) {
		UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		val model = new PushOfWarModel();
		val panel = new TestPanelJ2D(model);
		panel.setPreferredSize(new Dimension(1500, 600));

		model.getSettings().getSetting(TestbedSettings.DrawHelp).enabled = false

		model.addCategory("Push of War Tests"); // add a category
		val test = new PushOfWarTest
		if (args.length > 0)
			test.remoteAddress = args(0) 
		model.addTest(test); // add our test

		val testbed = new WrappedTestbedFrame(model, panel);
		test.setFrame(testbed)
		testbed.setVisible(true);
		test.setCamera(new Vec2(0, 0), 6F)
		testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
package com.sleepyduck.pushofwar

import org.jbox2d.testbed.framework._
import org.jbox2d.testbed.framework.TestList
import org.jbox2d.testbed.framework.TestbedModel
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D
import javax.swing.JFrame
import javax.swing.UIManager
import org.jbox2d.common.Vec2

object Main extends App {
	UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	val model = new PushOfWarModel();
	val panel = new TestPanelJ2D(model);
	
	model.addCategory("Push of War Tests"); // add a category
	val test = new PushOfWarTest
	model.addTest(test); // add our test

	val testbed = new WrappedTestbedFrame(model, panel);
	testbed.setVisible(true);
	test.setCamera(new Vec2(0,0), 4F)
	testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}
package com.sleepyduck.pushofwar

import org.jbox2d.testbed.framework._
import org.jbox2d.testbed.framework.TestList
import org.jbox2d.testbed.framework.TestbedModel
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D

import javax.swing.JFrame
import javax.swing.UIManager

object Main extends App {
	UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	val model = new TestbedModel();
	val panel = new TestPanelJ2D(model);
	
	model.addCategory("Push of War Tests"); // add a category
	model.addTest(new PushOfWarTest()); // add our test
	TestList.populateModel(model);

	// add our custom setting "My Range Setting", with a default value of 10, between 0 and 20
	model.getSettings().addSetting(new TestbedSetting("My Range Setting", 10, 0, 20));

	val testbed = new WrappedTestbedFrame(model, panel);
	testbed.setVisible(true);
	testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}
package com.sleepyduck.pushofwar;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;

import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedController.UpdateBehavior;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;

@SuppressWarnings("serial")
public class WrappedTestbedFrame extends JFrame {

	  private TestbedModel model;
	  private TestbedController controller;

	  public WrappedTestbedFrame(final TestbedModel argModel, final TestbedPanel argPanel) {
	    super("Push of War");
	    setLayout(new BorderLayout());

	    model = argModel;
	    model.setDebugDraw(argPanel.getDebugDraw());
	    controller = new TestbedController(model, argPanel, UpdateBehavior.UPDATE_CALLED);
	    
	    add((Component) argPanel, "Center");
	    pack();

	    controller.playTest(0);
	    controller.start();
	  }
}

package com.sleepyduck.pushofwar;

import org.jbox2d.testbed.framework.TestbedController.UpdateBehavior;
import org.jbox2d.testbed.framework.TestbedFrame;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;

@SuppressWarnings("serial")
public class WrappedTestbedFrame extends TestbedFrame {
	public WrappedTestbedFrame(TestbedModel argModel, TestbedPanel argPanel) {
		super(argModel, argPanel, UpdateBehavior.UPDATE_CALLED);
	}
}

package com.sleepyduck.pushofwar;

public class TestbedSetting extends org.jbox2d.testbed.framework.TestbedSetting {

	public TestbedSetting(String argName, boolean argValue) {
		super(argName, SettingType.ENGINE, argValue);
	}

	public TestbedSetting(String argName, int argValue, int argMinimum, int argMaximum) {
		super(argName, SettingType.ENGINE, argValue, argMinimum, argMaximum);
	}
}

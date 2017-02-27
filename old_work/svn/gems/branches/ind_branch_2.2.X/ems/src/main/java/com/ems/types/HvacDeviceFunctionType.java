/**
 * 
 */
package com.ems.types;

/**
 * @author yogesh
 * 
 */
public enum HvacDeviceFunctionType {
	READ_ROOM_TEMP(1000), READ_AMB_TEMP(1001), READ_COMPRESSOR_STATUS(1002), READ_MODBUS_STATUS(1003),

	SET_ROOM_TEMP(2000), SET_FAN_SPEED(2001), SET_RUNNING_MODE(2002);

	public enum FanSpeed {
		Auto(0), Low(1), Medium(2), High(3);

		int id;

		private FanSpeed(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	};

	public enum RunningMode {
		OFF(1), Cool(9), Dry(10), Fan(11), Heat(12);

		int id;

		private RunningMode(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	};

	public enum HvacErrorCodes {
		INVALID_FUNCTION(1), INVALID_ARGUMENT(2), NO_ARGUMENT(3), INVALID_RANGE(4);

		int id;

		private HvacErrorCodes(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

	private int functionId;
	private int args;

	private HvacDeviceFunctionType(int functionid) {
		this.functionId = functionid;
	}

	public static HvacDeviceFunctionType valueOf(int value) {
		HvacDeviceFunctionType[] oList = HvacDeviceFunctionType.values();
		int length = oList.length;
		for (int i = 0; i < length; i++) {
			if (value == oList[i].getFunctionId())
				return oList[i];
		}
		return null;
	}

	public static FanSpeed getFanSpeed(int value) {
		FanSpeed[] oList = FanSpeed.values();
		int length = oList.length;
		for (int i = 0; i < length; i++) {
			if (value == oList[i].getId())
				return oList[i];
		}
		return null;
	}

	public static RunningMode getRunningMode(int value) {
		RunningMode[] oList = RunningMode.values();
		int length = oList.length;
		for (int i = 0; i < length; i++) {
			if (value == oList[i].getId())
				return oList[i];
		}
		return null;
	}

	/**
	 * @return the id
	 */
	public int getFunctionId() {
		return functionId;
	}

	/**
	 * @return the args
	 */
	public int getArgs() {
		return args;
	}
}

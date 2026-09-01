package org.firstinspires.ftc.teamcode;

/**
 * HardwareNames.java
 *
 * Central list of hardware device names, as configured on the Driver
 * Station (Robot Configuration).
 *
 * STUDENT: Every OpMode needs hardwareMap.get(SomeType.class, "deviceName")
 * to find a physical device. Typing that name string in every OpMode is
 * risky - one typo and hardwareMap.get() throws an exception at runtime.
 * Referencing a constant from here instead means a typo becomes a compile
 * error (caught immediately), and renaming a device on the robot only
 * requires changing it in ONE place.
 *
 * IMPORTANT: These strings must exactly match the names typed into the
 * Driver Station's Robot Configuration for the test bench.
 */

public final class HardwareNames {

    // Prevent creating instances - this class is just a bucket of constants
    private HardwareNames() {
    }

    // ========== TEST BENCH HARDWARE ==========
    // 1 DC Motor, 2 Servos (1 positional, 1 continuous), 1 Camera,
    // 1 Touch Sensor, 1 Color Sensor

    public static final String DC_MOTOR = "motor_test_bench";
    public static final String SERVO_POSITION = "torque_servo_test_bench";
    public static final String SERVO_CONTINUOUS = "speed_servo_test_bench";
    public static final String TOUCH_SENSOR = "touch_sensor_test_bench";
    public static final String COLOR_SENSOR = "color_sensor_test_bench";
    public static final String WEBCAM = "webcam_test_bench";

    // STUDENT: Not every servo has the same physical range of motion!
    // This one measures ~300° in practice, not the "standard" 180° - always
    // verify against the real hardware instead of assuming.
    public static final double SERVO_POSITION_MAX_DEGREES = 300.0;
}

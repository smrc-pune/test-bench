package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;

/**
 * DashboardConfig.java
 *
 * Central list of values that are live-tunable from FTC Dashboard
 * (http://192.168.43.1:8080/dash while connected to the Control Hub's WiFi).
 *
 * STUDENT: The @Config annotation tells FTC Dashboard to show every public
 * static field below as an editable value in the browser - change it there
 * while the OpMode is RUNNING, no rebuild/redeploy needed. This is for
 * values you want to tune "by feel" (like a feedback-control gain), not
 * for facts about the hardware (like a device name or a servo's real
 * degree range) - those stay in HardwareNames.java as normal constants.
 *
 * IMPORTANT: Unlike a normal constant, these CANNOT be "final" - Dashboard
 * needs to write to them at runtime. That's a real trade-off: any code can
 * now accidentally reassign these too. Only put a value here if you
 * actually want it live-tunable.
 */
@Config
public class DashboardConfig {

    // Prevent creating instances - this class is just a bucket of tunable values
    private DashboardConfig() {
    }

    // ========== ConceptAprilTagAimServo ==========

    // How aggressively the servo reacts to bearing error (the "P" gain).
    // Too high = jittery overshoot; too low = sluggish aiming.
    public static double AIM_GAIN = 0.02;

    // ========== ConceptDcMotorVelocity ==========

    // STUDENT: This is a STARTING GUESS, not a verified spec - a motor's
    // true max velocity depends on its specific gear ratio and encoder
    // CPR, which varies by hardware. Tune this live from Dashboard while
    // ConceptDcMotorVelocity is running: push the stick to full, watch
    // "Actual Velocity" in telemetry, and raise or lower this number until
    // full stick actually reaches the motor's real top speed.
    public static double MOTOR_MAX_VELOCITY_TICKS_PER_SEC = 2000.0;
}

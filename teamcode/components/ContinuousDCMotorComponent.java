package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * ContinuousDCMotorComponent.java
 *
 * STUDENT GUIDE: Continuous DC Motor Control (Velocity-Based)
 *
 * PURPOSE:
 * Controls a DC motor with continuous velocity operation.
 * Used for: drivetrain motors, wheels, fans, spinners - anything that
 * runs indefinitely at a set speed without a target position.
 *
 * KEY CHARACTERISTICS:
 * - Runs continuously at a set power/velocity
 * - No target position to reach
 * - Control value is power (-1.0 to 1.0)
 * - Useful for open-loop control systems
 *
 * DIFFERENCE FROM PositionDCMotorComponent:
 * This component: "Run at 50% speed forward" (velocity-based)
 * Position motor: "Rotate 500 ticks" (position-based with target)
 *
 * FTC SDK CONCEPTS:
 * - DcMotor: Basic motor control
 * - setPower(power): Set velocity directly
 * - setDirection(FORWARD/REVERSE): Tells the SDK to flip sign internally,
 *   so setPower()/getPower() always stay in the caller's original frame
 * - ZeroPowerBehavior: BRAKE vs FLOAT when power is 0
 */

public class ContinuousDCMotorComponent {

    // The FTC SDK motor object
    private DcMotor motor;

    // Track current power for telemetry
    // Since we use motor.setDirection() for reversal (not manual negation),
    // this always matches exactly what the caller passed to setPower()
    private double currentPower = 0;

    // Optional: Track run direction for intuitive control
    // Some motors need to be reversed if wired backwards
    private boolean isReversed = false;

    /**
     * Constructor - Initialize continuous motor with normal direction
     *
     * @param motor The FTC DcMotor object from HardwareConfig
     *
     * EXAMPLE:
     * DcMotor driveMotor = hardware.dcMotor;
     * ContinuousDCMotorComponent drive = new ContinuousDCMotorComponent(driveMotor);
     */
    public ContinuousDCMotorComponent(DcMotor motor) {
        this(motor, false);  // Default: not reversed
    }

    /**
     * Constructor - Initialize continuous motor with optional reversal
     *
     * STUDENT: Use this if your motor runs backwards compared to controls.
     * Example: Left and right drive motors might spin opposite directions
     * to move forward together.
     *
     * @param motor The FTC DcMotor object
     * @param reversed true if motor direction should be reversed
     *
     * EXAMPLE:
     * // Right side motor runs backwards, so reverse it
     * ContinuousDCMotorComponent rightDrive =
     *     new ContinuousDCMotorComponent(hardware.dcMotor, true);
     */
    public ContinuousDCMotorComponent(DcMotor motor, boolean reversed) {
        this.motor = motor;
        this.isReversed = reversed;

        // FTC SDK: setDirection() tells the SDK to flip the sign internally
        // for every setPower()/getPower() call from here on. This keeps our
        // own power tracking in the caller's original frame - no manual
        // negation needed anywhere else in this class.
        motor.setDirection(reversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
    }

    /**
     * Set motor power (speed and direction)
     *
     * STUDENT: Main control method. Call this in your loop.
     *
     * @param power Value from -1.0 (full reverse) to 1.0 (full forward)
     *
     * BEHAVIOR:
     * - Positive power: Motor spins forward at proportional speed
     * - Negative power: Motor spins backward at proportional speed
     * - 0: Motor stops (applies BRAKE if configured)
     *
     * FTC SDK DETAIL:
     * motor.setPower() sets the PWM (pulse-width modulation) signal
     * to the motor controller, which controls the actual voltage
     * and direction sent to the motor.
     *
     * STUDENT LEARNING:
     * This is OPEN-LOOP control:
     * - You set a power value
     * - Motor runs at that power
     * - You don't know if it actually reached the power
     * - No feedback about motor speed or position
     *
     * Contrast with CLOSED-LOOP control (PositionDCMotorComponent)
     * which targets a specific position and adjusts power to reach it.
     */
    public void setPower(double power) {
        // Clamp power to valid range
        power = Math.max(-1.0, Math.min(1.0, power));

        // Send to motor - the SDK applies the sign flip internally based on
        // the direction set in the constructor/setReversed(), so we always
        // pass the caller's original value here (no manual negation)
        motor.setPower(power);
        currentPower = power;
    }

    /**
     * Get current motor power
     *
     * @return Power value we last set (-1.0 to 1.0), exactly as passed to setPower()
     */
    public double getPower() {
        return currentPower;
    }

    /**
     * Stop motor immediately
     *
     * STUDENT: Convenience method to stop the motor.
     * Applies BRAKE behavior configured in HardwareConfig.
     */
    public void stop() {
        setPower(0);
    }

    /**
     * Check if motor is currently running
     *
     * @return true if power is not zero (motor is running)
     */
    public boolean isRunning() {
        return currentPower != 0;
    }

    /**
     * Get motor direction (reversed or not)
     *
     * @return true if motor is reversed, false otherwise
     */
    public boolean isReversed() {
        return isReversed;
    }

    /**
     * Reverse motor direction after initialization
     *
     * STUDENT: Use this if you need to change direction mid-match.
     * Useful for configuring motors dynamically.
     *
     * @param reversed true to reverse, false for normal
     *
     * FTC SDK DETAIL:
     * Updates motor.setDirection() immediately, so the very next
     * setPower() call uses the new direction.
     */
    public void setReversed(boolean reversed) {
        this.isReversed = reversed;
        motor.setDirection(reversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
    }
}
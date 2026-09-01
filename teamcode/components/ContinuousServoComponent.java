package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.CRServo;

/**
 * ContinuousServoComponent.java
 *
 * STUDENT GUIDE: Continuous Rotation Servo Abstraction & Control
 *
 * PURPOSE:
 * This class wraps the FTC SDK's CRServo class (Continuous Rotation Servo)
 * to control motors that spin continuously, like wheels, spinners, conveyors.
 *
 * SERVO TYPES:
 * 1. Positional Servo (PostionServoComponent):
 *    - Moves to a specific angle (0° to 180°)
 *    - You set a position (0.0 to 1.0)
 *    - Example: arm, grabber, turret
 *
 * 2. Continuous Rotation Servo (THIS CLASS):
 *    - Spins continuously in one direction
 *    - You set speed and direction (-1.0 to 1.0)
 *    - Example: wheels, spinners, intakes, conveyors
 *
 * KEY DIFFERENCE:
 * - Positional servo: "Go to 90 degrees" (position-based)
 * - Continuous servo: "Spin at 50% speed, forward" (velocity-based)
 *
 * FTC SDK CONCEPTS:
 * - CRServo: Continuous Rotation Servo motor
 * - Power: Speed and direction (-1.0 to 1.0)
 *   * -1.0 = full reverse (counter-clockwise) at max speed
 *   * 0.0 = stopped
 *   * 1.0 = full forward (clockwise) at max speed
 * - Clamping: Ensuring power stays in valid range
 */

public class ContinuousServoComponent {

    // The actual FTC SDK continuous servo object
    // Sends velocity/power commands to the Control Hub
    private CRServo servo;

    // Track current power we set (-1.0 to 1.0)
    // 0 = stopped, positive = clockwise, negative = counter-clockwise
    private double currentPower = 0;

    /**
     * Constructor - Initialize the continuous servo component
     *
     * STUDENT: Pass in the continuous servo from HardwareConfig.
     * The constructor initializes it to 0 (stopped).
     *
     * @param servo The FTC CRServo object (from HardwareConfig)
     *
     * FTC SDK DETAIL:
     * servo.setPower(0) tells the servo to stop.
     * This is the safe default - nothing spinning when we start.
     *
     * EXAMPLE USAGE:
     * CRServo rawServo = hardware.servoContinuous;  // From HardwareConfig
     * ContinuousServoComponent spinner = new ContinuousServoComponent(rawServo);
     * // Servo is now stopped (power = 0)
     */
    public ContinuousServoComponent(CRServo servo) {
        this.servo = servo;

        // FTC SDK: setPower(double power) sets velocity for continuous servo
        // - Takes a value from -1.0 to 1.0
        // - Negative = counter-clockwise (or reverse)
        // - 0.0 = stopped
        // - Positive = clockwise (or forward)
        // This is DIFFERENT from positional servos!
        // With continuous servos, you set velocity (speed), not position (angle)
        servo.setPower(0);  // Stop initially (safe state)
    }

    /**
     * Set continuous servo power (speed and direction)
     *
     * STUDENT: Main method to control the servo's spinning speed and direction.
     *
     * @param power Value from -1.0 (reverse) to 1.0 (forward)
     *               Intermediate values like 0.5 = half speed
     *
     * POWER INTERPRETATION:
     * Power directly translates to servo velocity (unlike positional servos).
     * - -1.0 = maximum speed counter-clockwise
     * - -0.5 = half speed counter-clockwise
     * - 0.0 = stopped
     * - 0.5 = half speed clockwise
     * - 1.0 = maximum speed clockwise
     *
     * FTC SDK DETAIL:
     * CRServo.setPower() is optimized for continuous velocity control.
     * Unlike positional servos (which move to an angle), continuous servos
     * just keep spinning at the velocity you set.
     *
     * CLAMPING EXPLANATION:
     * Power values MUST be in range [-1.0, 1.0].
     * If you pass 1.5 or -2.0, the servo can't handle it.
     * So we "clamp" the value to keep it valid.
     *
     * STUDENT LEARNING:
     * This is DEFENSIVE PROGRAMMING:
     * - Never trust that input values are valid
     * - Always validate/constrain them
     * - Prevents runtime errors and unpredictable behavior
     */
    public void setPower(double power) {

        // CLAMPING: Ensure power stays in valid range [-1.0, 1.0]
        // This is identical to DCMotorComponent's validation
        // Math.max(a, b) returns the larger value
        // Math.min(a, b) returns the smaller value
        // Together they constrain the value to [-1.0, 1.0]
        //
        // EXAMPLE WALKTHROUGH for power = 1.5:
        // Step 1: Math.min(1.0, 1.5) = 1.0 (1.0 is smaller than 1.5)
        // Step 2: Math.max(-1.0, 1.0) = 1.0 (1.0 is bigger than -1.0)
        // Result: power = 1.0 ✓ (clamped to valid range)
        //
        // EXAMPLE for power = -2.0:
        // Step 1: Math.min(1.0, -2.0) = -2.0 (-2.0 is smaller than 1.0)
        // Step 2: Math.max(-1.0, -2.0) = -1.0 (-1.0 is bigger than -2.0)
        // Result: power = -1.0 ✓ (clamped to valid range)
        power = Math.max(-1.0, Math.min(1.0, power));

        // FTC SDK: Actually send the command to the servo
        // This is the line that talks to the Control Hub and physical servo
        // The servo will now spin at the specified power/velocity
        servo.setPower(power);

        // Store the value for state tracking
        // This way, if student code calls getPower(), we can return what we actually set
        // Useful for telemetry: telemetry.addData("Spinner Power", spinner.getPower());
        currentPower = power;
    }

    /**
     * Get current servo power
     *
     * STUDENT: Returns the power we last set (useful for telemetry or debugging)
     *
     * @return The power value we last set (-1.0 to 1.0)
     *
     * FTC SDK DETAIL:
     * We return our tracked 'currentPower' value, not servo.getPower().
     * Why? Because we control it and know what we set.
     * The servo's internal sensors might have slight delays or inaccuracies,
     * so our tracked value is more reliable for immediate queries.
     */
    public double getPower() {
        return currentPower;
    }

    /**
     * Stop the servo
     *
     * STUDENT: Convenience method to immediately stop the servo spinning.
     *
     * FTC SDK DETAIL:
     * Calling setPower(0) tells the servo to stop (no rotation).
     * The servo will coast to a stop or apply braking (depends on servo type).
     *
     * STUDENT LEARNING:
     * This is a CONVENIENCE METHOD:
     * - Does something common in a cleaner way
     * - Makes code more readable: servo.stop() vs servo.setPower(0)
     * - Prevents typos or mistakes
     * - If you later wanted to add braking logic, you'd do it here
     *
     * EXAMPLE:
     * Instead of: continuousServo.setPower(0);
     * Write:      continuousServo.stop();
     * Much clearer what you're trying to do!
     */
    public void stop() {
        // Calls setPower(0), which goes through our validation and tracking
        // This is better than calling servo.setPower(0) directly
        setPower(0);
    }
}
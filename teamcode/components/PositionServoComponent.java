package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.Servo;

/**
 * PositionServoComponent.java
 *
 * STUDENT GUIDE: Positional Servo Abstraction & Control
 *
 * PURPOSE:
 * This class wraps the FTC SDK's Servo class to provide simple control
 * over a positional servo (used for grabbers, gates, turrets, etc.)
 *
 * This servo is NOT continuous rotation - it has a limited range of motion.
 * Use ContinuousServoComponent for continuous-rotation servos.
 *
 * NOT EVERY SERVO IS 180 DEGREES:
 * "180-degree servo" is a common phrase, but it's not universal! Different
 * servo models have different real physical ranges - some hobby/FTC servos
 * (like goBILDA's Speed Servo) actually rotate closer to 300°. This class
 * defaults to 180° but lets you specify the real range for your hardware.
 * If your angle telemetry doesn't match what the servo actually does,
 * this is the first thing to check.
 *
 * FTC SDK CONCEPTS:
 * - Servo position: Represented as 0.0 to 1.0 (not degrees!)
 *   * 0.0 = one extreme (0°)
 *   * 0.5 = middle (half of the full range)
 *   * 1.0 = other extreme (the full range, e.g. 180° or 300°)
 * - Angle conversion: position * maxAngleDegrees = angle in degrees
 * - Incremental control: Move in small steps (0.05 = 5% steps)
 */

public class PositionServoComponent {

    // The actual FTC SDK servo object
    // Sends position commands to the Control Hub
    private Servo servo;

    // Track current position (0.0 to 1.0)
    // 0.5 is middle, useful for telemetry and resetting
    private double currentPosition = 0.5;

    // The servo's real physical range of motion, in degrees.
    // Defaults to 180 (the common case) but is overridable in the
    // constructor for servos with a different actual range (e.g. 300°).
    private final double maxAngleDegrees;

    // How much to move per button press (5% of total range)
    // This gives smooth, controlled movement
    // STUDENT NOTE: You could change this to 0.01 for finer control or 0.1 for coarser
    private final double INCREMENT = 0.05;

    // Minimum and maximum position values
    // These MUST be 0.0 and 1.0 for a standard servo
    // (Some advanced servos might have different ranges, but standard ones don't)
    private final double MIN_POSITION = 0.0;
    private final double MAX_POSITION = 1.0;

    /**
     * Constructor - Initialize the servo component
     *
     * STUDENT: When you create this, pass in the servo from HardwareConfig.
     * The constructor sets the servo to its middle (neutral) position.
     *
     * @param servo The FTC Servo object (from HardwareConfig)
     *
     * FTC SDK DETAIL:
     * servo.setPosition(0.5) tells the servo to move to the 0.5 position (90°/middle).
     * This is a safe default - not at an extreme where it might be damaged or interfere.
     *
     * EXAMPLE USAGE:
     * Servo rawServo = hardware.servoPosition;  // From HardwareConfig
     * PositionServoComponent grabber = new PositionServoComponent(rawServo);
     * // Servo is now at 0.5 (middle position), assumed 180° range
     */
    public PositionServoComponent(Servo servo) {
        this(servo, 180.0);
    }

    /**
     * Constructor - Initialize the servo component with a custom range
     *
     * STUDENT: Use this constructor when your servo's real physical range
     * ISN'T 180° - for example, some servos (like goBILDA's Speed Servo)
     * actually rotate about 300°. Only getAngleDegrees() is affected by
     * this - position(), incrementPosition(), etc. all still work in the
     * normalized 0.0-1.0 range regardless.
     *
     * @param servo The FTC Servo object (from HardwareConfig)
     * @param maxAngleDegrees The servo's real full-range rotation, in
     *                        degrees (check your servo's datasheet)
     *
     * EXAMPLE USAGE:
     * Servo rawServo = hardware.servoPosition;
     * // This servo actually rotates 300°, not the usual 180°
     * PositionServoComponent grabber = new PositionServoComponent(rawServo, 300.0);
     */
    public PositionServoComponent(Servo servo, double maxAngleDegrees) {
        this.servo = servo;
        this.maxAngleDegrees = maxAngleDegrees;

        // FTC SDK: setPosition(double position) moves servo to 0.0-1.0 position
        // - Takes a value from 0.0 to 1.0
        // - 0.0 = fully counterclockwise
        // - 0.5 = middle
        // - 1.0 = fully clockwise
        // This command sends the actual pulse to the servo via the Control Hub
        servo.setPosition(0.5);  // Start at middle
    }

    /**
     * Increase servo position (move towards 1.0/maxAngleDegrees)
     *
     * STUDENT: Call this to move the servo one step forward.
     * Incremental movement gives finer control than jumping to endpoints.
     *
     * FTC SDK DETAIL:
     * This demonstrates BOUNDARY CHECKING:
     * - We add INCREMENT to the position
     * - We check if it exceeds MAX_POSITION
     * - If it does, we cap it at MAX_POSITION
     * This prevents accidentally sending invalid values to the servo.
     *
     * STUDENT LEARNING:
     * This is safer than just doing currentPosition += INCREMENT without bounds checking.
     * If the servo gets a position > 1.0, it will ignore it or behave unpredictably.
     */
    public void incrementPosition() {
        // Add one increment step
        currentPosition += INCREMENT;

        // Check if we've exceeded maximum
        // If so, cap it at the maximum value
        // This prevents the servo from getting an invalid position > 1.0
        if (currentPosition > MAX_POSITION) {
            currentPosition = MAX_POSITION;  // Stop at the end
        }

        // FTC SDK: Send the new position to the servo
        // This causes the physical servo to move
        servo.setPosition(currentPosition);
    }

    /**
     * Decrease servo position (move towards 0.0/0°)
     *
     * STUDENT: Call this to move the servo one step backward.
     *
     * FTC SDK DETAIL:
     * Same boundary checking as incrementPosition(), but in the other direction.
     * We check if currentPosition drops below MIN_POSITION and cap it.
     */
    public void decrementPosition() {
        // Subtract one increment step
        currentPosition -= INCREMENT;

        // Check if we've gone below minimum
        // If so, cap it at the minimum value
        // This prevents the servo from getting an invalid position < 0.0
        if (currentPosition < MIN_POSITION) {
            currentPosition = MIN_POSITION;  // Stop at the start
        }

        // FTC SDK: Send the new position to the servo
        // This causes the physical servo to move
        servo.setPosition(currentPosition);
    }

    /**
     * Jump directly to a specific position
     *
     * STUDENT: Unlike incrementPosition()/decrementPosition() (which nudge
     * by one small step), this jumps straight to any position you want.
     * Useful when some OTHER piece of code has already calculated exactly
     * where the servo should go (e.g. "red detected → go to 0.0").
     *
     * @param targetPosition Desired position (0.0 to 1.0) - values outside
     *                        this range are clamped to the nearest valid one
     *
     * FTC SDK DETAIL:
     * Same boundary-checking idea as incrementPosition()/decrementPosition(),
     * just clamping the CALLER's value instead of an internal += step.
     *
     * EXAMPLE:
     * // Move to a computed position based on some sensor reading
     * servo.setPosition(0.75);
     */
    public void setPosition(double targetPosition) {
        // Clamp to the valid 0.0-1.0 range, same idea as increment/decrement
        currentPosition = Math.max(MIN_POSITION, Math.min(MAX_POSITION, targetPosition));

        // FTC SDK: Send the new position to the servo
        servo.setPosition(currentPosition);
    }

    /**
     * Get current servo position
     *
     * STUDENT: Returns the position in normalized form (0.0 to 1.0)
     * Useful for telemetry or for branching logic based on servo position
     *
     * @return Current position (0.0 = one extreme, 0.5 = middle, 1.0 = other extreme)
     *
     * FTC SDK DETAIL:
     * We return our tracked 'currentPosition' value.
     * This is faster and more reliable than calling servo.getPosition()
     * (which might have sensor delays or inaccuracies).
     */
    public double getPosition() {
        return currentPosition;
    }

    /**
     * Get servo angle in degrees
     *
     * STUDENT: Converts from normalized position (0-1) to human-readable
     * degrees, using THIS servo's real range (see maxAngleDegrees).
     * More intuitive for understanding where the servo actually is.
     *
     * @return Angle in degrees (0° to maxAngleDegrees)
     *
     * MATH EXPLANATION:
     * Servo position is a ratio (0.0 to 1.0).
     * Servo angle is 0° to maxAngleDegrees (180° by default, but not always -
     * see the constructor overload if your servo's range is different).
     * To convert: position * maxAngleDegrees = angle
     *
     * EXAMPLES (for a 180° servo):
     * - position = 0.0  →  angle = 0.0 * 180 = 0° (fully CCW)
     * - position = 0.5  →  angle = 0.5 * 180 = 90° (middle)
     * - position = 1.0  →  angle = 1.0 * 180 = 180° (fully CW)
     * - position = 0.25 →  angle = 0.25 * 180 = 45° (quarter way)
     *
     * STUDENT LEARNING:
     * This is a common pattern: UNIT CONVERSION
     * - FTC SDK uses 0-1, but students think in degrees
     * - This method bridges those two representations
     * - Same concept as converting Celsius to Fahrenheit: different units, same thing
     * - Getting the conversion FACTOR right matters just as much as the
     *   conversion itself - a wrong maxAngleDegrees gives confidently wrong
     *   numbers, which are worse than no numbers at all!
     */
    public double getAngleDegrees() {
        return currentPosition * maxAngleDegrees;
    }

    /**
     * Reset servo to middle position (0.5 / half of maxAngleDegrees)
     *
     * STUDENT: Convenience method to quickly return to neutral position.
     * Useful at the start of the program or when an action completes.
     *
     * FTC SDK DETAIL:
     * We both update our tracked position AND send the command to the servo.
     * This ensures both our internal state and the physical servo are in sync.
     *
     * WHY NOT JUST CALL incrementPosition() MULTIPLE TIMES?
     * - Slower (multiple servo commands)
     * - Doesn't guarantee exact 0.5 (might land at 0.45 or 0.55 depending on position)
     * - This direct approach is faster and more reliable
     */
    public void reset() {
        // Update our tracked state
        currentPosition = 0.5;

        // FTC SDK: Send the command to the servo
        // The servo will immediately move to position 0.5 (its middle)
        servo.setPosition(0.5);
    }
}
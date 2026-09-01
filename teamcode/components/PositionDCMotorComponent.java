package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * PositionDCMotorComponent.java
 *
 * STUDENT GUIDE: Position-Based DC Motor Control (Closed-Loop)
 *
 * PURPOSE:
 * Controls a DC motor with encoder feedback to reach a specific position.
 * Used for: lifts, arms, slides - anything that needs to move to an exact location.
 *
 * KEY CHARACTERISTICS:
 * - Requires motor with built-in encoder (most FTC motors have this)
 * - Targets a specific position (in encoder ticks)
 * - Uses RUN_TO_POSITION mode for closed-loop control
 * - Automatically adjusts power to reach target
 * - Can hold position after reaching it
 *
 * DIFFERENCE FROM ContinuousDCMotorComponent:
 * This component: "Rotate to tick 500" (position-based with feedback)
 * Continuous motor: "Run at 50% speed forward" (velocity-based, no target)
 *
 * HOW IT WORKS:
 * 1. You set target position in ticks
 * 2. Motor automatically moves toward that position
 * 3. Encoder provides feedback on current position
 * 4. Motor stops when target is reached
 * 5. Can hold position with configured power
 *
 * FTC SDK CONCEPTS:
 * - Encoder: Tracks motor shaft rotation (ticks/counts)
 * - RUN_TO_POSITION: Mode where motor targets a position
 * - RUN_WITHOUT_ENCODER: Basic mode (like ContinuousDCMotor)
 * - getCurrentPosition(): Returns current encoder reading
 * - setTargetPosition(): Sets the target position
 */

public class PositionDCMotorComponent {

    // The FTC SDK motor object with encoder
    private DcMotor motor;

    // Track target position for queries
    // Since we use motor.setDirection() for reversal (not manual negation),
    // this always matches exactly what the caller passed to moveToPosition()
    private int targetPosition = 0;

    // Current position (cached for faster access than querying motor)
    private int currentPosition = 0;

    // Power to use when moving (affects speed and acceleration)
    private double movePower = 0.5;  // 50% power by default

    // Power to hold position after reaching it
    private double holdPower = 0.1;  // 10% power to hold

    // Optional: Track if position was reached for state machines
    private boolean hasReachedTarget = false;

    // Tolerance: how close to target is "close enough" (in ticks)
    // Accounts for encoder noise and mechanical play
    private int positionTolerance = 10;  // ±10 ticks

    // Optional: reverse motor direction
    private boolean isReversed = false;

    /**
     * Constructor - Initialize position motor
     *
     * @param motor The FTC DcMotor object with encoder from HardwareConfig
     *
     * EXAMPLE:
     * DcMotor liftMotor = hardware.dcMotor;
     * PositionDCMotorComponent lift = new PositionDCMotorComponent(liftMotor);
     *
     * FTC SDK DETAIL:
     * The motor MUST have an encoder for this to work.
     * Most FTC motors include integrated encoders.
     * If motor doesn't have encoder, use ContinuousDCMotorComponent instead.
     */
    public PositionDCMotorComponent(DcMotor motor) {
        this.motor = motor;
        initializeMotor();
    }

    /**
     * Initialize motor for position control
     *
     * PRIVATE: Called by constructor.
     * Sets up the motor for RUN_TO_POSITION mode.
     *
     * FTC SDK DETAIL:
     * - resetEncoders(): Clear the current encoder count to 0
     * - setMode(RUN_TO_POSITION): Enable closed-loop position control
     *   With this mode, the motor will automatically move to target position
     *   and apply proportional power based on error (how far from target)
     * - setMode(RUN_WITHOUT_ENCODER): Would disable position control
     *
     * IMPORTANT SDK GOTCHA:
     * The SDK throws TargetPositionNotSetException if you switch to
     * RUN_TO_POSITION mode before ever calling setTargetPosition().
     * That's why we set a target (0) BEFORE switching modes below.
     */
    private void initializeMotor() {
        // Reset encoder count to zero
        // FTC SDK: resetEncoders() clears the current position to 0
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // Set a target position BEFORE entering RUN_TO_POSITION mode.
        // Skipping this throws TargetPositionNotSetException - the SDK
        // requires a target to already exist whenever RUN_TO_POSITION is set.
        motor.setTargetPosition(0);

        // Set motor to position control mode
        // FTC SDK: RUN_TO_POSITION mode:
        // - Motor automatically moves toward target
        // - Uses proportional control (power ∝ error)
        // - Motor stops when target is reached
        // This is the KEY difference - the SDK does the work!
        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // Set initial power (speed for movements)
        motor.setPower(movePower);
    }

    /**
     * Move motor to target position
     *
     * STUDENT: Main control method. Set where you want the motor to go.
     *
     * @param position Target position in encoder ticks
     *
     * BEHAVIOR:
     * - Positive position: motor rotates one direction
     * - Negative position: motor rotates opposite direction
     * - 0: Returns to starting position
     *
     * FTC SDK DETAIL:
     * motor.setTargetPosition(position) tells the motor where to go.
     * With RUN_TO_POSITION mode enabled, the motor will automatically
     * move toward this position and stop when reached.
     *
     * The motor doesn't start moving until you call setPower() with
     * a non-zero value.
     *
     * STUDENT LEARNING:
     * This is CLOSED-LOOP control:
     * - You set a target
     * - Motor automatically adjusts power to reach it
     * - Feedback (encoder) tells motor when target is reached
     * - More predictable and reliable than open-loop
     *
     * EXAMPLE:
     * // Move lift up 500 ticks
     * lift.moveToPosition(500);
     *
     * // Move back to start
     * lift.moveToPosition(0);
     */
    public void moveToPosition(int position) {
        // No manual reversal needed here - motor.setDirection() (set in
        // setReversed()) makes the SDK flip the sign internally for both
        // setTargetPosition() and getCurrentPosition(), so this stays in
        // the caller's original frame the whole way through.

        // Store target for queries
        targetPosition = position;

        // Tell motor where to go
        // FTC SDK: setTargetPosition() sets the goal for RUN_TO_POSITION mode
        motor.setTargetPosition(position);

        // Always (re)apply movePower for this move. We can't just check for
        // "motor.getPower() == 0" here - if the motor was previously holding
        // at holdPower (see hasReachedTarget()), power is already non-zero
        // but at the wrong (lower) level, so a plain zero-check would leave
        // us moving at hold speed instead of move speed.
        motor.setPower(movePower);

        // Reset reached flag - we're moving again
        hasReachedTarget = false;
    }

    /**
     * Get current motor position
     *
     * STUDENT: Returns how far the motor has rotated (in encoder ticks).
     *
     * @return Current position in encoder ticks
     *
     * FTC SDK DETAIL:
     * motor.getCurrentPosition() queries the encoder.
     * Returns the current rotation count since motor was initialized.
     *
     * STUDENT LEARNING:
     * - Positive position: rotations in one direction
     * - Negative position: rotations in opposite direction
     * - 0: Starting position (where motor was when initialized)
     */
    public int getCurrentPosition() {
        // Query motor's encoder
        currentPosition = motor.getCurrentPosition();
        return currentPosition;
    }

    /**
     * Get target position we're moving toward
     *
     * @return Target position in encoder ticks, exactly as passed to moveToPosition()
     */
    public int getTargetPosition() {
        return targetPosition;
    }

    /**
     * Check if motor has reached its target position
     *
     * STUDENT: Useful for state machines and sequencing actions.
     * Waits until position is "close enough" to target.
     *
     * @return true if motor is within tolerance of target
     *
     * STUDENT LEARNING:
     * We use a tolerance instead of exact match because:
     * - Encoders have noise (±1-2 ticks of jitter)
     * - Motor might oscillate around target
     * - We don't need EXACT position, just "good enough"
     *
     * EXAMPLE:
     * while (!lift.hasReachedTarget()) {
     *     telemetry.addData("Position", lift.getCurrentPosition());
     *     telemetry.update();
     * }
     * // Now lift is at target position
     *
     * FTC SDK DETAIL:
     * The first time this detects we're within tolerance, we drop the motor
     * power down to holdPower (see setHoldPower()). This is what actually
     * makes "holding position" happen - RUN_TO_POSITION mode will keep
     * pushing at whatever power was last set, so we lower it here instead
     * of leaving the motor straining at full movePower after arriving.
     */
    public boolean hasReachedTarget() {
        int currentPos = getCurrentPosition();
        int error = Math.abs(currentPos - targetPosition);

        boolean justReached = !hasReachedTarget && (error <= positionTolerance);

        // Check if we're within tolerance of target
        hasReachedTarget = (error <= positionTolerance);

        // Switch down to holding power the moment we arrive, so gravity-loaded
        // mechanisms (like lifts) don't sag once movement stops
        if (justReached) {
            motor.setPower(holdPower);
        }

        return hasReachedTarget;
    }

    /**
     * Get error from target
     *
     * STUDENT: How far away are we from the target?
     * Useful for debugging position control.
     *
     * @return Difference from current position to target (in ticks)
     *
     * EXAMPLE:
     * telemetry.addData("Position Error", lift.getPositionError());
     */
    public int getPositionError() {
        return Math.abs(getCurrentPosition() - targetPosition);
    }

    /**
     * Set movement power (speed)
     *
     * STUDENT: How fast should the motor move?
     * Higher power = faster movement but less control.
     *
     * @param power Power for movements (0.0 to 1.0)
     *
     * STUDENT LEARNING:
     * - 0.3 = slow, smooth, controlled movement
     * - 0.5 = medium speed
     * - 0.8-1.0 = fast movement (harder to control)
     *
     * Most FTC teams use 0.6-0.8 for good balance of speed and control.
     *
     * EXAMPLE:
     * // Move slowly for precision
     * lift.setMovePower(0.3);
     * lift.moveToPosition(500);
     */
    public void setMovePower(double power) {
        movePower = Math.max(0, Math.min(1.0, power));
        motor.setPower(movePower);
    }

    /**
     * Set hold power (power to maintain position)
     *
     * STUDENT: Some mechanisms (like lifts) sag when power is removed.
     * Hold power keeps them in place.
     *
     * @param power Power to hold position (typically 0.0-0.3)
     *
     * STUDENT LEARNING:
     * - 0.0 = coast to stop (motor can sag down)
     * - 0.1 = light holding power
     * - 0.2+ = strong holding power (uses battery, creates heat)
     *
     * Most FTC gravity-based mechanisms use 0.1-0.2 hold power.
     *
     * NOTE: This only sets the value - it takes effect the next time
     * hasReachedTarget() detects that the motor just arrived at its target.
     * Call hasReachedTarget() from your control loop for holding to kick in.
     *
     * EXAMPLE:
     * // Stop lift at position but hold against gravity
     * lift.setHoldPower(0.15);
     * lift.moveToPosition(500);
     */
    public void setHoldPower(double power) {
        holdPower = Math.max(0, Math.min(1.0, power));
    }

    /**
     * Set position tolerance (how close is "close enough")
     *
     * STUDENT: Controls precision of position reaching.
     *
     * @param ticks Tolerance in encoder ticks (±value)
     *
     * STUDENT LEARNING:
     * - 5 ticks = very precise (might oscillate)
     * - 10 ticks = good balance
     * - 20+ ticks = loose (faster, but less accurate)
     *
     * EXAMPLE:
     * // Lift needs to be very precise for game piece placement
     * lift.setPositionTolerance(5);
     *
     * // Drive train doesn't need precision
     * drive.setPositionTolerance(20);
     */
    public void setPositionTolerance(int ticks) {
        positionTolerance = Math.max(0, ticks);
    }

    /**
     * Stop motor immediately
     *
     * STUDENT: Convenience method to stop the motor.
     */
    public void stop() {
        motor.setPower(0);
    }

    /**
     * Check if motor is busy moving to target
     *
     * STUDENT: Is the motor currently moving?
     *
     * @return true if motor is still moving toward target
     *
     * FTC SDK DETAIL:
     * motor.isBusy() returns true while RUN_TO_POSITION mode is active
     * and target hasn't been reached yet.
     *
     * EXAMPLE:
     * while (lift.isBusy()) {
     *     telemetry.addData("Moving to", lift.getTargetPosition());
     *     telemetry.update();
     * }
     * // Motor has reached target
     */
    public boolean isBusy() {
        return motor.isBusy();
    }

    /**
     * Reset encoder to zero
     *
     * STUDENT: Clear the position counter.
     * Useful at program start or when recalibrating.
     *
     * CAUTION: This stops the motor during reset!
     * Don't call this while motor is in motion.
     */
    public void resetEncoder() {
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // Same SDK gotcha as initializeMotor(): a target must be set BEFORE
        // switching back to RUN_TO_POSITION, or the SDK throws
        // TargetPositionNotSetException.
        motor.setTargetPosition(0);
        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        currentPosition = 0;
        targetPosition = 0;
    }

    /**
     * Set motor reversal
     *
     * @param reversed true to reverse direction
     *
     * FTC SDK DETAIL:
     * Updates motor.setDirection(), so the SDK flips the sign internally
     * for setTargetPosition()/getCurrentPosition() from here on - no
     * manual negation needed elsewhere in this class.
     */
    public void setReversed(boolean reversed) {
        this.isReversed = reversed;
        motor.setDirection(reversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
    }

    /**
     * Check if motor is reversed
     *
     * @return true if motor is reversed
     */
    public boolean isReversed() {
        return isReversed;
    }
}
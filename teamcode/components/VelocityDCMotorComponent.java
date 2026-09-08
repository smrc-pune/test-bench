package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * VelocityDCMotorComponent.java
 *
 * STUDENT GUIDE: Closed-Loop DC Motor Velocity Control (DcMotorEx)
 *
 * PURPOSE:
 * Controls a DC motor's SPEED (encoder ticks per second) instead of its
 * power. Used for: flywheels/shooters, intakes, or anything that needs to
 * spin at a consistent RPM regardless of battery voltage or how hard the
 * mechanism is being loaded.
 *
 * DIFFERENCE FROM ContinuousDCMotorComponent AND PositionDCMotorComponent:
 * This component:      "Spin at 1500 ticks/sec, no matter what" (closed-loop SPEED)
 * ContinuousDCMotor:    "Run at 50% power forward" (open-loop, no feedback at all)
 * PositionDCMotor:      "Rotate to tick 500 and stop" (closed-loop POSITION)
 *
 * HOW IT WORKS:
 * 1. You set a target speed, in encoder ticks/second
 * 2. The motor's encoder measures its actual speed continuously
 * 3. The SDK's own PID loop compares actual vs. target every cycle and
 *    adjusts the real applied power on its own to close the gap
 * 4. You never touch power directly - only the target speed
 *
 * FTC SDK CONCEPTS:
 * - DcMotorEx: An extended motor interface with velocity read/write
 *   (getVelocity()/setVelocity()) that plain DcMotor doesn't expose
 * - RUN_USING_ENCODER: The run mode that makes setVelocity() closed-loop
 * - getVelocity(): Reads the motor's CURRENT measured speed, straight from
 *   the encoder
 */

public class VelocityDCMotorComponent {

    // The FTC SDK's extended motor object - needed for velocity control
    private DcMotorEx motor;

    // Track what we last asked for, for telemetry
    private double targetVelocity = 0;

    private boolean isReversed = false;

    /**
     * Constructor - Initialize the motor for velocity control
     *
     * @param motor The FTC DcMotorEx object from HardwareConfig
     *
     * FTC SDK DETAIL:
     * Sets RUN_USING_ENCODER mode, which is what makes setVelocity() below
     * actually closed-loop. Without this mode, setVelocity() has no effect.
     *
     * EXAMPLE:
     * DcMotorEx flywheelMotor = hardwareMap.get(DcMotorEx.class, HardwareNames.DC_MOTOR);
     * VelocityDCMotorComponent flywheel = new VelocityDCMotorComponent(flywheelMotor);
     */
    public VelocityDCMotorComponent(DcMotorEx motor) {
        this.motor = motor;
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    /**
     * Command a target speed
     *
     * STUDENT: Main control method. Call this every loop with whatever
     * speed you want right now.
     *
     * @param ticksPerSecond Target speed in encoder ticks/second. Positive
     *                        and negative both work, same as setPower().
     *
     * FTC SDK DETAIL:
     * motor.setVelocity() hands the target to the SDK's own closed-loop
     * controller. From here, the SDK compares getVelocity() against your
     * target every control cycle and adjusts the actual applied power on
     * its own - that's the entire benefit over setPower().
     *
     * STUDENT LEARNING:
     * This is CLOSED-LOOP control, same idea as PositionDCMotorComponent,
     * but the thing being controlled is SPEED instead of POSITION. You can
     * verify it's actually working by watching getVelocity() converge on
     * whatever you passed in here, even while getPower() keeps changing on
     * its own to make that happen.
     *
     * EXAMPLE:
     * // Spin the flywheel at 1500 ticks/sec, whatever it takes
     * flywheel.setTargetVelocity(1500);
     */
    public void setTargetVelocity(double ticksPerSecond) {
        targetVelocity = ticksPerSecond;
        motor.setVelocity(ticksPerSecond);
    }

    /**
     * Get the CURRENT measured speed, straight from the encoder
     *
     * @return Current speed in encoder ticks/second (signed, same
     *         direction convention as setTargetVelocity())
     *
     * FTC SDK DETAIL:
     * motor.getVelocity() differentiates encoder position over time. It's
     * available on DcMotorEx specifically - plain DcMotor has no equivalent.
     */
    public double getVelocity() {
        return motor.getVelocity();
    }

    /**
     * Get the power the motor is actually applying right now
     *
     * STUDENT: Watch this move on its own even though YOU never changed it
     * directly - that's the SDK's PID working to hold your target speed.
     *
     * @return Current applied power (-1.0 to 1.0)
     */
    public double getPower() {
        return motor.getPower();
    }

    /**
     * Get the velocity we last asked for
     *
     * @return Target ticks/second, exactly as passed to setTargetVelocity()
     */
    public double getTargetVelocity() {
        return targetVelocity;
    }

    /**
     * Stop the motor immediately
     *
     * STUDENT: Sets a target velocity of 0 - the SDK will actively brake
     * toward a stop, the same closed-loop way it chases any other target.
     */
    public void stop() {
        setTargetVelocity(0);
    }

    /**
     * Set motor reversal
     *
     * @param reversed true to reverse direction
     *
     * FTC SDK DETAIL:
     * Updates motor.setDirection(), so the SDK flips the sign internally
     * for setVelocity()/getVelocity() from here on - no manual negation
     * needed elsewhere in this class.
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

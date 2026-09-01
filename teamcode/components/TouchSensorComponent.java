package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.TouchSensor;

/**
 * TouchSensorComponent.java
 *
 * STUDENT GUIDE: Touch Sensor Abstraction & Event Counting
 *
 * PURPOSE:
 * This class wraps the FTC SDK's TouchSensor class to detect physical contact.
 * Useful for detecting when a robot arm hits a limit, when an object is grabbed, etc.
 *
 * SENSOR BEHAVIOR:
 * - Returns true when pressed (something touching the sensor)
 * - Returns false when not pressed (no contact)
 * - Like a switch: ON/OFF
 *
 * TYPICAL USES:
 * - Limit switches: Detect when arm reaches end of travel
 * - Grabber detection: Detect when gripper is holding something
 * - Collision detection: Stop motors if something is stuck
 *
 * FTC SDK CONCEPTS:
 * - TouchSensor: Binary input (true or false)
 * - isPressed(): Returns true/false for current state
 * - Unlike sensors that return analog values (color, distance),
 *   touch sensors just answer "is it pressed?"
 */

public class TouchSensorComponent {

    // The actual FTC SDK touch sensor object
    // Reads the physical touch sensor connected to Control Hub
    private TouchSensor sensor;

    // Counter for how many times the sensor has been pressed
    // Useful for state machines or event tracking
    // Example: "Stop after gripper touches something 3 times"
    private int pressCount = 0;

    /**
     * Constructor - Initialize the touch sensor component
     *
     * STUDENT: Pass in the touch sensor from HardwareConfig.
     *
     * @param sensor The FTC TouchSensor object (from HardwareConfig)
     *
     * EXAMPLE USAGE:
     * TouchSensor rawSensor = hardware.touchSensor;  // From HardwareConfig
     * TouchSensorComponent limit = new TouchSensorComponent(rawSensor);
     * // Now you can use: limit.isPressed(), limit.getPressCount(), etc.
     */
    public TouchSensorComponent(TouchSensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Check if touch sensor is currently pressed
     *
     * STUDENT: This is the main method - use it to detect contact.
     *
     * @return true if sensor is pressed (something touching it)
     *         false if sensor is not pressed (no contact)
     *
     * FTC SDK DETAIL:
     * sensor.isPressed() queries the current state of the physical sensor.
     * The Control Hub reads the sensor's electrical signal and reports it to your code.
     *
     * COMMON USAGE PATTERNS:
     *
     * // Simple if statement:
     * if (limitSwitch.isPressed()) {
     *     // Stop the arm when it hits the limit
     *     arm.stop();
     * }
     *
     * // Waiting for something:
     * while (!gripper.isPressed()) {
     *     // Keep closing the gripper until it presses the sensor
     *     gripper.close();
     * }
     *
     * // Detecting release:
     * if (wasPressed && !limitSwitch.isPressed()) {
     *     // Sensor just released - do something
     * }
     *
     * STUDENT LEARNING:
     * Note: isPressed() tells you the CURRENT state, not a change.
     * If the sensor is held down, isPressed() returns true every loop cycle.
     * To detect a NEW press (vs just being held), you need to track the previous state
     * (like GamepadConfig does with button states).
     */
    public boolean isPressed() {
        // FTC SDK: Query the current sensor state
        // This sends a request to the Control Hub to read the sensor
        // Returns true if pressed, false if not pressed
        return sensor.isPressed();
    }

    /**
     * Get the number of times this sensor has been pressed
     *
     * STUDENT: Useful for counting events or implementing state machines.
     *
     * @return Total press count (starts at 0, incremented by incrementPressCount())
     *
     * EXAMPLE USAGE:
     * // Stop after object has been grabbed 3 times
     * if (graspCounter.getPressCount() >= 3) {
     *     gripper.stop();
     * }
     *
     * STUDENT LEARNING:
     * This is state tracking - your code remembers information between loop cycles.
     * The counter doesn't automatically increment when the sensor is pressed.
     * You must manually call incrementPressCount() when you detect a press.
     * See the note in incrementPressCount() below.
     */
    public int getPressCount() {
        return pressCount;
    }

    /**
     * Increment press count (manually call when sensor is pressed)
     *
     * STUDENT: You must MANUALLY call this to count presses.
     * It doesn't automatically increment when isPressed() returns true!
     *
     * WHY MANUAL?
     * If we auto-incremented every loop cycle while the sensor is pressed,
     * the count would go way too high (100+ times per second!).
     * Manual control lets YOU decide when to count:
     * - Count a press only once (when transitioning from released to pressed)
     * - Count only certain types of presses
     * - Debounce the sensor (ignore quick noise/bounces)
     *
     * EXAMPLE: Count presses with debouncing
     *
     * boolean prevPressed = false;
     *
     * while (opModeIsActive()) {
     *     boolean currentPressed = limitSwitch.isPressed();
     *
     *     // Detect transition from not-pressed to pressed
     *     if (currentPressed && !prevPressed) {
     *         // NEW press detected - increment count
     *         limitSwitch.incrementPressCount();
     *     }
     *
     *     prevPressed = currentPressed;
     * }
     *
     * This counts ONLY when the sensor transitions from false→true,
     * preventing multiple counts from a single physical press.
     *
     * STUDENT LEARNING:
     * This is EDGE DETECTION - detecting state transitions, not just state.
     * Same concept as GamepadConfig tracking button press vs hold.
     */
    public void incrementPressCount() {
        pressCount++;
    }

    /**
     * Reset press count back to zero
     *
     * STUDENT: Call this to clear the count.
     * Useful at the start of the program or when restarting a routine.
     *
     * EXAMPLE USAGE:
     * // In autonomous, move arm to position, reset counter
     * arm.moveToPosition();
     * limitSwitch.resetPressCount();  // Reset counter for teleop
     *
     * // In teleop, count presses
     * if (limitSwitch.isPressed()) {
     *     limitSwitch.incrementPressCount();
     * }
     *
     * STUDENT LEARNING:
     * This is a simple way to clear state.
     * Simple as it is, it's important - forgetting to reset can cause bugs!
     */
    public void resetPressCount() {
        pressCount = 0;
    }
}
package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.DistanceSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * DistanceSensorComponent.java
 *
 * STUDENT GUIDE: Distance Sensor Abstraction & Range Analysis
 *
 * PURPOSE:
 * This class wraps the FTC SDK's DistanceSensor class to measure how far away
 * an object is. Used for detecting walls, game pieces, parking distance, etc.
 *
 * SENSOR BASICS:
 * - Returns distance as a decimal number (e.g. 12.5)
 * - You choose the UNIT the distance is reported in (CM, MM, INCH)
 * - Smaller values = object is closer
 * - Larger values = object is farther away (or nothing detected)
 *
 * SENSOR RANGE:
 * Most FTC distance sensors (like the REV 2M Distance Sensor) only work reliably
 * within a limited range:
 * - Too close: readings can be inaccurate or clipped
 * - Too far: sensor may return a very large "out of range" value
 * - Best accuracy is usually in the sensor's specified sweet spot (check datasheet)
 *
 * DISTANCE UNITS:
 * FTC SDK gives you these built-in DistanceUnit options:
 * - DistanceUnit.CM    (centimeters)
 * - DistanceUnit.MM    (millimeters)
 * - DistanceUnit.INCH  (inches)
 * - DistanceUnit.METER (meters)
 *
 * FTC SDK CONCEPTS:
 * - DistanceSensor: Returns analog distance values (not binary like touch sensor)
 * - getDistance(DistanceUnit): Returns distance in requested unit
 * - Requires clear line-of-sight to the object being measured
 */

public class DistanceSensorComponent {

    // The actual FTC SDK distance sensor object
    // Reads how far away the nearest object is
    private DistanceSensor sensor;

    /**
     * Constructor - Initialize the distance sensor component
     *
     * STUDENT: Pass in the distance sensor from HardwareConfig.
     *
     * @param sensor The FTC DistanceSensor object (from HardwareConfig)
     *
     * EXAMPLE USAGE:
     * DistanceSensor rawSensor = hardware.distanceSensor;  // From HardwareConfig
     * DistanceSensorComponent ranger = new DistanceSensorComponent(rawSensor);
     * // Now you can use: ranger.getDistanceCM(), ranger.isWithinRange(), etc.
     *
     * NOTE: Distance sensors are picky about surfaces!
     * - Dark or shiny/reflective surfaces can give inaccurate readings
     * - Keep the sensor lens clean
     * - Test with your actual game pieces/field elements, not just a hand or wall
     */
    public DistanceSensorComponent(DistanceSensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Get distance in centimeters
     *
     * STUDENT: Returns how far away the nearest object is, in centimeters.
     *
     * @return Distance in centimeters (smaller = closer, larger = farther)
     *
     * FTC SDK DETAIL:
     * sensor.getDistance(DistanceUnit.CM) queries the sensor and converts
     * the raw reading into centimeters for you.
     *
     * EXAMPLE:
     * if (ranger.getDistanceCM() < 10) {
     *     // Object is very close - within 10cm
     * }
     */
    public double getDistanceCM() {
        // FTC SDK: Query distance in centimeters
        return sensor.getDistance(DistanceUnit.CM);
    }

    /**
     * Get distance in millimeters
     *
     * STUDENT: Returns how far away the nearest object is, in millimeters.
     * Useful when you need finer precision than centimeters.
     *
     * @return Distance in millimeters (smaller = closer, larger = farther)
     *
     * FTC SDK DETAIL:
     * sensor.getDistance(DistanceUnit.MM) queries the sensor and converts
     * the raw reading into millimeters for you.
     */
    public double getDistanceMM() {
        // FTC SDK: Query distance in millimeters
        return sensor.getDistance(DistanceUnit.MM);
    }

    /**
     * Get distance in inches
     *
     * STUDENT: Returns how far away the nearest object is, in inches.
     *
     * @return Distance in inches (smaller = closer, larger = farther)
     *
     * FTC SDK DETAIL:
     * sensor.getDistance(DistanceUnit.INCH) queries the sensor and converts
     * the raw reading into inches for you.
     *
     * EXAMPLE:
     * if (ranger.getDistanceInches() < 4) {
     *     // Object is very close - within 4 inches
     * }
     */
    public double getDistanceInches() {
        // FTC SDK: Query distance in inches
        return sensor.getDistance(DistanceUnit.INCH);
    }

    /**
     * Check if an object is within a given distance
     *
     * STUDENT: Convenience method - compares the current distance to a threshold.
     *
     * @param thresholdCM Distance threshold in centimeters
     * @return true if the nearest object is closer than thresholdCM, false otherwise
     *
     * HOW IT WORKS:
     * This method just calls getDistanceCM() and compares it to your threshold.
     * It exists so you don't have to write the same comparison everywhere.
     *
     * EXAMPLE:
     * if (ranger.isWithinRange(15)) {
     *     // Something is within 15cm - stop driving forward
     *     drive.stop();
     * }
     *
     * STUDENT LEARNING:
     * Watch out for sensor noise! A single reading can be wrong.
     * For important decisions (like stopping a robot), consider checking
     * multiple readings in a row before trusting the result.
     */
    public boolean isWithinRange(double thresholdCM) {
        return getDistanceCM() < thresholdCM;
    }
}

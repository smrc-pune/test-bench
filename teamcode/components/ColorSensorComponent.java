package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.ColorSensor;

/**
 * ColorSensorComponent.java
 *
 * STUDENT GUIDE: Color Sensor Abstraction & Analysis
 *
 * PURPOSE:
 * This class wraps the FTC SDK's ColorSensor class to read RGB color values
 * and detect colors. Used for detecting game pieces, sorting objects, etc.
 *
 * SENSOR BASICS:
 * - Returns Red (R), Green (G), Blue (B) values
 * - Each value ranges from 0-255
 * - Higher values = more of that color
 * - By analyzing the mix of R, G, B, you can determine what color something is
 *
 * RGB COLOR THEORY:
 * Red = {255, 0, 0}      (maximum red, no green, no blue)
 * Green = {0, 255, 0}    (no red, maximum green, no blue)
 * Blue = {0, 0, 255}     (no red, no green, maximum blue)
 * Yellow = {255, 255, 0} (red + green = yellow)
 * White = {255, 255, 255} (all colors mixed)
 * Black = {0, 0, 0}      (no light detected)
 *
 * ALPHA:
 * Alpha (brightness/intensity) tells you if the sensor can even see anything
 * - Low alpha = dark environment or object far from sensor
 * - High alpha = bright environment or object close to sensor
 * - Useful for detecting if something is actually there
 *
 * FTC SDK CONCEPTS:
 * - ColorSensor: Can read RGB and Alpha values
 * - Returns analog values (not binary like touch sensor)
 * - Requires good lighting and proper distance for accuracy
 */

public class ColorSensorComponent {

    // The actual FTC SDK color sensor object
    // Reads light reflected from the object being sensed
    private ColorSensor sensor;

    /**
     * Constructor - Initialize the color sensor component
     *
     * STUDENT: Pass in the color sensor from HardwareConfig.
     *
     * @param sensor The FTC ColorSensor object (from HardwareConfig)
     *
     * EXAMPLE USAGE:
     * ColorSensor rawSensor = hardware.colorSensor;  // From HardwareConfig
     * ColorSensorComponent detector = new ColorSensorComponent(rawSensor);
     * // Now you can use: detector.getRed(), detector.detectColor(), etc.
     *
     * NOTE: Color sensors are sensitive!
     * - Clean the sensor lens regularly
     * - Make sure lighting is consistent
     * - Test calibration in your actual competition environment
     */
    public ColorSensorComponent(ColorSensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Get red value (0-255)
     *
     * STUDENT: Returns how much RED the sensor detects.
     * Higher values = more red in the object.
     *
     * @return Red channel value (0 = no red, 255 = maximum red)
     *
     * FTC SDK DETAIL:
     * sensor.red() queries the red channel of the RGB sensor.
     * The Control Hub reads the sensor's output and reports it.
     *
     * EXAMPLE:
     * if (color.getRed() > 200) {
     *     // Detected something very red
     * }
     */
    public int getRed() {
        // FTC SDK: Query red channel value
        return sensor.red();
    }

    /**
     * Get green value (0-255)
     *
     * STUDENT: Returns how much GREEN the sensor detects.
     * Higher values = more green in the object.
     *
     * @return Green channel value (0 = no green, 255 = maximum green)
     *
     * FTC SDK DETAIL:
     * sensor.green() queries the green channel of the RGB sensor.
     *
     * EXAMPLE:
     * if (color.getGreen() > 150 && color.getRed() < 100) {
     *     // Detected something greenish (more green than red)
     * }
     */
    public int getGreen() {
        // FTC SDK: Query green channel value
        return sensor.green();
    }

    /**
     * Get blue value (0-255)
     *
     * STUDENT: Returns how much BLUE the sensor detects.
     * Higher values = more blue in the object.
     *
     * @return Blue channel value (0 = no blue, 255 = maximum blue)
     *
     * FTC SDK DETAIL:
     * sensor.blue() queries the blue channel of the RGB sensor.
     */
    public int getBlue() {
        // FTC SDK: Query blue channel value
        return sensor.blue();
    }

    /**
     * Get alpha value (0-255)
     *
     * STUDENT: Returns the sensor's brightness/intensity reading.
     * This is NOT another color channel - it's brightness.
     *
     * @return Alpha (brightness) value (0 = dark, 255 = very bright)
     *
     * FTC SDK DETAIL:
     * sensor.alpha() queries the overall brightness.
     * This is useful for:
     * - Detecting if the sensor is even seeing something
     * - Adjusting detection thresholds based on lighting
     * - Detecting if something is blocking the sensor
     *
     * STUDENT LEARNING:
     * Alpha is often more reliable than individual RGB values!
     * If your color detection is failing, check alpha first:
     * - Low alpha = bad lighting or object too far away
     * - Adjust your sensor position or test environment
     *
     * EXAMPLE:
     * if (color.getAlpha() < 50) {
     *     // Not seeing anything - sensor blocked or dark environment
     * }
     */
    public int getAlpha() {
        // FTC SDK: Query alpha (brightness) value
        return sensor.alpha();
    }

    /**
     * Detect color by analyzing RGB values
     *
     * STUDENT: Compares RGB values to determine what color the object is.
     *
     * @return Color name as string: "RED", "GREEN", "BLUE", "YELLOW", or "UNKNOWN"
     *
     * HOW IT WORKS:
     * This method uses simple comparison logic:
     * 1. Find which color channel has the highest value
     * 2. If red is highest → "RED"
     * 3. If green is highest → "GREEN"
     * 4. If blue is highest → "BLUE"
     * 5. Special case: If red AND green are high but blue is low → "YELLOW"
     * 6. Tie-break: If red and green are TIED for highest and both beat
     *    blue, that's still a red+green mix → "YELLOW"
     * 7. Otherwise → "UNKNOWN"
     *
     * LIMITATIONS:
     * This is a SIMPLE algorithm. Real-world color detection is complex!
     * - Lighting changes affect readings
     * - Mixed colors (orange, purple, etc.) return "UNKNOWN"
     * - Sensor must be very close for accurate readings
     *
     * FOR BETTER COLOR DETECTION:
     * - Students could implement more complex algorithms
     * - Could use machine learning if available
     * - Could calibrate thresholds for specific lighting conditions
     *
     * FTC SDK DETAIL:
     * This method uses multiple sensor.red(), sensor.green(), sensor.blue() calls.
     * Each one queries the actual hardware, so this is slightly inefficient
     * (could cache the values), but it keeps the code simple.
     *
     * STUDENT LEARNING:
     * This shows conditional logic - making decisions based on sensor input.
     * Real robotics involves LOTS of conditional logic like this!
     */
    public String detectColor() {
        // Read the three color values
        int red = getRed();
        int green = getGreen();
        int blue = getBlue();

        // Find dominant color - which channel has the highest value?
        // Use simple > comparison to find the maximum

        // If red is higher than both green and blue
        if (red > green && red > blue) {
            return "RED";
        }
        // If green is higher than both red and blue
        else if (green > red && green > blue) {
            return "GREEN";
        }
        // If blue is higher than both red and green
        else if (blue > red && blue > green) {
            return "BLUE";
        }
        // Special case: Yellow is RED + GREEN (low blue)
        // If both red and green are high, but blue is low
        else if (red > 100 && green > 100 && blue < 100) {
            return "YELLOW";
        }
        // Couldn't determine the color clearly
        else {
            return "UNKNOWN";
        }
    }
}

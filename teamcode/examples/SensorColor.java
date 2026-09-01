package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;

import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.ColorSensorComponent;

/**
 * SensorColor.java
 *
 * STUDENT GUIDE: Reading a Color Sensor
 *
 * PURPOSE:
 * Minimal example showing how to read RGB/Alpha values from a color sensor
 * and detect a color name. This OpMode doesn't drive anything - it just
 * demonstrates the sensor by itself.
 *
 * HARDWARE SETUP:
 * Configure one Color Sensor in the Driver Station's hardware config,
 * named to match HardwareNames.COLOR_SENSOR
 *
 * NOTE: This test bench's color sensor (REV Color Sensor V3) has its LED
 * physically hardwired always-on - it can't be controlled in software at
 * all, so this example just reads the sensor - no gamepad controls needed.
 *
 * FTC SDK CONCEPTS:
 * - hardwareMap.get(): Looks up a configured hardware device by name
 * - LinearOpMode: Runs step-by-step, top to bottom, inside a while loop
 * - telemetry.addData()/update(): Sends text to the Driver Station screen
 */
@TeleOp(name = "Basic: Color", group = "Sensor")
public class SensorColor extends LinearOpMode {

    @Override
    public void runOpMode() {
        // FTC SDK: Look up the color sensor from the robot's hardware config
        ColorSensor rawSensor = hardwareMap.get(ColorSensor.class, HardwareNames.COLOR_SENSOR);
        ColorSensorComponent colorSensor = new ColorSensorComponent(rawSensor);

        telemetry.addLine("Ready! Press START to begin reading the color sensor.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== TELEMETRY =====
            telemetry.addLine("===== COLOR SENSOR =====");
            telemetry.addData("Detected Color", colorSensor.detectColor());
            telemetry.addData("Red", colorSensor.getRed());
            telemetry.addData("Green", colorSensor.getGreen());
            telemetry.addData("Blue", colorSensor.getBlue());
            telemetry.addData("Alpha", colorSensor.getAlpha());
            telemetry.update();
        }
    }
}

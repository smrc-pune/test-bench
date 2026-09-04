package org.firstinspires.ftc.teamcode.examples;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.teamcode.GamepadConfig;
import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.TouchSensorComponent;

/**
 * SensorTouch.java
 *
 * STUDENT GUIDE: Reading a Touch Sensor
 *
 * PURPOSE:
 * Minimal example showing how to read a touch sensor and display its state
 * on telemetry. This OpMode doesn't drive anything - it just demonstrates
 * the sensor by itself.
 *
 * HARDWARE SETUP:
 * Configure one Touch Sensor in the Driver Station's hardware config,
 * named to match HardwareNames.TOUCH_SENSOR
 *
 * GAMEPAD CONTROLS:
 * - RB (right bumper): Reset the press counter back to 0
 *
 * FTC SDK CONCEPTS:
 * - hardwareMap.get(): Looks up a configured hardware device by name
 * - LinearOpMode: Runs step-by-step, top to bottom, inside a while loop
 * - telemetry.addData()/update(): Sends text to the Driver Station screen
 *
 * FTC DASHBOARD:
 * This OpMode's telemetry also streams to FTC Dashboard
 * (http://192.168.43.1:8080/dash while connected to the Control Hub's
 * WiFi), which graphs it live instead of just showing static text - handy
 * for watching Press Count tick up over time.
 */
@TeleOp(name = "Basic: Touch", group = "Sensor")
public class SensorTouch extends LinearOpMode {

    @Override
    public void runOpMode() {
        // FTC SDK: Look up the touch sensor from the robot's hardware config
        TouchSensor rawSensor = hardwareMap.get(TouchSensor.class, HardwareNames.TOUCH_SENSOR);
        TouchSensorComponent touchSensor = new TouchSensorComponent(rawSensor);

        // Wrap gamepad1 so we can read button presses
        GamepadConfig controls = new GamepadConfig(gamepad1);

        // Track previous press state so we only count NEW presses (not held ones)
        boolean prevPressed = false;

        // FTC Dashboard: send every telemetry.addData()/addLine()/update()
        // call below to BOTH the Driver Station and the Dashboard browser
        // page, instead of just the Driver Station.
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        telemetry.addLine("Ready! Press START to begin reading the touch sensor.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== READ GAMEPAD INPUT =====
            if (controls.isRightBumperPressed()) {
                touchSensor.resetPressCount();
            }

            // ===== UPDATE SENSOR STATE =====
            boolean currentlyPressed = touchSensor.isPressed();

            // STUDENT: Only count NEW presses (transition from not-pressed to
            // pressed) - see TouchSensorComponent.incrementPressCount() for why.
            if (currentlyPressed && !prevPressed) {
                touchSensor.incrementPressCount();
            }
            prevPressed = currentlyPressed;

            // ===== TELEMETRY =====
            telemetry.addLine("===== TOUCH SENSOR =====");
            telemetry.addData("Currently Pressed", currentlyPressed ? "YES" : "NO");
            telemetry.addData("Press Count", touchSensor.getPressCount());
            telemetry.addLine();
            telemetry.addLine("RB: Reset press count");
            telemetry.update();
        }
    }
}

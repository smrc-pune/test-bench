package org.firstinspires.ftc.teamcode.examples;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.teamcode.GamepadConfig;
import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.ContinuousDCMotorComponent;
import org.firstinspires.ftc.teamcode.components.TouchSensorComponent;

/**
 * ConceptTouchStopsMotor.java
 *
 * STUDENT GUIDE: Sensor as a Safety Limit Switch
 *
 * PURPOSE:
 * Shows a SENSOR overriding an ACTUATOR - the classic "limit switch" safety
 * pattern used all over real robots. The stick controls the motor like
 * BasicDcMotor does, but the touch sensor can force it to stop no matter
 * what the stick says.
 *
 * WHY THIS MATTERS:
 * Real FTC arms/slides use touch sensors (or limit switches) exactly like
 * this: mounted at the end of travel, so if the mechanism drives too far,
 * the sensor gets pressed and cuts power BEFORE something breaks. The
 * motor doesn't know or care why it stopped - the sensor just wins.
 *
 * HARDWARE SETUP:
 * Configure one DC Motor and one Touch Sensor in the Driver Station's
 * hardware config, named to match HardwareNames.DC_MOTOR and
 * HardwareNames.TOUCH_SENSOR
 *
 * GAMEPAD CONTROLS:
 * - Left Stick Y: Variable speed forward/reverse (ignored while the touch
 *   sensor is pressed)
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
 * for watching Applied Power drop to zero the instant the sensor is pressed.
 */
@TeleOp(name = "Concept: Touch Stops Motor", group = "Concept")
public class ConceptTouchStopsMotor extends LinearOpMode {

    @Override
    public void runOpMode() {
        // FTC SDK: Look up the motor and touch sensor from the hardware config
        DcMotor rawMotor = hardwareMap.get(DcMotor.class, HardwareNames.DC_MOTOR);
        ContinuousDCMotorComponent motor = new ContinuousDCMotorComponent(rawMotor);

        TouchSensor rawTouch = hardwareMap.get(TouchSensor.class, HardwareNames.TOUCH_SENSOR);
        TouchSensorComponent touchSensor = new TouchSensorComponent(rawTouch);

        // Wrap gamepad1 so we can read stick input
        GamepadConfig controls = new GamepadConfig(gamepad1);

        // Track previous press state so we only count NEW presses (not held ones)
        boolean prevPressed = false;

        // FTC Dashboard: send every telemetry.addData()/addLine()/update()
        // call below to BOTH the Driver Station and the Dashboard browser
        // page, instead of just the Driver Station.
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        telemetry.addLine("Ready! Press START to begin. Touch sensor overrides the stick.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== READ GAMEPAD INPUT =====
            double requestedPower = controls.getLeftStickY();

            // ===== SAFETY OVERRIDE =====
            // STUDENT: This is the whole lesson - check the sensor BEFORE
            // trusting driver input. The sensor's "stop" always wins,
            // regardless of what the stick is asking for.
            boolean currentlyPressed = touchSensor.isPressed();
            double appliedPower = currentlyPressed ? 0.0 : requestedPower;
            motor.setPower(appliedPower);

            // Only count NEW presses (transition from not-pressed to pressed) -
            // otherwise holding the sensor down would count hundreds of times
            // per second instead of once. See TouchSensorComponent for why.
            if (currentlyPressed && !prevPressed) {
                touchSensor.incrementPressCount();
            }
            prevPressed = currentlyPressed;

            // ===== TELEMETRY =====
            telemetry.addLine("===== TOUCH STOPS MOTOR =====");
            telemetry.addData("Touch Sensor", currentlyPressed ? "PRESSED (blocking)" : "clear");
            telemetry.addData("Requested Power (stick)", "%.2f", requestedPower);
            telemetry.addData("Applied Power (actual)", "%.2f", appliedPower);
            telemetry.addData("Times Blocked", touchSensor.getPressCount());
            telemetry.addLine();
            telemetry.addLine("Left Stick Y: drive motor | Touch sensor: safety stop");
            telemetry.update();
        }

        motor.stop();
    }
}

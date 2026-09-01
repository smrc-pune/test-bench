package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;

import org.firstinspires.ftc.teamcode.GamepadConfig;
import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.ContinuousServoComponent;

/**
 * BasicServoContinuous.java
 *
 * STUDENT GUIDE: Continuous-Rotation Servo Control
 *
 * PURPOSE:
 * Minimal example showing how to spin a continuous-rotation servo with the
 * gamepad, using direct stick-to-speed control (same idea as BasicDcMotor,
 * for a servo instead of a motor).
 *
 * HARDWARE SETUP:
 * Configure one Continuous Rotation Servo in the Driver Station's hardware
 * config, named to match HardwareNames.SERVO_CONTINUOUS
 *
 * GAMEPAD CONTROLS:
 * - Right Stick Y: Variable speed forward/reverse
 *
 * FTC SDK CONCEPTS:
 * - hardwareMap.get(): Looks up a configured hardware device by name
 * - LinearOpMode: Runs step-by-step, top to bottom, inside a while loop
 * - telemetry.addData()/update(): Sends text to the Driver Station screen
 */
@TeleOp(name = "Basic: Servo Continuous", group = "Basic")
public class BasicServoContinuous extends LinearOpMode {

    @Override
    public void runOpMode() {
        // FTC SDK: Look up the continuous servo from the robot's hardware config
        CRServo rawServo = hardwareMap.get(CRServo.class, HardwareNames.SERVO_CONTINUOUS);
        ContinuousServoComponent servo = new ContinuousServoComponent(rawServo);

        // Wrap gamepad1 so we can read stick input
        GamepadConfig controls = new GamepadConfig(gamepad1);

        telemetry.addLine("Ready! Press START to begin spinning the servo.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== READ GAMEPAD INPUT =====
            servo.setPower(controls.getRightStickY());

            // ===== TELEMETRY =====
            telemetry.addLine("===== CONTINUOUS SERVO =====");
            telemetry.addData("Power", "%.2f", servo.getPower());
            telemetry.addLine();
            telemetry.addLine("Right Stick Y: variable power (forward/reverse)");
            telemetry.update();
        }

        servo.stop();
    }
}

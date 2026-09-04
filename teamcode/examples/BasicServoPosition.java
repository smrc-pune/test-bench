package org.firstinspires.ftc.teamcode.examples;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.GamepadConfig;
import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.PositionServoComponent;

/**
 * BasicServoPosition.java
 *
 * STUDENT GUIDE: Positional Servo Control
 *
 * PURPOSE:
 * Minimal example showing how to step a positional servo up/down with the
 * gamepad, one increment per button press.
 *
 * HARDWARE SETUP:
 * Configure one Servo in the Driver Station's hardware config,
 * named to match HardwareNames.SERVO_POSITION
 *
 * NOTE: This particular servo's real range is ~300°, not the "standard"
 * 180° - see HardwareNames.SERVO_POSITION_MAX_DEGREES and
 * PositionServoComponent's two-argument constructor.
 *
 * GAMEPAD CONTROLS:
 * - Dpad Up: Move one increment toward maximum
 * - Dpad Down: Move one increment toward minimum
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
 * for watching Position/Angle step up and down as you press Dpad Up/Down.
 */
@TeleOp(name = "Basic: Servo Position", group = "Basic")
public class BasicServoPosition extends LinearOpMode {

    @Override
    public void runOpMode() {
        // FTC SDK: Look up the servo from the robot's hardware config
        Servo rawServo = hardwareMap.get(Servo.class, HardwareNames.SERVO_POSITION);
        PositionServoComponent servo = new PositionServoComponent(
                rawServo, HardwareNames.SERVO_POSITION_MAX_DEGREES);

        // Wrap gamepad1 so we can read button presses
        GamepadConfig controls = new GamepadConfig(gamepad1);

        // FTC Dashboard: send every telemetry.addData()/addLine()/update()
        // call below to BOTH the Driver Station and the Dashboard browser
        // page, instead of just the Driver Station.
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        telemetry.addLine("Ready! Press START to begin moving the servo.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== READ GAMEPAD INPUT =====
            // STUDENT: Dpad Up/Down are edge-detected, so each press moves
            // exactly one increment - holding the button does NOT keep moving it.
            if (controls.isDpadUpPressed()) {
                servo.incrementPosition();
            }
            if (controls.isDpadDownPressed()) {
                servo.decrementPosition();
            }

            // ===== TELEMETRY =====
            telemetry.addLine("===== SERVO (POSITION) =====");
            telemetry.addData("Position", "%.2f (0-1)", servo.getPosition());
            telemetry.addData("Angle", "%.1f°", servo.getAngleDegrees());
            telemetry.addLine();
            telemetry.addLine("Dpad Up: increment | Dpad Down: decrement");
            telemetry.update();
        }
    }
}

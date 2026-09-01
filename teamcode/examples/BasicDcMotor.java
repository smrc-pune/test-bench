package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.GamepadConfig;
import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.ContinuousDCMotorComponent;

/**
 * BasicDcMotor.java
 *
 * STUDENT GUIDE: Open-Loop DC Motor Control
 *
 * PURPOSE:
 * Minimal example showing how to drive a DC motor with the gamepad, using
 * simple open-loop (no encoder feedback, no target position) velocity
 * control - the stick directly sets how fast and which direction it spins.
 *
 * HARDWARE SETUP:
 * Configure one DC Motor in the Driver Station's hardware config,
 * named to match HardwareNames.DC_MOTOR
 *
 * GAMEPAD CONTROLS:
 * - Left Stick Y: Variable speed forward/reverse
 *
 * FTC SDK CONCEPTS:
 * - hardwareMap.get(): Looks up a configured hardware device by name
 * - LinearOpMode: Runs step-by-step, top to bottom, inside a while loop
 * - telemetry.addData()/update(): Sends text to the Driver Station screen
 */
@TeleOp(name = "Basic: DC Motor", group = "Basic")
public class BasicDcMotor extends LinearOpMode {

    @Override
    public void runOpMode() {
        // FTC SDK: Look up the motor from the robot's hardware config
        DcMotor rawMotor = hardwareMap.get(DcMotor.class, HardwareNames.DC_MOTOR);
        ContinuousDCMotorComponent motor = new ContinuousDCMotorComponent(rawMotor);

        // Wrap gamepad1 so we can read stick input
        GamepadConfig controls = new GamepadConfig(gamepad1);

        telemetry.addLine("Ready! Press START to begin driving the motor.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== READ GAMEPAD INPUT =====
            motor.setPower(controls.getLeftStickY());

            // ===== TELEMETRY =====
            telemetry.addLine("===== DC MOTOR =====");
            telemetry.addData("Power", "%.2f", motor.getPower());
            telemetry.addLine();
            telemetry.addLine("Left Stick Y: variable power (forward/reverse)");
            telemetry.update();
        }

        motor.stop();
    }
}

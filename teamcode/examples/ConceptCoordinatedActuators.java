package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.GamepadConfig;
import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.PositionDCMotorComponent;
import org.firstinspires.ftc.teamcode.components.PositionServoComponent;

/**
 * ConceptCoordinatedActuators.java
 *
 * STUDENT GUIDE: Coordinating Multiple Actuators
 *
 * PURPOSE:
 * Shows ONE button controlling MULTIPLE actuators together, driving them
 * toward a matched pair of positions - like a real mechanism where an arm
 * and a wrist need to move together to "stow" or "deploy" cleanly. No
 * sensors involved this time - just one trigger commanding a coordinated
 * pair of motions.
 *
 * WHY THIS MATTERS:
 * Real robots rarely move ONE thing at a time - a scoring mechanism might
 * need an arm, a wrist, AND a claw to all reach specific positions before
 * it's actually ready to score. This is the simplest version of that idea:
 * two actuators, two named presets, one button each.
 *
 * HARDWARE SETUP:
 * Configure one DC Motor (with encoder) and one Servo in the Driver
 * Station's hardware config, named to match HardwareNames.DC_MOTOR and
 * HardwareNames.SERVO_POSITION
 *
 * GAMEPAD CONTROLS:
 * - A: Move both actuators to the "Stow" preset
 * - B: Move both actuators to the "Deploy" preset
 *
 * FTC SDK CONCEPTS:
 * - hardwareMap.get(): Looks up a configured hardware device by name
 * - LinearOpMode: Runs step-by-step, top to bottom, inside a while loop
 * - telemetry.addData()/update(): Sends text to the Driver Station screen
 */
@TeleOp(name = "Concept: Coordinated Actuators", group = "Concept")
public class ConceptCoordinatedActuators extends LinearOpMode {

    // "Stow" preset - motor and servo both pulled in/back
    private static final int STOW_MOTOR_TICKS = 0;
    private static final double STOW_SERVO_POSITION = 0.0;

    // "Deploy" preset - motor and servo both pushed out/forward
    private static final int DEPLOY_MOTOR_TICKS = 500;
    private static final double DEPLOY_SERVO_POSITION = 1.0;

    @Override
    public void runOpMode() {
        // FTC SDK: Look up the motor and servo from the hardware config
        DcMotor rawMotor = hardwareMap.get(DcMotor.class, HardwareNames.DC_MOTOR);
        PositionDCMotorComponent motor = new PositionDCMotorComponent(rawMotor);

        Servo rawServo = hardwareMap.get(Servo.class, HardwareNames.SERVO_POSITION);
        PositionServoComponent servo = new PositionServoComponent(
                rawServo, HardwareNames.SERVO_POSITION_MAX_DEGREES);

        // Wrap gamepad1 so we can read button presses
        GamepadConfig controls = new GamepadConfig(gamepad1);

        // Track which preset was last requested, just for telemetry
        String currentPreset = "NONE (moved manually or not yet commanded)";

        telemetry.addLine("Ready! Press START, then A (Stow) or B (Deploy).");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== READ GAMEPAD INPUT =====
            // STUDENT: This is the whole lesson - ONE button press below
            // commands BOTH actuators at once, toward a matched pair of
            // positions. Neither actuator "knows" about the other; the
            // OpMode is just the one place coordinating both.
            if (controls.isAPressed()) {
                motor.moveToPosition(STOW_MOTOR_TICKS);
                servo.setPosition(STOW_SERVO_POSITION);
                currentPreset = "STOW";
            }
            if (controls.isBPressed()) {
                motor.moveToPosition(DEPLOY_MOTOR_TICKS);
                servo.setPosition(DEPLOY_SERVO_POSITION);
                currentPreset = "DEPLOY";
            }

            // ===== TELEMETRY =====
            telemetry.addLine("===== COORDINATED ACTUATORS =====");
            telemetry.addData("Current Preset", currentPreset);
            telemetry.addLine();
            telemetry.addLine("--- Motor ---");
            telemetry.addData("Position", motor.getCurrentPosition());
            telemetry.addData("Target", motor.getTargetPosition());
            telemetry.addData("At Target", motor.hasReachedTarget() ? "YES" : "NO");
            telemetry.addLine();
            telemetry.addLine("--- Servo ---");
            telemetry.addData("Position", "%.2f (0-1)", servo.getPosition());
            telemetry.addData("Angle", "%.1f°", servo.getAngleDegrees());
            telemetry.addLine();
            telemetry.addLine("A: Stow both | B: Deploy both");
            telemetry.update();
        }

        motor.stop();
    }
}

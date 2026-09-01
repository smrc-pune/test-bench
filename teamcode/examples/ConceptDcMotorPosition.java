package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.GamepadConfig;
import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.PositionDCMotorComponent;

/**
 * ConceptDcMotorPosition.java
 *
 * STUDENT GUIDE: Closed-Loop DC Motor Position Control
 *
 * PURPOSE:
 * Minimal example showing how to command a DC motor to a specific encoder
 * position (closed-loop control) instead of just spinning at a set speed
 * like BasicDcMotor does. Dial in a target position with the bumpers, then
 * press A to actually send the motor there.
 *
 * WHY BUILD THE TARGET UP SEPARATELY INSTEAD OF MOVING IMMEDIATELY?
 * This separates "choosing where to go" from "actually going" - the
 * bumpers only change a LOCAL number (pendingTarget), they never touch the
 * motor. Nothing physically moves until you press A. This mirrors a common
 * real pattern: let the driver dial in a setpoint, then commit to it.
 *
 * HARDWARE SETUP:
 * Configure one DC Motor (with encoder) in the Driver Station's hardware
 * config, named to match HardwareNames.DC_MOTOR
 *
 * GAMEPAD CONTROLS:
 * - Right Bumper: Increase pending target position by 100 ticks
 * - Left Bumper: Decrease pending target position by 100 ticks
 * - A: Move the motor to the pending target position
 *
 * FTC SDK CONCEPTS:
 * - hardwareMap.get(): Looks up a configured hardware device by name
 * - LinearOpMode: Runs step-by-step, top to bottom, inside a while loop
 * - telemetry.addData()/update(): Sends text to the Driver Station screen
 */
@TeleOp(name = "Concept: DC Motor Position", group = "Concept")
public class ConceptDcMotorPosition extends LinearOpMode {

    // How far each bumper press moves the pending target, in encoder ticks
    private static final int POSITION_STEP = 100;

    @Override
    public void runOpMode() {
        // FTC SDK: Look up the motor from the robot's hardware config
        DcMotor rawMotor = hardwareMap.get(DcMotor.class, HardwareNames.DC_MOTOR);
        PositionDCMotorComponent motor = new PositionDCMotorComponent(rawMotor);

        // Wrap gamepad1 so we can read button presses
        GamepadConfig controls = new GamepadConfig(gamepad1);

        // STUDENT: This is a LOCAL number the bumpers adjust - it has
        // nothing to do with the motor until A actually sends it over.
        int pendingTarget = 0;

        telemetry.addLine("Ready! Press START, then use the bumpers + A to move the motor.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== READ GAMEPAD INPUT =====
            if (controls.isRightBumperPressed()) {
                pendingTarget += POSITION_STEP;
            }
            if (controls.isLeftBumperPressed()) {
                pendingTarget -= POSITION_STEP;
            }
            if (controls.isAPressed()) {
                motor.moveToPosition(pendingTarget);
            }

            // ===== TELEMETRY =====
            // STUDENT: hasReachedTarget() does double duty here - it also
            // switches the motor to its lower "hold power" once it arrives.
            // See PositionDCMotorComponent for details.
            telemetry.addLine("===== DC MOTOR (POSITION) =====");
            telemetry.addData("Pending Target (not sent yet)", pendingTarget);
            telemetry.addData("Motor's Actual Target", motor.getTargetPosition());
            telemetry.addData("Current Position", motor.getCurrentPosition());
            telemetry.addData("Error", motor.getPositionError());
            telemetry.addData("At Target", motor.hasReachedTarget() ? "YES" : "NO");
            telemetry.addData("Busy", motor.isBusy() ? "YES" : "NO");
            telemetry.addLine();
            telemetry.addLine("RB: +100 | LB: -100 | A: Move to pending target");
            telemetry.update();
        }

        motor.stop();
    }
}

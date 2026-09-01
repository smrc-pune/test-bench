package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.GamepadConfig;

/**
 * BasicGamepad.java
 *
 * STUDENT GUIDE: Reading the Gamepad
 *
 * PURPOSE:
 * The safest possible first example - NO hardware devices at all, nothing
 * on the robot moves. This just reads every stick and button on the
 * controller and displays it, so you can see how GamepadConfig behaves
 * before wiring it up to anything physical.
 *
 * HARDWARE SETUP:
 * None. This OpMode doesn't touch hardwareMap at all.
 *
 * GAMEPAD CONTROLS:
 * - Left Stick X/Y, Right Stick X/Y: shown as live numbers
 * - A, B, X, Y, Dpad Up/Down/Left/Right, both bumpers: shown as press counts
 *
 * WHY PRESS COUNTS, NOT JUST ON/OFF?
 * GamepadConfig's button methods are EDGE-DETECTED - each one is only true
 * for a single loop cycle per physical press (see GamepadConfig's class
 * doc). The loop runs many times per second, so a raw true/false display
 * would flicker "true" for a fraction of a second and be almost impossible
 * to actually see. Counting presses instead gives you something stable to
 * look at, while still proving the edge-detection is working correctly -
 * tap a button 3 times, watch its count go up by exactly 3.
 *
 * FTC SDK CONCEPTS:
 * - LinearOpMode: Runs step-by-step, top to bottom, inside a while loop
 * - telemetry.addData()/update(): Sends text to the Driver Station screen
 */
@TeleOp(name = "Basic: Gamepad", group = "Basic")
public class BasicGamepad extends LinearOpMode {

    @Override
    public void runOpMode() {
        // Wrap gamepad1 so we can read stick/button input
        GamepadConfig controls = new GamepadConfig(gamepad1);

        // One press-count tracker per button
        int countA = 0;
        int countB = 0;
        int countX = 0;
        int countY = 0;
        int countDpadUp = 0;
        int countDpadDown = 0;
        int countDpadLeft = 0;
        int countDpadRight = 0;
        int countLeftBumper = 0;
        int countRightBumper = 0;

        telemetry.addLine("Ready! Press START, then try the sticks and buttons.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== READ GAMEPAD INPUT =====
            // STUDENT: Each isXPressed() call only returns true once per
            // physical press (edge-detected), so this simple += 1 pattern
            // counts presses correctly instead of counting every loop
            // cycle the button happens to be held.
            if (controls.isAPressed()) countA++;
            if (controls.isBPressed()) countB++;
            if (controls.isXPressed()) countX++;
            if (controls.isYPressed()) countY++;
            if (controls.isDpadUpPressed()) countDpadUp++;
            if (controls.isDpadDownPressed()) countDpadDown++;
            if (controls.isDpadLeftPressed()) countDpadLeft++;
            if (controls.isDpadRightPressed()) countDpadRight++;
            if (controls.isLeftBumperPressed()) countLeftBumper++;
            if (controls.isRightBumperPressed()) countRightBumper++;

            // ===== TELEMETRY =====
            telemetry.addLine("===== STICKS (live value) =====");
            telemetry.addData("Left Stick X", "%.2f", controls.getLeftStickX());
            telemetry.addData("Left Stick Y", "%.2f", controls.getLeftStickY());
            telemetry.addData("Right Stick X", "%.2f", controls.getRightStickX());
            telemetry.addData("Right Stick Y", "%.2f", controls.getRightStickY());

            telemetry.addLine();
            telemetry.addLine("===== BUTTONS (press count) =====");
            telemetry.addData("A", countA);
            telemetry.addData("B", countB);
            telemetry.addData("X", countX);
            telemetry.addData("Y", countY);
            telemetry.addData("Dpad Up", countDpadUp);
            telemetry.addData("Dpad Down", countDpadDown);
            telemetry.addData("Dpad Left", countDpadLeft);
            telemetry.addData("Dpad Right", countDpadRight);
            telemetry.addData("Left Bumper", countLeftBumper);
            telemetry.addData("Right Bumper", countRightBumper);
            telemetry.update();
        }
    }
}

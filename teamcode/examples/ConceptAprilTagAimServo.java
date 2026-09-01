package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.GamepadConfig;
import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.CameraComponent;
import org.firstinspires.ftc.teamcode.components.ContinuousServoComponent;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

import java.util.List;

/**
 * ConceptAprilTagAimServo.java
 *
 * STUDENT GUIDE: Basic Feedback Control (Proportional Aiming)
 *
 * PURPOSE:
 * Shows a SENSOR continuously steering an ACTUATOR toward a moving target -
 * the camera looks for any visible AprilTag, and the continuous servo spins
 * to reduce the tag's bearing (its left/right angle) toward zero, aka
 * "aim at it." This is a tiny, real example of CLOSED-LOOP feedback control.
 *
 * WHY THIS MATTERS:
 * Instead of a fixed action, the servo's speed is PROPORTIONAL to how far
 * off-target the tag is: barely off-center → spin slowly, way off to one
 * side → spin fast. This "bigger error = bigger correction" idea is the
 * same core concept behind PID control, just without the I and D parts.
 *
 * HARDWARE SETUP:
 * Configure one Webcam and one Continuous Servo in the Driver Station's
 * hardware config, named to match HardwareNames.WEBCAM and
 * HardwareNames.SERVO_CONTINUOUS
 *
 * GAMEPAD CONTROLS:
 * - A: Toggle aiming on/off (starts OFF - the servo won't move on its own
 *   until you enable it, since it's continuously hunting for a tag)
 *
 * FTC SDK CONCEPTS:
 * - hardwareMap.get(): Looks up a configured hardware device by name
 * - LinearOpMode: Runs step-by-step, top to bottom, inside a while loop
 * - telemetry.addData()/update(): Sends text to the Driver Station screen
 */
@TeleOp(name = "Concept: AprilTag Aim Servo", group = "Concept")
public class ConceptAprilTagAimServo extends LinearOpMode {

    // How aggressively the servo reacts to bearing error.
    // STUDENT: This is the "P" (proportional) gain. Too high = jittery
    // overshoot; too low = sluggish aiming. Try adjusting this and see!
    private static final double AIM_GAIN = 0.02;

    @Override
    public void runOpMode() {
        // FTC SDK: Look up the webcam and servo from the hardware config
        WebcamName webcam = hardwareMap.get(WebcamName.class, HardwareNames.WEBCAM);
        CameraComponent camera = new CameraComponent(webcam);

        CRServo rawServo = hardwareMap.get(CRServo.class, HardwareNames.SERVO_CONTINUOUS);
        ContinuousServoComponent servo = new ContinuousServoComponent(rawServo);

        // Wrap gamepad1 so we can read button presses
        GamepadConfig controls = new GamepadConfig(gamepad1);

        // STUDENT: Starts OFF on purpose - this servo actively hunts for a
        // tag once enabled, so we don't want it spinning the instant START
        // is pressed with nobody ready to watch it.
        boolean aimingEnabled = false;

        telemetry.addLine("Ready! Press START, then A to enable aiming.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== READ GAMEPAD INPUT =====
            if (controls.isAPressed()) {
                aimingEnabled = !aimingEnabled;
            }

            // ===== SENSOR-DRIVEN FEEDBACK CONTROL =====
            List<AprilTagDetection> detections = camera.getDetections();
            double bearing = 0.0;
            double appliedPower = 0.0;

            if (aimingEnabled && !detections.isEmpty()) {
                // STUDENT: Aim at whichever tag is visible first - no need
                // to know its ID ahead of time.
                int tagId = detections.get(0).id;
                bearing = camera.getBearingDegrees(tagId);

                // Proportional control: power scales with how far off we are.
                // Positive bearing (tag to the right) -> spin one direction;
                // negative bearing (tag to the left) -> spin the other way.
                appliedPower = Math.max(-1.0, Math.min(1.0, bearing * AIM_GAIN));
            }

            servo.setPower(appliedPower);

            // ===== TELEMETRY =====
            telemetry.addLine("===== APRILTAG AIM SERVO =====");
            telemetry.addData("Aiming Enabled", aimingEnabled ? "YES" : "NO");
            telemetry.addData("Tags Detected", detections.size());
            telemetry.addData("Bearing (deg)", "%.1f", bearing);
            telemetry.addData("Servo Power", "%.2f", appliedPower);
            telemetry.addLine();
            telemetry.addLine("A: Toggle aiming on/off");
            telemetry.update();
        }

        servo.stop();
        camera.close();
    }
}

package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.CameraComponent;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

import java.util.List;

/**
 * SensorCamera.java
 *
 * STUDENT GUIDE: Reading AprilTags with a Webcam
 *
 * PURPOSE:
 * Minimal example showing how to detect AprilTags with a webcam and display
 * their ID, range, and bearing on telemetry. This OpMode doesn't drive
 * anything - it just demonstrates the camera by itself.
 *
 * HARDWARE SETUP:
 * Configure one Webcam in the Driver Station's hardware config,
 * named to match HardwareNames.WEBCAM
 *
 * GAMEPAD CONTROLS:
 * None - this OpMode just reads and displays the camera continuously.
 *
 * NOTE: CameraComponent.pauseStreaming()/resumeStreaming() exist but are
 * deliberately NOT wired to a button here. Per the FTC SDK's own docs,
 * calling resumeStreaming() before a prior stopStreaming() has finished
 * makes it block the OpMode's main loop SYNCHRONOUSLY for a second or two
 * - which is long enough to trip the Driver Station's watchdog and make it
 * look like the robot crashed/reset. Tapping Dpad Up then Dpad Down quickly
 * hit exactly this. Those methods are still fine to call ONCE during a long
 * stretch of a match where vision genuinely isn't needed - just not from an
 * instantly-toggleable gamepad button.
 *
 * FTC SDK CONCEPTS:
 * - hardwareMap.get(): Looks up a configured hardware device by name
 * - LinearOpMode: Runs step-by-step, top to bottom, inside a while loop
 * - telemetry.addData()/update(): Sends text to the Driver Station screen
 */
@TeleOp(name = "Basic: Camera", group = "Sensor")
public class SensorCamera extends LinearOpMode {

    @Override
    public void runOpMode() {
        // FTC SDK: Look up the webcam from the robot's hardware config
        WebcamName webcam = hardwareMap.get(WebcamName.class, HardwareNames.WEBCAM);
        CameraComponent camera = new CameraComponent(webcam);

        telemetry.addLine("Ready! Press START to begin AprilTag detection.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== TELEMETRY =====
            List<AprilTagDetection> detections = camera.getDetections();
            telemetry.addLine("===== APRILTAG CAMERA =====");
            // STUDENT: If tags aren't showing up (or the Driver Station's
            // Camera Stream preview looks blank), check this first - it
            // tells you where camera startup actually got to.
            telemetry.addData("Camera State", camera.getCameraState());
            telemetry.addData("Tags Detected", detections.size());

            for (AprilTagDetection tag : detections) {
                telemetry.addLine();
                telemetry.addData("Tag ID", tag.id);

                // STUDENT: range/bearing come back as NaN for tags the SDK
                // doesn't recognize (no pose data) - always check before using them.
                double range = camera.getRangeInches(tag.id);
                double bearing = camera.getBearingDegrees(tag.id);
                if (!Double.isNaN(range)) {
                    telemetry.addData("Range (in)", "%.1f", range);
                    telemetry.addData("Bearing (deg)", "%.1f", bearing);
                } else {
                    telemetry.addLine("(Unknown tag - no pose data)");
                }
            }

            telemetry.update();
        }

        // FTC SDK: Free up the camera hardware now that we're done with it
        camera.close();
    }
}

package org.firstinspires.ftc.teamcode.components;

import com.acmerobotics.dashboard.FtcDashboard;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

/**
 * CameraComponent.java
 *
 * STUDENT GUIDE: Webcam & AprilTag Abstraction
 *
 * PURPOSE:
 * This class wraps the FTC SDK's VisionPortal + AprilTagProcessor to detect
 * AprilTags with a webcam. Used for field localization, aligning to game
 * elements, and autonomous navigation.
 *
 * APRILTAG BASICS:
 * - An AprilTag is a black-and-white square barcode-like pattern
 * - Each tag has a unique ID number
 * - The camera can detect multiple tags at once
 * - For tags in the season's official TagLibrary, the SDK also reports
 *   exactly where the tag is relative to the camera (position + rotation)
 *
 * POSE DATA (detection.ftcPose):
 * - X, Y, Z: Position of the tag relative to camera, in inches
 *   * X = Right, Y = Forward, Z = Up
 * - Pitch, Roll, Yaw: Rotation of the tag, in degrees
 * - Range, Bearing, Elevation: Polar coordinates from camera to tag
 *   * Range = straight-line distance (inches)
 *   * Bearing = angle left/right (degrees, 0 = directly ahead)
 *   * Elevation = angle up/down (degrees)
 *
 * FTC SDK CONCEPTS:
 * - VisionPortal: Manages the camera stream and runs vision processors
 * - AprilTagProcessor: Analyzes camera frames for AprilTags
 * - AprilTagDetection: One detected tag's ID, pose, and metadata
 * - Processing happens on a background thread - getDetections() just
 *   reads whatever the processor found most recently
 */

public class CameraComponent {

    // Detects AprilTags in each camera frame
    private AprilTagProcessor aprilTag;

    // Manages the camera stream itself (starts/stops it, feeds frames to aprilTag)
    private VisionPortal visionPortal;

    /**
     * Constructor - Initialize the camera and AprilTag detection
     *
     * STUDENT: Pass in the webcam from HardwareConfig.
     * This starts the camera streaming immediately.
     *
     * @param webcam The FTC WebcamName object (from HardwareConfig)
     *
     * EXAMPLE USAGE:
     * WebcamName rawWebcam = hardware.webcam;  // From HardwareConfig
     * CameraComponent camera = new CameraComponent(rawWebcam);
     * // Now you can use: camera.getDetections(), camera.isTagVisible(5), etc.
     *
     * FTC SDK DETAIL:
     * AprilTagProcessor.easyCreateWithDefaults() builds a tag detector with
     * sensible default settings (good for getting started quickly).
     * VisionPortal.easyCreateWithDefaults() connects that detector to the
     * webcam and starts streaming.
     *
     * NOTE: Cameras are resource-intensive!
     * - Call close() when you're completely done with the camera
     * - Good lighting helps detection accuracy a lot
     * - The tag must be fully visible and not too far away
     */
    public CameraComponent(WebcamName webcam) {
        // FTC SDK: Build the AprilTag detector with default settings
        aprilTag = AprilTagProcessor.easyCreateWithDefaults();

        // FTC SDK: Connect the detector to the webcam and start streaming
        visionPortal = VisionPortal.easyCreateWithDefaults(webcam, aprilTag);
    }

    /**
     * Get all currently detected AprilTags
     *
     * STUDENT: Returns every tag the camera can currently see.
     *
     * @return List of AprilTagDetection objects (empty list if none visible)
     *
     * FTC SDK DETAIL:
     * aprilTag.getDetections() returns the results from the most recently
     * processed camera frame - it doesn't take a new picture right now.
     *
     * EXAMPLE:
     * for (AprilTagDetection tag : camera.getDetections()) {
     *     telemetry.addData("Tag", tag.id);
     * }
     */
    public List<AprilTagDetection> getDetections() {
        return aprilTag.getDetections();
    }

    /**
     * Get how many AprilTags are currently visible
     *
     * STUDENT: Quick way to check if the camera sees anything at all.
     *
     * @return Number of tags currently detected
     *
     * EXAMPLE:
     * if (camera.getDetectionCount() == 0) {
     *     // No tags visible - maybe scan by rotating the robot
     * }
     */
    public int getDetectionCount() {
        return getDetections().size();
    }

    /**
     * Check if a specific tag ID is currently visible
     *
     * STUDENT: Useful for "wait until I see tag X" logic.
     *
     * @param tagId The AprilTag ID to look for
     * @return true if that tag is in the current detections
     *
     * EXAMPLE:
     * if (camera.isTagVisible(5)) {
     *     // Found the tag we're looking for
     * }
     */
    public boolean isTagVisible(int tagId) {
        return getDetection(tagId) != null;
    }

    /**
     * Get the detection details for one specific tag
     *
     * STUDENT: Use this when you need more than just "is it visible" -
     * like its exact distance or angle.
     *
     * @param tagId The AprilTag ID to look for
     * @return The AprilTagDetection for that tag, or null if not currently visible
     *
     * STUDENT LEARNING:
     * This method loops through every detection and compares IDs -
     * a common pattern called LINEAR SEARCH.
     *
     * EXAMPLE:
     * AprilTagDetection tag = camera.getDetection(5);
     * if (tag != null) {
     *     telemetry.addData("Distance", tag.ftcPose.range);
     * }
     */
    public AprilTagDetection getDetection(int tagId) {
        for (AprilTagDetection detection : getDetections()) {
            if (detection.id == tagId) {
                return detection;
            }
        }
        return null;
    }

    /**
     * Get distance to a specific tag, in inches
     *
     * STUDENT: How far away is the tag?
     *
     * @param tagId The AprilTag ID to check
     * @return Range in inches, or Double.NaN if the tag isn't currently
     *         visible or isn't in the season's TagLibrary (no pose data)
     *
     * FTC SDK DETAIL:
     * Pose data (ftcPose) is only available for tags the SDK recognizes
     * from the official TagLibrary. Unknown/custom tags are still detected
     * (you get an ID), but detection.ftcPose and detection.metadata are null.
     *
     * STUDENT LEARNING:
     * We use Double.NaN here (not a magic number like -1) to match
     * getBearingDegrees() - every method in this class that returns a
     * double uses the same "not found" signal, checked with Double.isNaN().
     *
     * EXAMPLE:
     * double distance = camera.getRangeInches(5);
     * if (!Double.isNaN(distance) && distance < 12) {
     *     // Very close to the tag
     * }
     */
    public double getRangeInches(int tagId) {
        AprilTagDetection detection = getDetection(tagId);

        if (detection == null || detection.ftcPose == null) {
            return Double.NaN;
        }

        return detection.ftcPose.range;
    }

    /**
     * Get bearing (left/right angle) to a specific tag, in degrees
     *
     * STUDENT: Which way should the robot turn to face the tag?
     *
     * @param tagId The AprilTag ID to check
     * @return Bearing in degrees (negative = tag is to the left,
     *         positive = tag is to the right, 0 = directly ahead),
     *         or Double.NaN if the tag isn't visible / has no pose data
     *
     * STUDENT LEARNING:
     * We can't use 0 as the "not found" signal here - a bearing of exactly
     * 0 is a real, valid reading (tag is centered)! So we use Double.NaN
     * instead, which can never be a real bearing value. Always check with
     * Double.isNaN(bearing) before trusting the result. (getRangeInches()
     * uses the same NaN convention for consistency.)
     *
     * EXAMPLE:
     * double bearing = camera.getBearingDegrees(5);
     * if (Double.isNaN(bearing)) {
     *     // Tag not visible - don't trust this reading
     * } else if (bearing > 2) {
     *     // Tag is to the right - turn right to face it
     * }
     */
    public double getBearingDegrees(int tagId) {
        AprilTagDetection detection = getDetection(tagId);

        if (detection == null || detection.ftcPose == null) {
            return Double.NaN;
        }

        return detection.ftcPose.bearing;
    }

    /**
     * Get the camera's current connection/streaming state
     *
     * STUDENT: Useful for debugging - if you're not seeing tags (or the
     * Camera Stream preview on the Driver Station is blank), this tells you
     * WHERE in the startup process things actually are.
     *
     * @return The current VisionPortal.CameraState, e.g. OPENING_CAMERA_DEVICE,
     *         STREAMING, or ERROR
     *
     * FTC SDK DETAIL:
     * Camera startup isn't instant - it goes through several states
     * (opening the device, then starting the stream) before reaching
     * STREAMING. If it's stuck on an early state or shows ERROR, that
     * points at a hardware/configuration problem rather than your code.
     *
     * EXAMPLE:
     * telemetry.addData("Camera State", camera.getCameraState());
     */
    public VisionPortal.CameraState getCameraState() {
        return visionPortal.getCameraState();
    }

    /**
     * Pause the camera stream to save CPU resources
     *
     * STUDENT: Call this when you don't need vision for a while.
     * Resume with resumeStreaming().
     *
     * FTC SDK DETAIL:
     * visionPortal.stopStreaming() pauses the camera without releasing it,
     * so it's much faster to resume than calling close() and re-creating
     * a new CameraComponent.
     */
    public void pauseStreaming() {
        visionPortal.stopStreaming();
    }

    /**
     * Resume the camera stream after pauseStreaming()
     *
     * STUDENT: Call this to start seeing tags again after pausing.
     */
    public void resumeStreaming() {
        visionPortal.resumeStreaming();
    }

    /**
     * Start streaming the camera feed to FTC Dashboard
     *
     * STUDENT: Shows the live camera view (with AprilTag detection overlays)
     * in a browser at 192.168.43.1:8080/dash, while connected to the
     * Control Hub's WiFi - independent of the Driver Station app's own
     * "Camera Stream" menu option.
     *
     * FTC SDK DETAIL:
     * VisionPortal already implements the FTC Dashboard's CameraStreamSource
     * interface, so we can hand it straight to FtcDashboard - no separate
     * vision processor needed just for streaming.
     *
     * STUDENT LEARNING:
     * The second argument is a max frames-per-second cap. 0 means "no cap -
     * send frames as fast as they arrive," which is fine to start with, but
     * can be lowered (e.g. 10) if the stream feels laggy or is competing
     * with other CPU-heavy work on the Control Hub.
     *
     * EXAMPLE:
     * camera.startDashboardStream();
     */
    public void startDashboardStream() {
        // FTC SDK: VisionPortal is itself a valid CameraStreamSource
        FtcDashboard.getInstance().startCameraStream(visionPortal, 0);
    }

    /**
     * Stop streaming the camera feed to FTC Dashboard
     *
     * STUDENT: Call this if you want to free up the bandwidth/CPU the
     * Dashboard stream was using, without pausing AprilTag detection itself
     * (see pauseStreaming() for that).
     */
    public void stopDashboardStream() {
        FtcDashboard.getInstance().stopCameraStream();
    }

    /**
     * Shut down the camera completely
     *
     * STUDENT: Call this when you're totally done with the camera
     * (for example, at the end of an OpMode) to free up resources.
     *
     * FTC SDK DETAIL:
     * visionPortal.close() releases the camera hardware. Unlike
     * pauseStreaming(), you cannot resume after calling this - you'd
     * need to create a new CameraComponent.
     */
    public void close() {
        visionPortal.close();
    }
}
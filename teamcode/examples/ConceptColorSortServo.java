package org.firstinspires.ftc.teamcode.examples;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.ColorSensorComponent;
import org.firstinspires.ftc.teamcode.components.PositionServoComponent;

/**
 * ConceptColorSortServo.java
 *
 * STUDENT GUIDE: Sensor-Driven Decision Logic
 *
 * PURPOSE:
 * Shows a SENSOR reading choosing between several ACTUATOR positions - the
 * kind of logic a game-piece sorting mechanism uses. Every loop, whatever
 * color the sensor currently sees decides where the servo moves to. No
 * gamepad needed - just hold a colored object up to the sensor and watch.
 *
 * WHY THIS MATTERS:
 * This is a BRANCHING decision: instead of one fixed action, the code picks
 * from several possible outcomes based on sensor input. A real sorting
 * mechanism might use this exact pattern to route red game pieces one way
 * and blue ones another.
 *
 * HARDWARE SETUP:
 * Configure one Color Sensor and one Servo in the Driver Station's
 * hardware config, named to match HardwareNames.COLOR_SENSOR and
 * HardwareNames.SERVO_POSITION
 *
 * GAMEPAD CONTROLS:
 * None - this OpMode runs fully automatically off the color sensor.
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
 * for watching Red/Green/Blue update as you swap colored objects in front
 * of the sensor.
 */
@TeleOp(name = "Concept: Color Sort Servo", group = "Concept")
public class ConceptColorSortServo extends LinearOpMode {

    // Preset servo positions for each color, spread evenly across the range.
    // STUDENT: These are normalized positions (0.0-1.0), not degrees - see
    // PositionServoComponent.setPosition() for why.
    private static final double POSITION_RED = 0.0;
    private static final double POSITION_GREEN = 0.33;
    private static final double POSITION_BLUE = 0.66;
    private static final double POSITION_YELLOW = 1.0;
    private static final double POSITION_UNKNOWN = 0.5;  // neutral/home

    @Override
    public void runOpMode() {
        // FTC SDK: Look up the color sensor and servo from the hardware config
        ColorSensor rawSensor = hardwareMap.get(ColorSensor.class, HardwareNames.COLOR_SENSOR);
        ColorSensorComponent colorSensor = new ColorSensorComponent(rawSensor);

        Servo rawServo = hardwareMap.get(Servo.class, HardwareNames.SERVO_POSITION);
        PositionServoComponent servo = new PositionServoComponent(
                rawServo, HardwareNames.SERVO_POSITION_MAX_DEGREES);

        // FTC Dashboard: send every telemetry.addData()/addLine()/update()
        // call below to BOTH the Driver Station and the Dashboard browser
        // page, instead of just the Driver Station.
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        telemetry.addLine("Ready! Press START, then hold a colored object up to the sensor.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== SENSOR-DRIVEN DECISION =====
            String detectedColor = colorSensor.detectColor();

            // STUDENT: This is the whole lesson - the sensor reading decides
            // WHICH position to move to, not just whether to move.
            switch (detectedColor) {
                case "RED":
                    servo.setPosition(POSITION_RED);
                    break;
                case "GREEN":
                    servo.setPosition(POSITION_GREEN);
                    break;
                case "BLUE":
                    servo.setPosition(POSITION_BLUE);
                    break;
                case "YELLOW":
                    servo.setPosition(POSITION_YELLOW);
                    break;
                default:
                    // UNKNOWN - nothing recognizable in front of the sensor
                    servo.setPosition(POSITION_UNKNOWN);
                    break;
            }

            // ===== TELEMETRY =====
            telemetry.addLine("===== COLOR SORT SERVO =====");
            telemetry.addData("Detected Color", detectedColor);
            telemetry.addData("Red", colorSensor.getRed());
            telemetry.addData("Green", colorSensor.getGreen());
            telemetry.addData("Blue", colorSensor.getBlue());
            telemetry.addData("Servo Position", "%.2f (0-1)", servo.getPosition());
            telemetry.addData("Servo Angle", "%.1f°", servo.getAngleDegrees());
            telemetry.update();
        }
    }
}

package org.firstinspires.ftc.teamcode.examples;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.DashboardConfig;
import org.firstinspires.ftc.teamcode.GamepadConfig;
import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.VelocityDCMotorComponent;

/**
 * ConceptDcMotorVelocity.java
 *
 * STUDENT GUIDE: Closed-Loop DC Motor Velocity Control (DcMotorEx)
 *
 * PURPOSE:
 * Press A, and the motor spins up to ONE target speed (encoder
 * ticks/second) and holds it there - not a power level that just happens
 * to spin the motor fast, an actual speed the SDK's own PID loop
 * continuously fights to maintain.
 *
 * WHY THIS MATTERS:
 * With plain power control, the motor's actual speed depends on battery
 * voltage and how hard it's being loaded - the same power can spin faster
 * on a fresh battery than a tired one, and sags the moment something loads
 * the shaft. With velocity control, you ask for a speed and the SDK keeps
 * adjusting power on its own to hold it, even as conditions change. A
 * shooter flywheel is the classic example of wanting this.
 *
 * TRY THIS ON THE REAL BENCH:
 * Press A and let it spin up, then watch "Applied Power" in telemetry
 * settle down once "Actual Velocity" reaches the target. Now grip the
 * motor's output shaft by hand to load it down - Applied Power climbs on
 * its own to fight back, and Actual Velocity is pulled right back to the
 * target instead of staying sagged. Nobody touched anything.
 *
 * HARDWARE SETUP:
 * Configure one DC Motor (with encoder) in the Driver Station's hardware
 * config, named to match HardwareNames.DC_MOTOR
 *
 * GAMEPAD CONTROLS:
 * - A: Toggle the motor between spinning at the target velocity and stopped
 *
 * FTC SDK CONCEPTS:
 * - DcMotorEx: An extended motor interface with getVelocity()/setVelocity(),
 *   not available on plain DcMotor
 * - RUN_USING_ENCODER: The run mode that makes setVelocity() closed-loop
 * - See VelocityDCMotorComponent for the full explanation
 *
 * FTC DASHBOARD:
 * This OpMode's telemetry also streams to FTC Dashboard
 * (http://192.168.43.1:8080/dash while connected to the Control Hub's
 * WiFi), which graphs Target vs. Actual Velocity live - the clearest way
 * to watch the SDK reach and hold that target while you load the shaft by
 * hand. The target itself, DashboardConfig.MOTOR_MAX_VELOCITY_TICKS_PER_SEC,
 * is also live-tunable there - see the comment on that field for how to
 * measure the real number on this bench.
 */
@TeleOp(name = "Concept: DC Motor Velocity", group = "Concept")
public class ConceptDcMotorVelocity extends LinearOpMode {

    @Override
    public void runOpMode() {
        // FTC SDK: DcMotorEx, not plain DcMotor - that's what exposes
        // getVelocity()/setVelocity() at all.
        DcMotorEx rawMotor = hardwareMap.get(DcMotorEx.class, HardwareNames.DC_MOTOR);
        VelocityDCMotorComponent motor = new VelocityDCMotorComponent(rawMotor);

        // Wrap gamepad1 so we can read the button press
        GamepadConfig controls = new GamepadConfig(gamepad1);

        // STUDENT: Whether we're currently asking the motor to spin at all.
        // Toggled by A, same edge-detected on/off pattern used elsewhere in
        // this codebase (see ConceptAprilTagAimServo).
        boolean running = false;

        // FTC Dashboard: send every telemetry.addData()/addLine()/update()
        // call below to BOTH the Driver Station and the Dashboard browser
        // page, instead of just the Driver Station.
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        telemetry.addLine("Ready! Press START, then A to spin up to the target velocity.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // ===== READ GAMEPAD INPUT =====
            if (controls.isAPressed()) {
                running = !running;
            }

            // STUDENT: This is the whole lesson - we're commanding a
            // SPEED, not a power. Everything below is just watching the
            // SDK reach and hold that speed on its own.
            motor.setTargetVelocity(running ? DashboardConfig.MOTOR_MAX_VELOCITY_TICKS_PER_SEC : 0);

            // ===== TELEMETRY =====
            telemetry.addLine("===== DC MOTOR (VELOCITY) =====");
            telemetry.addData("Running", running ? "YES" : "NO");
            telemetry.addData("Target Velocity", "%.0f ticks/sec", motor.getTargetVelocity());
            telemetry.addData("Actual Velocity", "%.0f ticks/sec", motor.getVelocity());
            telemetry.addData("Applied Power", "%.2f", motor.getPower());
            telemetry.addLine();
            telemetry.addLine("A: toggle spin up to target velocity / stop");
            telemetry.update();
        }

        motor.stop();
    }
}

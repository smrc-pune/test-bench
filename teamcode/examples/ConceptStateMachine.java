package org.firstinspires.ftc.teamcode.examples;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.GamepadConfig;
import org.firstinspires.ftc.teamcode.HardwareNames;
import org.firstinspires.ftc.teamcode.components.ColorSensorComponent;
import org.firstinspires.ftc.teamcode.components.ContinuousServoComponent;
import org.firstinspires.ftc.teamcode.components.PositionDCMotorComponent;
import org.firstinspires.ftc.teamcode.components.PositionServoComponent;
import org.firstinspires.ftc.teamcode.components.TouchSensorComponent;

/**
 * ConceptStateMachine.java
 *
 * STUDENT GUIDE: A Real, Five-State State Machine (Color Sorter)
 *
 * PURPOSE:
 * A fuller example of the state machine pattern than a simple "press A,
 * press B" demo - this one runs a small, self-contained color-sorting
 * routine across FIVE states, using every actuator and sensor on the test
 * bench together: the touch sensor starts a cycle, the DC motor "searches,"
 * the color sensor decides what was found, the positional servo aims a
 * bin, the continuous servo pushes the item out, and the motor drives
 * itself back home - all without a single guessed sleep().
 *
 * WHY THIS MATTERS:
 * A real mechanism is rarely one sensor driving one motor. It's usually a
 * SEQUENCE: search, decide, act, recover, repeat - with different rules for
 * "what does it mean to be done" at each step. This is what that looks like
 * with actual hardware conditions instead of gamepad button presses.
 *
 * STATE DIAGRAM:
 *
 *        (touch pressed) ---> SEEKING ---> SORTING ---> RELEASING
 *              ^                                              |
 *              |                                              v
 *        WAIT_FOR_TOUCH  <----------------------- RETURNING_HOME
 *
 * - WAIT_FOR_TOUCH:  motor off, waiting for a NEW touch-sensor press
 * - SEEKING:         motor drives "outward," watching the color sensor
 * - SORTING:         motor stopped, positional servo aims at the right bin
 * - RELEASING:       continuous servo pulses briefly to push the item out
 * - RETURNING_HOME:  motor drives itself back to its starting encoder tick
 *
 * WHEN IS A TIMER OKAY? (the nuance this example adds)
 * The whole point of a state machine is escaping sleep()-chains that GUESS
 * how long something takes. But this example still uses ElapsedTime in two
 * places - on purpose, and they are NOT the same mistake:
 *   1. SEEKING has a bounded TIMEOUT. It's a safety net, not the plan - if
 *      a real condition (a valid color) shows up first, that wins. The
 *      timer only fires if nothing ever does, so the robot can't get stuck
 *      searching forever.
 *   2. SORTING and RELEASING use a short, bounded SETTLE/PULSE timer.
 *      That's because a positional servo and a continuous servo don't
 *      report back "I'm there yet" the way an encoder does - there's no
 *      condition to check. Giving an already-decided motion a fixed amount
 *      of time to physically finish is different from using a timer to
 *      DECIDE what happens next.
 * Contrast this with RETURNING_HOME, which has a real condition available
 * (the encoder) and uses it instead - hasReachedTarget(), not a timer.
 *
 * HARDWARE SETUP:
 * Configure one DC Motor, one Touch Sensor, one Color Sensor, one
 * positional Servo, and one continuous-rotation Servo (CRServo) in the
 * Driver Station's hardware config, named to match HardwareNames.DC_MOTOR,
 * HardwareNames.TOUCH_SENSOR, HardwareNames.COLOR_SENSOR,
 * HardwareNames.SERVO_POSITION, and HardwareNames.SERVO_CONTINUOUS.
 *
 * GAMEPAD CONTROLS:
 * - B: Emergency stop - instantly halts every actuator and resets back to
 *   WAIT_FOR_TOUCH, from ANY state. Real mechanisms need an abort button
 *   too, not just a happy path.
 *
 * FTC SDK CONCEPTS:
 * - enum + switch: one named case per state (see ConceptDcMotorPosition
 *   and the "Concept: Blackboard" sample for related patterns)
 * - ElapsedTime: a stopwatch you can reset() and query with milliseconds()
 * - PositionDCMotorComponent.moveToPosition() with a deliberately distant
 *   target is how this OpMode gets "just keep driving" behavior WITHOUT
 *   giving up closed-loop control - see the SEEKING case for why.
 *
 * FTC DASHBOARD:
 * This OpMode's telemetry also streams to FTC Dashboard
 * (http://192.168.43.1:8080/dash while connected to the Control Hub's
 * WiFi). Watch "Current State" and "Time In State" there to see the whole
 * sequence run without staring at the small Driver Station screen.
 */
@TeleOp(name = "Concept: State Machine", group = "Concept")
public class ConceptStateMachine extends LinearOpMode {

    // ===== THE STATES =====
    // STUDENT: Naming every state (instead of using raw 0/1/2/3/4) means
    // inserting a new step later doesn't require renumbering anything -
    // see ConceptStateMachine's companion slide deck for why this matters.
    private enum State {
        WAIT_FOR_TOUCH,
        SEEKING,
        SORTING,
        RELEASING,
        RETURNING_HOME
    }

    // ===== SEEKING tuning =====
    // A deliberately distant target. RUN_TO_POSITION mode will happily
    // drive toward this forever (it's far further than the bench motor
    // could ever actually turn) - so this gives us "just keep spinning"
    // behavior while the motor is STILL under real closed-loop control and
    // STILL accumulating real encoder ticks the whole time. That matters
    // later: RETURNING_HOME uses those exact accumulated ticks to drive
    // back to precisely where it started.
    private static final int SEEK_TARGET_TICKS = 100_000;
    private static final double SEEK_MOVE_POWER = 0.4;
    private static final double SEEK_TIMEOUT_MS = 8000; // safety net, not the plan

    // A color reading only counts if the sensor is actually seeing
    // something (alpha/brightness above this floor) AND it isn't
    // "UNKNOWN". Otherwise SEEKING would "detect" whatever the sensor
    // happens to be pointed at when the OpMode starts.
    private static final int MIN_ALPHA_FOR_DETECTION = 40;

    // ===== SORTING targets =====
    // Bin positions for the positional servo, 0.0-1.0 (see
    // PositionServoComponent). Anything that isn't RED or BLUE - GREEN,
    // YELLOW, or a SEEKING timeout - goes to the reject bin in the middle.
    private static final double RED_BIN_POSITION = 0.0;
    private static final double BLUE_BIN_POSITION = 1.0;
    private static final double REJECT_BIN_POSITION = 0.5;

    // Positional servos don't report "I've arrived" - this is a bounded
    // settle time for a motion we already commanded, not a guess about an
    // external event. See the class-level "WHEN IS A TIMER OKAY?" note.
    private static final double SERVO_SETTLE_MS = 500;

    // ===== RELEASING tuning =====
    // Same reasoning as SERVO_SETTLE_MS: a fixed, bounded pulse for a
    // continuous servo that has no position feedback at all.
    private static final double RELEASE_POWER = 1.0;
    private static final double RELEASE_PULSE_MS = 500;

    @Override
    public void runOpMode() {
        // ===== HARDWARE SETUP =====
        // FTC SDK: Look up every device from the hardware config, then
        // wrap each one in its student-facing component class.
        DcMotor rawMotor = hardwareMap.get(DcMotor.class, HardwareNames.DC_MOTOR);
        PositionDCMotorComponent motor = new PositionDCMotorComponent(rawMotor);
        motor.setMovePower(SEEK_MOVE_POWER);

        TouchSensor rawTouch = hardwareMap.get(TouchSensor.class, HardwareNames.TOUCH_SENSOR);
        TouchSensorComponent touchSensor = new TouchSensorComponent(rawTouch);

        ColorSensor rawColor = hardwareMap.get(ColorSensor.class, HardwareNames.COLOR_SENSOR);
        ColorSensorComponent colorSensor = new ColorSensorComponent(rawColor);

        Servo rawPosServo = hardwareMap.get(Servo.class, HardwareNames.SERVO_POSITION);
        PositionServoComponent binServo = new PositionServoComponent(
                rawPosServo, HardwareNames.SERVO_POSITION_MAX_DEGREES);
        binServo.setPosition(REJECT_BIN_POSITION);

        CRServo rawCrServo = hardwareMap.get(CRServo.class, HardwareNames.SERVO_CONTINUOUS);
        ContinuousServoComponent ejector = new ContinuousServoComponent(rawCrServo);

        // Wrap gamepad1 so we can read the emergency-stop button
        GamepadConfig controls = new GamepadConfig(gamepad1);

        // ===== STATE MACHINE BOOKKEEPING =====
        State state = State.WAIT_FOR_TOUCH;

        // A stopwatch we reset() every time we ENTER a state, so each case
        // can ask "how long have I been here?" - used for the SEEKING
        // timeout and the SORTING/RELEASING settle pulses.
        ElapsedTime stateTimer = new ElapsedTime();

        // Track the previous touch reading so WAIT_FOR_TOUCH only starts a
        // cycle on a NEW press, never on every loop the sensor is held.
        boolean prevTouchPressed = false;

        // What SEEKING found (or "TIMEOUT" if it never found anything),
        // read back by SORTING to pick a bin. Persists across states -
        // this is exactly what makes it different from a local variable.
        String detectedColor = "NONE";

        int cyclesCompleted = 0;
        int emergencyStops = 0;

        // FTC Dashboard: send every telemetry.addData()/addLine()/update()
        // call below to BOTH the Driver Station and the Dashboard browser
        // page, instead of just the Driver Station.
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        telemetry.addLine("Ready! Press START, then press the touch sensor to run a cycle.");
        telemetry.addLine("Gamepad B: emergency stop, from any state.");
        telemetry.update();

        waitForStart();
        stateTimer.reset();

        while (opModeIsActive()) {
            // ===== EMERGENCY STOP (works from ANY state) =====
            // STUDENT: Not every transition has to come from inside the
            // switch below. A real abort button has to interrupt whatever
            // is happening RIGHT NOW, so we check it before the switch and
            // let it override anything the current state was about to do.
            if (controls.isBPressed()) {
                motor.stop();
                ejector.stop();
                binServo.setPosition(REJECT_BIN_POSITION);
                state = State.WAIT_FOR_TOUCH;
                stateTimer.reset();
                emergencyStops++;
            }

            boolean touchPressed = touchSensor.isPressed();

            switch (state) {
                case WAIT_FOR_TOUCH:
                    // Only a NEW press starts a cycle - see
                    // TouchSensorComponent for why isPressed() alone isn't
                    // enough (it's true every loop while held).
                    if (touchPressed && !prevTouchPressed) {
                        detectedColor = "NONE";
                        motor.moveToPosition(SEEK_TARGET_TICKS);
                        state = State.SEEKING;
                        stateTimer.reset();
                    }
                    break;

                case SEEKING: {
                    boolean seeingSomething = colorSensor.getAlpha() >= MIN_ALPHA_FOR_DETECTION;
                    String reading = colorSensor.detectColor();
                    boolean validColor = seeingSomething && !reading.equals("UNKNOWN");
                    boolean timedOut = stateTimer.milliseconds() >= SEEK_TIMEOUT_MS;

                    if (validColor) {
                        // The real condition won - stop exactly here and
                        // remember what we found.
                        detectedColor = reading;
                        motor.stop();
                        state = State.SORTING;
                        stateTimer.reset();
                    } else if (timedOut) {
                        // The safety net fired instead - nothing valid was
                        // ever seen. We still leave cleanly, just with a
                        // result that routes to the reject bin.
                        detectedColor = "TIMEOUT";
                        motor.stop();
                        state = State.SORTING;
                        stateTimer.reset();
                    }
                    break;
                }

                case SORTING: {
                    // One command, aimed by whatever SEEKING found. RED and
                    // BLUE get their own bin; everything else (GREEN,
                    // YELLOW, or a timeout) shares the reject bin.
                    double binPosition;
                    if (detectedColor.equals("RED")) {
                        binPosition = RED_BIN_POSITION;
                    } else if (detectedColor.equals("BLUE")) {
                        binPosition = BLUE_BIN_POSITION;
                    } else {
                        binPosition = REJECT_BIN_POSITION;
                    }
                    binServo.setPosition(binPosition);

                    // Bounded settle time for a motion we already
                    // commanded - not a guess about an external event.
                    if (stateTimer.milliseconds() >= SERVO_SETTLE_MS) {
                        state = State.RELEASING;
                        stateTimer.reset();
                    }
                    break;
                }

                case RELEASING:
                    ejector.setPower(RELEASE_POWER);

                    if (stateTimer.milliseconds() >= RELEASE_PULSE_MS) {
                        ejector.stop();
                        motor.moveToPosition(0);
                        state = State.RETURNING_HOME;
                        stateTimer.reset();
                    }
                    break;

                case RETURNING_HOME:
                    // Unlike SORTING/RELEASING, a real condition IS
                    // available here - the encoder - so we use it instead
                    // of another timer.
                    if (motor.hasReachedTarget()) {
                        cyclesCompleted++;
                        state = State.WAIT_FOR_TOUCH;
                        stateTimer.reset();
                    }
                    break;
            }

            prevTouchPressed = touchPressed;

            // ===== TELEMETRY =====
            telemetry.addLine("===== STATE MACHINE: COLOR SORTER =====");
            telemetry.addData("Current State", state);
            telemetry.addData("Time In State", "%.1f sec", stateTimer.seconds());
            telemetry.addData("Cycles Completed", cyclesCompleted);
            telemetry.addData("Emergency Stops", emergencyStops);
            telemetry.addLine();
            telemetry.addLine("--- Motor ---");
            telemetry.addData("Position", motor.getCurrentPosition());
            telemetry.addData("Target", motor.getTargetPosition());
            telemetry.addLine();
            telemetry.addLine("--- Color Sensor ---");
            telemetry.addData("Reading", colorSensor.detectColor());
            telemetry.addData("Alpha", colorSensor.getAlpha());
            telemetry.addData("Last Sorted As", detectedColor);
            telemetry.addLine();
            telemetry.addLine("--- Servos ---");
            telemetry.addData("Bin Servo", "%.2f (0-1)", binServo.getPosition());
            telemetry.addData("Ejector Power", "%.2f", ejector.getPower());
            telemetry.addLine();
            telemetry.addLine("Touch sensor: start a cycle | B: emergency stop");
            telemetry.update();
        }

        // Leave everything in a safe, stopped state when the OpMode ends.
        motor.stop();
        ejector.stop();
    }
}

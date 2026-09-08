# FTC Test Bench — Student Learning Code

A set of hardware-abstraction classes and example OpModes built for a physical FTC test bench, used to teach students how individual hardware components (motors, servos, sensors, a camera) work before they're combined into a full robot.

This isn't competition robot code — it's a teaching codebase. Every class here favors clear, heavily-commented Java over clever or terse code, so students can read the source itself as part of the lesson.

## Test Bench Hardware

| Device | Qty | Config Name (`HardwareNames`) |
|---|---|---|
| DC Motor (with encoder) | 1 | `DC_MOTOR` |
| Servo — positional | 1 | `SERVO_POSITION` |
| Servo — continuous rotation | 1 | `SERVO_CONTINUOUS` |
| Webcam | 1 | `WEBCAM` |
| Touch Sensor | 1 | `TOUCH_SENSOR` |
| Color Sensor (REV Color Sensor V3) | 1 | `COLOR_SENSOR` |
| Gamepad | 1 | — |

All device names live in one place: [`HardwareNames.java`](HardwareNames.java). If you rename a device in the Driver Station's Robot Configuration, update it there — nowhere else.

## Project Structure

```
teamcode/
├── HardwareNames.java      Central hardware device name constants
├── GamepadConfig.java      Generic, reusable gamepad abstraction
├── DashboardConfig.java    Live-tunable values, shown/editable in FTC Dashboard
├── components/             One class per physical device, wraps the raw FTC SDK object
└── examples/                OpModes that demonstrate the components, gamepad-driven
```

### `components/` — Hardware Abstractions

Each class wraps a single FTC SDK hardware interface and exposes a small, purpose-built API. None of them reference the gamepad or any other component — they only know about their own device.

| Class | Wraps | Purpose |
|---|---|---|
| [`ContinuousDCMotorComponent`](components/ContinuousDCMotorComponent.java) | `DcMotor` | Open-loop velocity control — set a power, it spins |
| [`PositionDCMotorComponent`](components/PositionDCMotorComponent.java) | `DcMotor` | Closed-loop position control using the motor's encoder (`RUN_TO_POSITION`) |
| [`VelocityDCMotorComponent`](components/VelocityDCMotorComponent.java) | `DcMotorEx` | Closed-loop **speed** control — set a target in encoder ticks/second (`RUN_USING_ENCODER` + `setVelocity()`) and the SDK holds it |
| [`PositionServoComponent`](components/PositionServoComponent.java) | `Servo` | Positional servo control — increment/decrement/jump to a position, with a configurable real-world degree range (not every servo is 180°) |
| [`ContinuousServoComponent`](components/ContinuousServoComponent.java) | `CRServo` | Continuous-rotation servo — same idea as the DC motor, for a servo |
| [`TouchSensorComponent`](components/TouchSensorComponent.java) | `TouchSensor` | Press state + manual press counting (with edge-detection guidance) |
| [`ColorSensorComponent`](components/ColorSensorComponent.java) | `ColorSensor` | RGB/Alpha reading and simple color-name detection |
| [`DistanceSensorComponent`](components/DistanceSensorComponent.java) | `DistanceSensor` | Distance reading in CM/MM/inches (not currently on the physical bench) |
| [`CameraComponent`](components/CameraComponent.java) | `VisionPortal` + `AprilTagProcessor` | AprilTag detection (ID, range, bearing), plus optionally streaming the live camera feed to FTC Dashboard |

### `GamepadConfig.java`

A generic wrapper around a single `Gamepad`. It knows nothing about what's wired to it — no component references, no assumptions about what a button *does*. Every button method is **edge-detected**: it returns `true` for exactly one loop cycle per physical press, never on every cycle a button is held. This is a deliberate design choice (see the class doc for the reasoning) — there's no "is currently held" query, only "was just pressed."

```java
GamepadConfig driver = new GamepadConfig(gamepad1);
double power = driver.getLeftStickY();
if (driver.isAPressed()) { ... }   // fires once per press, not while held
```

### `DashboardConfig.java`

A central, `@Config`-annotated class holding values meant to be tuned live from FTC Dashboard while an OpMode is running — no rebuild/redeploy per change. See the [FTC Dashboard](#ftc-dashboard) section below for the full picture. Only put a value here if you actually want it editable at runtime; it can't be `final`, so anything placed here trades away compile-time immutability for live tunability.

## Example OpModes

All examples live in [`examples/`](examples/) and extend `LinearOpMode`. They're organized in a rough difficulty progression:

### Tier 0 — No hardware risk

| OpMode | Components | Gamepad | What it teaches |
|---|---|---|---|
| `Basic: Gamepad` | none | all sticks/buttons → telemetry | Reading raw controller input safely, before touching any actuator |

### Tier 1 — Single sensor (read-only)

| OpMode | Components | Gamepad | What it teaches |
|---|---|---|---|
| `Basic: Touch` | Touch Sensor | RB: reset press count | Digital sensor reading, manual press counting with edge-detection |
| `Basic: Color` | Color Sensor | none | RGB/Alpha reading, `detectColor()` |
| `Basic: Camera` | Webcam | none | AprilTag detection, reading range/bearing telemetry |

### Tier 2 — Single actuator (direct control)

| OpMode | Components | Gamepad | What it teaches |
|---|---|---|---|
| `Basic: DC Motor` | DC Motor | Left Stick Y | Open-loop velocity control |
| `Basic: Servo Position` | Position Servo | Dpad Up/Down | Positional servo, incremental movement |
| `Basic: Servo Continuous` | Continuous Servo | Right Stick Y | Continuous-rotation servo |
| `Concept: DC Motor Position` | DC Motor | RB/LB: ±100 ticks, A: move | Closed-loop position control, encoders, dial-in-then-commit interaction |
| `Concept: DC Motor Velocity` | DC Motor | A: toggle spin/stop | Closed-loop **speed** control via `DcMotorEx` — the motor reaches one target speed and holds it (see below) |

#### `Concept: DC Motor Velocity` — speed instead of power

Every other motor example on this bench uses plain `DcMotor` and `setPower()` — whatever power you send is
exactly what gets applied, with no feedback. This example uses **`DcMotorEx`** instead, which adds
`setVelocity()`/`getVelocity()`: press A and the motor spins up to ONE target speed (encoder ticks/second,
tunable live via `DashboardConfig.MOTOR_MAX_VELOCITY_TICKS_PER_SEC`), and the SDK's own PID loop
continuously adjusts the real applied power on its own to hold it there.

**Try it on the real bench:** press A and let it spin up, then watch `Applied Power` in telemetry settle
down once `Actual Velocity` reaches the target. Now grip the motor's output shaft by hand to load it down —
`Applied Power` climbs on its own to fight back, and `Actual Velocity` is pulled right back to the target
instead of staying sagged. That's the whole point: a flywheel, intake, or anything else that needs a
*consistent* speed regardless of battery voltage or load wants velocity control, not power control.

### Tier 3 — Sensor drives actuator / multi-actuator coordination

| OpMode | Components | Gamepad | What it teaches |
|---|---|---|---|
| `Concept: Touch Stops Motor` | Touch Sensor + DC Motor | Left Stick Y | Sensor as a safety limit switch, overriding driver input |
| `Concept: Color Sort Servo` | Color Sensor + Position Servo | none | Sensor-driven branching logic — detected color selects a servo position |
| `Concept: AprilTag Aim Servo` | Webcam + Continuous Servo | A: enable/disable | Basic proportional (closed-loop) feedback control, **plus FTC Dashboard**: live-graphed telemetry, a live camera preview, and a live-tunable gain constant (see below) |
| `Concept: Coordinated Actuators` | DC Motor + Position Servo | A: Stow, B: Deploy | One trigger commanding multiple actuators toward matched presets |

### Tier 4 — Full state machine (every component, together)

| OpMode | Components | Gamepad | What it teaches |
|---|---|---|---|
| `Concept: State Machine` | DC Motor + Touch Sensor + Color Sensor + Position Servo + Continuous Servo | B: emergency stop (any state) | A five-state `enum` + `switch` state machine (search → decide → act → recover → repeat) built from real sensor conditions instead of gamepad button presses, including a bounded safety-net timeout and a from-any-state abort transition. See the class Javadoc for a note on when a bounded settle/pulse timer is still okay in a state machine, and when it isn't. |

#### `Concept: State Machine` — full walkthrough

[`ConceptStateMachine.java`](examples/ConceptStateMachine.java) runs a small, self-contained color-sorting
routine that uses every actuator and sensor on the bench together, one touch-sensor press at a time.

```
       (touch pressed) ---> SEEKING ---> SORTING ---> RELEASING
             ^                                              |
             |                                              v
       WAIT_FOR_TOUCH  <----------------------- RETURNING_HOME
```

| State | What's happening | How it exits |
|---|---|---|
| `WAIT_FOR_TOUCH` | Everything is off/idle | A **new** touch-sensor press (edge-detected, not just "is it pressed") |
| `SEEKING` | Motor drives toward a deliberately distant encoder target while the color sensor watches every loop | A valid color reading (alpha above a floor, name isn't `"UNKNOWN"`) — **or** an 8-second safety-net timeout, whichever comes first |
| `SORTING` | Motor stopped; positional servo aims at the bin for whatever color was found (or the reject bin, for a timeout/unrecognized color) | A short, bounded settle timer |
| `RELEASING` | Continuous servo pulses briefly to push the item out | A short, bounded pulse timer |
| `RETURNING_HOME` | Motor drives itself back to its exact starting encoder tick | `hasReachedTarget()` — a real condition, not a timer |

From `RETURNING_HOME`, the cycle loops itself back to `WAIT_FOR_TOUCH` automatically — no second button
press needed, ready for the next item.

**Hardware used:** `DC_MOTOR` (with encoder), `TOUCH_SENSOR`, `COLOR_SENSOR`, `SERVO_POSITION`
(the sorting bin), `SERVO_CONTINUOUS` (the ejector) — every device on the bench except the webcam.

**Gamepad:** `B` is an emergency stop that works from **any** state, not just a normal transition. It
immediately zeroes every actuator and resets to `WAIT_FOR_TOUCH` — the "abort button" every real
mechanism needs alongside its happy path.

**When is a timer okay?** The whole point of a state machine is escaping `sleep()`-chains that *guess*
how long something takes — but this example still uses `ElapsedTime` in two places, and neither is that
mistake:

1. **`SEEKING`'s timeout is a safety net, not the plan.** A real condition (a valid color) always wins if
   it shows up first; the timer only fires if nothing ever does, so the routine can't get stuck searching
   forever.
2. **`SORTING`/`RELEASING`'s settle and pulse timers bound a motion already in progress.** Positional and
   continuous servos don't report "I've arrived" the way an encoder does — there's no condition to check.
   Giving an already-decided motion a fixed, bounded amount of time to physically finish is different from
   using a timer to *decide* what happens next.

Contrast both with `RETURNING_HOME`, which has a real condition available (the encoder) and uses it
instead — `motor.hasReachedTarget()`, not a timer.

## Getting Started

1. In the Driver Station's Robot Configuration, name each device to **exactly** match the strings in [`HardwareNames.java`](HardwareNames.java).
2. Make sure the Gradle setup includes FTC Dashboard — see [FTC Dashboard](#ftc-dashboard) below. If you just pulled this repo fresh, **sync Gradle** before building (see the gotcha about this further down).
3. Build and deploy this module to the Control Hub.
4. On the Driver Station, select an OpMode by its `@TeleOp` name (e.g. "Basic: Touch") — they're grouped as `Basic` or `Concept` in the OpMode list.
5. Start with Tier 0/1 (nothing moves) before running any Tier 2/3 OpMode.

## FTC Dashboard

[FTC Dashboard](https://acmerobotics.github.io/ftc-dashboard/) is a third-party (ACME Robotics) web-based tool that runs a server on the Robot Controller itself. Connect a laptop/tablet browser to the Control Hub's WiFi and open:

```
http://192.168.43.1:8080/dash
```

No app install needed on the viewing device. It gives us three things the Driver Station app can't:

1. **Live-graphed telemetry**, instead of static text on the DS screen.
2. **Live-tunable constants** — anything in `DashboardConfig.java` (annotated `@Config`) becomes an editable field in the browser, changeable **while the OpMode is running**.
3. **A live camera preview**, independent of the Driver Station's own "Camera Stream" menu — useful if that menu is ever missing (see the Driver Hub/Control Hub version-mismatch gotcha below).

### Required Gradle setup

This does **not** ship with the stock FTC SDK — it's an added third-party dependency. Both changes go in the root-level `build.dependencies.gradle` (not any file inside `TeamCode/`):

```gradle
repositories {
    mavenCentral()
    google()
    maven { url = 'https://maven.brott.dev/' }   // required: FTC Dashboard's own repo
}

dependencies {
    // ...existing org.firstinspires.ftc:* lines...
    implementation 'com.acmerobotics.dashboard:dashboard:0.6.0'   // required: FTC Dashboard
}
```

**After editing this file, you must trigger a Gradle sync in Android Studio** (File → Sync Project with Gradle Files, or the "Sync Now" banner) before the `com.acmerobotics.dashboard` package will resolve — editing the `.gradle` file alone does not update the IDE's index. See the gotcha below.

### How it's used in code

Two patterns, both demonstrated in [`ConceptAprilTagAimServo.java`](examples/ConceptAprilTagAimServo.java):

**Telemetry** — one line broadcasts every existing `telemetry.addData()`/`addLine()`/`update()` call to both the Driver Station and the Dashboard, no other call sites need to change:
```java
telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
```

**Live-tunable values** — add a `public static` (not `final`) field to `DashboardConfig.java`, then reference it from your OpMode:
```java
// In DashboardConfig.java
public static double AIM_GAIN = 0.02;

// In the OpMode
appliedPower = bearing * DashboardConfig.AIM_GAIN;   // editable live in the browser
```

**Camera streaming** — `CameraComponent.startDashboardStream()` shows the live webcam feed (with AprilTag overlays) in the Dashboard's camera panel:
```java
CameraComponent camera = new CameraComponent(webcam);
camera.startDashboardStream();
```

## Hardware Notes & Gotchas

Real lessons learned testing this code on the physical bench — worth knowing before you extend this project:

- **The REV Color Sensor V3's LED is physically hardwired always-on.** Neither the legacy `ColorSensor.enableLed()` nor the `SwitchableLight` interface can turn it off — confirmed directly from the FTC SDK's own source. `ColorSensorComponent` no longer exposes LED control at all.
- **Not every "180° servo" actually is one.** This bench's positional servo measures closer to 300° in practice. `PositionServoComponent` takes an optional `maxAngleDegrees` constructor argument for exactly this reason — always verify against the real hardware rather than assuming.
- **`VisionPortal.resumeStreaming()`/`stopStreaming()` are asynchronous and can block.** Calling one before the other's prior operation completes makes it block the OpMode's main loop synchronously for a second or two — long enough to trip the Driver Station's watchdog and look like the robot crashed. Don't wire these to instantly-toggleable gamepad buttons.
- **The Driver Station's "Camera Stream" preview doesn't appear automatically** — even with a `VisionPortal` actively `STREAMING`, a driver has to manually open it from the three-dot overflow menu. If that menu option is missing entirely, check that the Driver Hub/Control Hub software versions match. (FTC Dashboard's own camera panel, described above, is a workaround that doesn't depend on this menu at all.)
- **`GamepadConfig` has no "held" state, only edge-detected "just pressed."** A control meant to run continuously while a button is held (e.g. "hold A for full power") can't be built directly off these methods — the OpMode itself needs to latch a flag on press and clear it on release.
- **"Cannot resolve symbol" for a newly-added dependency (like `com.acmerobotics.dashboard`) usually just means Android Studio hasn't re-synced yet.** Editing a `.gradle` file doesn't refresh the IDE's index automatically — always follow it with File → Sync Project with Gradle Files before assuming the dependency itself is broken.

## Still To Do

A few more example ideas from the project's planning notes, not yet implemented: `ConceptColorGatesMotor`, `ConceptTouchToggleServo`, `ConceptColorSampleHold`, `ConceptAprilTagSelectPosition`, `ConceptMotorHoming`, `ConceptReversedMotor`, `ConceptTimedSequence`, and a `TestBenchFull` capstone that ties every component together.

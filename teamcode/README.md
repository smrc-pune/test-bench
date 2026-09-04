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

### Tier 3 — Sensor drives actuator / multi-actuator coordination

| OpMode | Components | Gamepad | What it teaches |
|---|---|---|---|
| `Concept: Touch Stops Motor` | Touch Sensor + DC Motor | Left Stick Y | Sensor as a safety limit switch, overriding driver input |
| `Concept: Color Sort Servo` | Color Sensor + Position Servo | none | Sensor-driven branching logic — detected color selects a servo position |
| `Concept: AprilTag Aim Servo` | Webcam + Continuous Servo | A: enable/disable | Basic proportional (closed-loop) feedback control, **plus FTC Dashboard**: live-graphed telemetry, a live camera preview, and a live-tunable gain constant (see below) |
| `Concept: Coordinated Actuators` | DC Motor + Position Servo | A: Stow, B: Deploy | One trigger commanding multiple actuators toward matched presets |

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

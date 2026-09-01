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

All device names live in one place: [`HardwareNames.java`](src/main/java/org/firstinspires/ftc/teamcode/HardwareNames.java). If you rename a device in the Driver Station's Robot Configuration, update it there — nowhere else.

## Project Structure

```
teamcode/
├── HardwareNames.java      Central hardware device name constants
├── GamepadConfig.java      Generic, reusable gamepad abstraction
├── components/             One class per physical device, wraps the raw FTC SDK object
└── examples/               OpModes that demonstrate the components, gamepad-driven
```

### `components/` — Hardware Abstractions

Each class wraps a single FTC SDK hardware interface and exposes a small, purpose-built API. None of them reference the gamepad or any other component — they only know about their own device.

| Class | Wraps | Purpose |
|---|---|---|
| [`ContinuousDCMotorComponent`](src/main/java/org/firstinspires/ftc/teamcode/components/ContinuousDCMotorComponent.java) | `DcMotor` | Open-loop velocity control — set a power, it spins |
| [`PositionDCMotorComponent`](src/main/java/org/firstinspires/ftc/teamcode/components/PositionDCMotorComponent.java) | `DcMotor` | Closed-loop position control using the motor's encoder (`RUN_TO_POSITION`) |
| [`PositionServoComponent`](src/main/java/org/firstinspires/ftc/teamcode/components/PositionServoComponent.java) | `Servo` | Positional servo control — increment/decrement/jump to a position, with a configurable real-world degree range (not every servo is 180°) |
| [`ContinuousServoComponent`](src/main/java/org/firstinspires/ftc/teamcode/components/ContinuousServoComponent.java) | `CRServo` | Continuous-rotation servo — same idea as the DC motor, for a servo |
| [`TouchSensorComponent`](src/main/java/org/firstinspires/ftc/teamcode/components/TouchSensorComponent.java) | `TouchSensor` | Press state + manual press counting (with edge-detection guidance) |
| [`ColorSensorComponent`](src/main/java/org/firstinspires/ftc/teamcode/components/ColorSensorComponent.java) | `ColorSensor` | RGB/Alpha reading and simple color-name detection |
| [`DistanceSensorComponent`](src/main/java/org/firstinspires/ftc/teamcode/components/DistanceSensorComponent.java) | `DistanceSensor` | Distance reading in CM/MM/inches (not currently on the physical bench) |
| [`CameraComponent`](src/main/java/org/firstinspires/ftc/teamcode/components/CameraComponent.java) | `VisionPortal` + `AprilTagProcessor` | AprilTag detection — ID, range, bearing |

### `GamepadConfig.java`

A generic wrapper around a single `Gamepad`. It knows nothing about what's wired to it — no component references, no assumptions about what a button *does*. Every button method is **edge-detected**: it returns `true` for exactly one loop cycle per physical press, never on every cycle a button is held. This is a deliberate design choice (see the class doc for the reasoning) — there's no "is currently held" query, only "was just pressed."

```java
GamepadConfig driver = new GamepadConfig(gamepad1);
double power = driver.getLeftStickY();
if (driver.isAPressed()) { ... }   // fires once per press, not while held
```

## Example OpModes

All examples live in [`examples/`](src/main/java/org/firstinspires/ftc/teamcode/examples/) and extend `LinearOpMode`. They're organized in a rough difficulty progression:

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
| `Concept: AprilTag Aim Servo` | Webcam + Continuous Servo | A: enable/disable | Basic proportional (closed-loop) feedback control |
| `Concept: Coordinated Actuators` | DC Motor + Position Servo | A: Stow, B: Deploy | One trigger commanding multiple actuators toward matched presets |

## Getting Started

1. In the Driver Station's Robot Configuration, name each device to **exactly** match the strings in [`HardwareNames.java`](src/main/java/org/firstinspires/ftc/teamcode/HardwareNames.java).
2. Build and deploy this module to the Control Hub.
3. On the Driver Station, select an OpMode by its `@TeleOp` name (e.g. "Basic: Touch") — they're grouped as `Basic` or `Concept` in the OpMode list.
4. Start with Tier 0/1 (nothing moves) before running any Tier 2/3 OpMode.

## Hardware Notes & Gotchas

Real lessons learned testing this code on the physical bench — worth knowing before you extend this project:

- **The REV Color Sensor V3's LED is physically hardwired always-on.** Neither the legacy `ColorSensor.enableLed()` nor the `SwitchableLight` interface can turn it off — confirmed directly from the FTC SDK's own source. `ColorSensorComponent` no longer exposes LED control at all.
- **Not every "180° servo" actually is one.** This bench's positional servo measures closer to 300° in practice. `PositionServoComponent` takes an optional `maxAngleDegrees` constructor argument for exactly this reason — always verify against the real hardware rather than assuming.
- **`VisionPortal.resumeStreaming()`/`stopStreaming()` are asynchronous and can block.** Calling one before the other's prior operation completes makes it block the OpMode's main loop synchronously for a second or two — long enough to trip the Driver Station's watchdog and look like the robot crashed. Don't wire these to instantly-toggleable gamepad buttons.
- **The Driver Station's "Camera Stream" preview doesn't appear automatically** — even with a `VisionPortal` actively `STREAMING`, a driver has to manually open it from the three-dot overflow menu. If that menu option is missing entirely, check that the Driver Hub/Control Hub software versions match.
- **`GamepadConfig` has no "held" state, only edge-detected "just pressed."** A control meant to run continuously while a button is held (e.g. "hold A for full power") can't be built directly off these methods — the OpMode itself needs to latch a flag on press and clear it on release.

## Still To Do

A few more example ideas from the project's planning notes, not yet implemented: `ConceptColorGatesMotor`, `ConceptTouchToggleServo`, `ConceptColorSampleHold`, `ConceptAprilTagSelectPosition`, `ConceptMotorHoming`, `ConceptReversedMotor`, `ConceptTimedSequence`, and a `TestBenchFull` capstone that ties every component together.

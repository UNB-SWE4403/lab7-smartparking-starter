# lab7-smartparking-starter

**SWE 4403 / CS 4015 / CS 6075 — Software Architecture & Design Patterns**
**Lab 7 Fallback — Smart Parking Platform**
Winter 2026 · Dr. J. Cardenas-Barrera · University of New Brunswick

---

## Purpose

This repository is the **fallback starting point for Lab 7** if your team project repository is not accessible during the lab session. The instructions in the lab guide are identical whether you work here or in your own project — only the codebase you diagnose and refactor differs.

The system is a simplified version of the **Real-Time Smart Parking Platform** used as the running example throughout the Week 9 lectures. It is intentionally broken in the same ways the lectures described.

---

## What Is Here

```
src/smartparking/
    OccupancyEvent.java    — domain event (do not modify)
    SensorService.java     — THE PROBLEM: tight coupling + sync fan-out
    Analytics.java         — slow consumer (simulates 150 ms write)
    AlertsService.java     — unreliable consumer (throws on FAULT_ZONE)
    MobileApp.java         — consumer that should always receive events
    Main.java              — three observable scenarios

docs/
    coupling-diagnosis.md  — YOUR Part 1 deliverable (fill this in)
    decisions/             — YOUR Part 3 deliverable (create ADR here)
    atam-notes.md          — YOUR Part 4 deliverable (fill this in)
```

---

## Quick Start

No build tool required. Compile and run with plain `javac`:

```bash
# From the repository root
javac -d out src/smartparking/*.java
java  -cp out smartparking.Main
```

Expected output **before** your refactoring:

```
=== Scenario 1: Normal occupancy change ===
[SensorService] processing -> OccupancyEvent[spot=A-042, zone=LEVEL_1, occupied=true, ts=...]
[Analytics ] recorded  -> ...          ← takes ~150 ms
[AlertsService] evaluated -> ...
[MobileApp ] pushed    -> ...
[SensorService] fan-out complete in ~150 ms
Wall time: ~150 ms

=== Scenario 2: Event in FAULT_ZONE ===
[SensorService] processing -> OccupancyEvent[spot=B-017, zone=FAULT_ZONE, ...]
[Analytics ] recorded  -> ...
[Main] caught exception: [AlertsService] downstream failure for zone: FAULT_ZONE
[Main] -- did MobileApp receive the event? --
                                        ← MobileApp is SILENT — never received it

=== Scenario 3: Burst of three events ===
...
Total wall time for 3 events: ~450 ms  ← 150 ms × 3, fully sequential
```

---

## The Architectural Problems (Your Part 1 Diagnosis)

Read `SensorService.java` carefully. The comments in that file name the problems explicitly. Your `coupling-diagnosis.md` should identify **one** of the following coupling sites (the most direct one is the first):

| # | Coupling site | Composition to apply | QA harmed |
|---|--------------|----------------------|-----------|
| 1 | `SensorService` constructs its own consumers (`new Analytics()` etc.) | Observer + interface → inject `IOccupancyConsumer` from outside | Testability, Modifiability |
| 2 | `notifyAll()` calls consumers sequentially on the calling thread | Observer + async dispatch → each consumer runs independently | Latency, Reliability |

Pick **one**. You do not need to fix both in this lab.

---

## The Refactoring Target (Your Part 2 Code)

### Option A — Extract `IOccupancyConsumer` and inject (recommended)

The most direct fix for the direct-instantiation coupling:

```java
// 1. New interface
public interface IOccupancyConsumer {
    void onOccupancyEvent(OccupancyEvent event);
}

// 2. Each consumer implements it (rename existing method):
public class Analytics implements IOccupancyConsumer {
    @Override
    public void onOccupancyEvent(OccupancyEvent event) { ... }
}
// Same for AlertsService and MobileApp.

// 3. SensorService receives a list — no longer creates consumers:
public class SensorService {
    private final List<IOccupancyConsumer> consumers;

    public SensorService(List<IOccupancyConsumer> consumers) {
        this.consumers = consumers;
    }

    public void process(String spotId, String zone, boolean occupied) {
        OccupancyEvent event = new OccupancyEvent(spotId, zone, occupied);
        for (IOccupancyConsumer c : consumers) {
            c.onOccupancyEvent(event);
        }
    }
}

// 4. Main.java wires it up:
List<IOccupancyConsumer> consumers = List.of(
    new Analytics(), new AlertsService(), new MobileApp()
);
SensorService sensor = new SensorService(consumers);
```

### Option B — Introduce isolated dispatch (harder, stronger)

If you want to address the reliability problem as well, wrap each consumer call in a try/catch so that one failure does not abort the others:

```java
for (IOccupancyConsumer c : consumers) {
    try {
        c.onOccupancyEvent(event);
    } catch (Exception ex) {
        System.err.println("[SensorService] consumer " + c.getClass().getSimpleName()
                + " failed: " + ex.getMessage() + " — continuing.");
    }
}
```

This is not async, but it eliminates the reliability problem without adding concurrency complexity.

---

## Deliverables Reminder

By the end of the lab session, your fork must contain four committed files:

| File | Part |
|------|------|
| `docs/coupling-diagnosis.md` | Part 1 |
| Modified `src/smartparking/` files | Part 2 |
| `docs/decisions/adr-XXX-title.md` | Part 3 |
| `docs/atam-notes.md` | Part 4 |

Commit at the end of each part — do not push everything at once at the end.

---

## Acceptance Check After Refactoring

Run `Main` again. If Option A is correctly implemented:

- Scenario 1 still works: all three consumers receive the event.
- Scenario 2: `AlertsService` still throws, but the exception is handled gracefully (Option B) or propagates to `Main` without silencing `MobileApp` if you added per-consumer isolation.
- Scenario 3: wall time is still ~450 ms if you used synchronous dispatch — that is acceptable. The Modifiability gain is the goal, not latency.

If your ADR claims a latency benefit, your QA scenarios in Part 4 must reflect what the code actually achieves. Do not claim async behaviour if you implemented synchronous dispatch.

---

## Academic Integrity

This repository is seeded with deliberate architectural problems for educational purposes. You are expected to:

- Diagnose the problems yourself (the comments guide you; they do not do the work for you).
- Write original ADR and QA scenario text.
- Not copy solutions from other students or generative tools without understanding them.

University of New Brunswick Academic Integrity Policy applies.

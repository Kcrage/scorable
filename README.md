# CPU Scheduling Simulator

A Java Swing desktop application that demonstrates the four classic **CPU scheduling algorithms** commonly asked about in operating-system technical interviews.

---

## Algorithms Implemented

### 1. First Come First Serve (FCFS)

> "Whoever arrived first gets the CPU first."

- Processes are sorted by **arrival time** and run in that order.
- **Non-preemptive**: once a process starts it runs to completion.
- Simple but can cause the **convoy effect** — short processes stuck behind a long one.

**Example** (arrival=0, burst=6 | arrival=1, burst=4 | arrival=2, burst=8):
```
| P1(6) | P2(4) | P3(8) |
0       6      10      18
```

---

### 2. Shortest Job First (SJF)

> "Among all ready processes, run the shortest one next."

- At each scheduling point the **ready process with the smallest burst time** is chosen.
- **Non-preemptive**: the current process finishes before the next decision is made.
- Provably optimal for **minimum average waiting time** (for non-preemptive, batch systems).
- Suffers from **starvation** — long processes may wait indefinitely.
- Requires knowing burst time in advance (not feasible in real OSes without prediction).

---

### 3. Priority Scheduling

> "The most important process runs first."

- Each process has a **priority number**. A **lower number = higher priority** (priority 1 runs before priority 5).
- **Non-preemptive**: the selected process runs to completion.
- Can cause **starvation** for low-priority processes.
- Used in real operating systems (e.g., real-time tasks get higher priority).

---

### 4. Round Robin (RR)

> "Everyone gets a fair, equal time slice."

- Processes are kept in a **FIFO ready queue**.
- Each process runs for at most **Q time units** (the time quantum).
- If not finished, it is **preempted** and moved to the back of the queue.
- **Fair** — no process waits more than `(n−1) × Q` units.
- **Response time** is good for interactive systems.
- Choosing the right quantum is key: too small → too many context switches; too large → degenerates to FCFS.

---

## Project Structure

```
src/
└── cpuscheduler/
    ├── Process.java            # Data model: PID, arrival, burst, priority
    ├── GanttEntry.java         # One block on the Gantt chart (process + time range)
    ├── Scheduler.java          # Abstract base class with shared utilities
    ├── FCFSScheduler.java      # First Come First Serve implementation
    ├── SJFScheduler.java       # Shortest Job First implementation
    ├── PriorityScheduler.java  # Priority Scheduling implementation
    ├── RoundRobinScheduler.java# Round Robin implementation
    ├── SimulationThread.java   # Runs the scheduler on a background thread
    └── SchedulerGUI.java       # Main Swing window (entry point: main())
.vscode/
├── launch.json                 # "Run CPU Scheduling Simulator" launch config
└── settings.json               # Java source/output paths for VS Code
README.md
```

---

## How to Run

### Prerequisites

- **Java 11+** (Java 17 recommended)
- **VS Code** with the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) installed

### Option A — VS Code (recommended)

1. Open the repository folder in VS Code.
2. Wait for the Java extension to index the project (bottom status bar shows "Java: Ready").
3. Open **Run and Debug** (`Ctrl+Shift+D`), select **"Run CPU Scheduling Simulator"**, and press **▶**.

### Option B — Command Line

```bash
# Compile all Java files
javac -d bin src/cpuscheduler/*.java

# Run the GUI
java -cp bin cpuscheduler.SchedulerGUI
```

---

## Using the Application

1. **Enter processes** in the table (Process ID, Arrival Time, Burst Time, Priority).
   - Click **Add Process** to add a row; **Remove Selected** to delete one.
2. **Choose an algorithm** from the dropdown.
3. If **Round Robin** is selected, enter a **Time Quantum** in the input box.
4. Click **▶ Run Simulation**.
5. The **Gantt Chart** visualises execution order.
6. The **Results** table shows per-process Waiting Time and Turnaround Time, plus averages.

---

## Key Formulas

| Metric | Formula |
|---|---|
| Turnaround Time | `Finish Time − Arrival Time` |
| Waiting Time    | `Turnaround Time − Burst Time` |
| Average WT      | `ΣWaiting Time / n` |
| Average TAT     | `ΣTurnaround Time / n` |

---

## OOP Design

| Class | Role |
|---|---|
| `Process` | Plain data object (encapsulates all per-process state) |
| `Scheduler` | Abstract base — defines the contract and provides shared helpers |
| `FCFSScheduler` etc. | Concrete implementations — each overrides `schedule()` |
| `GanttEntry` | Immutable value object representing one Gantt bar |
| `SimulationThread` | Separates concurrency from UI logic (Single Responsibility) |
| `SchedulerGUI` | Swing window — only handles presentation and user events |

---

## License

MIT — see [LICENSE](LICENSE).

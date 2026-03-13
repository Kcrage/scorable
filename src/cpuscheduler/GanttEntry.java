package cpuscheduler;

/**
 * Represents one block on the Gantt chart.
 * Each entry records which process ran and during what time interval.
 *
 * Example:
 *   | P1 | P2 | P1 | IDLE |
 *     0    3    6    10
 *
 * A "processId" of "IDLE" is used when the CPU is idle (no processes ready).
 */
public class GanttEntry {

    private final String processId; // Name of the process (or "IDLE")
    private final int startTime;    // Time this slot begins
    private final int endTime;      // Time this slot ends

    public GanttEntry(String processId, int startTime, int endTime) {
        this.processId = processId;
        this.startTime = startTime;
        this.endTime   = endTime;
    }

    public String getProcessId() { return processId; }
    public int    getStartTime() { return startTime; }
    public int    getEndTime()   { return endTime; }

    @Override
    public String toString() {
        return String.format("[%s: %d-%d]", processId, startTime, endTime);
    }
}

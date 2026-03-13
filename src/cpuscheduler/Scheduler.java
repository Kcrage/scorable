package cpuscheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all CPU scheduling algorithms.
 *
 * Each concrete scheduler must implement the {@link #schedule(List)} method,
 * which takes a list of processes, simulates the CPU execution, and returns
 * an ordered list of Gantt chart entries.
 *
 * After calling schedule(), the waiting time and turnaround time fields
 * of every Process object in the list are populated.
 */
public abstract class Scheduler {

    /**
     * Run the scheduling algorithm on the given list of processes.
     *
     * @param processes  List of Process objects (must not be empty).
     * @return           Ordered list of GanttEntry blocks representing execution.
     */
    public abstract List<GanttEntry> schedule(List<Process> processes);

    /**
     * Utility: compute and set waitingTime and turnaroundTime for each process.
     *
     * Formula:
     *   Turnaround Time = Finish Time - Arrival Time
     *   Waiting Time    = Turnaround Time - Burst Time
     */
    protected void calculateTimes(List<Process> processes) {
        for (Process p : processes) {
            int tat = p.getFinishTime() - p.getArrivalTime();
            int wt  = tat - p.getBurstTime();
            p.setTurnaroundTime(tat);
            p.setWaitingTime(wt);
        }
    }

    /**
     * Utility: compute average waiting time across all processes.
     */
    public double averageWaitingTime(List<Process> processes) {
        if (processes.isEmpty()) return 0;
        double total = 0;
        for (Process p : processes) total += p.getWaitingTime();
        return total / processes.size();
    }

    /**
     * Utility: compute average turnaround time across all processes.
     */
    public double averageTurnaroundTime(List<Process> processes) {
        if (processes.isEmpty()) return 0;
        double total = 0;
        for (Process p : processes) total += p.getTurnaroundTime();
        return total / processes.size();
    }

    /**
     * Helper: reset all processes so the same list can be re-scheduled.
     */
    protected void resetProcesses(List<Process> processes) {
        for (Process p : processes) p.reset();
    }

    /**
     * Helper: build a result summary string showing all per-process metrics
     * plus the averages.
     */
    public String buildResultSummary(List<Process> processes) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-10s %-10s %-10s %-15s %-15s%n",
                "PID", "Arrival", "Burst", "Priority", "Waiting", "Turnaround"));
        sb.append("-".repeat(66)).append("\n");
        for (Process p : processes) {
            sb.append(String.format("%-6s %-10d %-10d %-10d %-15d %-15d%n",
                    p.getProcessId(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getPriority(),
                    p.getWaitingTime(),
                    p.getTurnaroundTime()));
        }
        sb.append("-".repeat(66)).append("\n");
        sb.append(String.format("Average Waiting Time    : %.2f%n", averageWaitingTime(processes)));
        sb.append(String.format("Average Turnaround Time : %.2f%n", averageTurnaroundTime(processes)));
        return sb.toString();
    }
}

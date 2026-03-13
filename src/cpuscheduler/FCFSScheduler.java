package cpuscheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * First Come First Serve (FCFS) Scheduling Algorithm.
 *
 * ALGORITHM:
 *   - Processes are executed in the order they arrive (sorted by arrival time).
 *   - Non-preemptive: once a process starts, it runs to completion.
 *   - If the CPU is idle (no process has arrived yet), it fast-forwards to the
 *     next arriving process.
 *
 * PROS:  Simple to implement, fair in terms of arrival order.
 * CONS:  Can cause the "convoy effect" where short processes wait behind
 *        a long process.
 *
 * TIME COMPLEXITY: O(n log n) for sorting by arrival time.
 */
public class FCFSScheduler extends Scheduler {

    @Override
    public List<GanttEntry> schedule(List<Process> processes) {
        // Reset any previous simulation data
        resetProcesses(processes);

        // Sort processes by arrival time (ties broken by process ID for determinism)
        List<Process> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt(Process::getArrivalTime)
                              .thenComparing(Process::getProcessId));

        List<GanttEntry> gantt = new ArrayList<>();
        int currentTime = 0;

        for (Process p : sorted) {
            // If the CPU is idle before this process arrives, record an IDLE block
            if (currentTime < p.getArrivalTime()) {
                gantt.add(new GanttEntry("IDLE", currentTime, p.getArrivalTime()));
                currentTime = p.getArrivalTime();
            }

            // Record the start time for this process
            p.setStartTime(currentTime);

            // The process runs from currentTime to currentTime + burstTime
            int finishTime = currentTime + p.getBurstTime();
            p.setFinishTime(finishTime);
            gantt.add(new GanttEntry(p.getProcessId(), currentTime, finishTime));

            currentTime = finishTime;
        }

        // Calculate waiting and turnaround times for all processes
        calculateTimes(processes);
        return gantt;
    }
}

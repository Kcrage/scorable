package cpuscheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Priority Scheduling Algorithm — Non-Preemptive.
 *
 * ALGORITHM:
 *   - Each process is assigned a numeric priority.  In this implementation,
 *     a LOWER number means HIGHER priority (common convention, e.g., priority 1
 *     runs before priority 5).
 *   - At each scheduling point the highest-priority ready process is selected.
 *   - Non-preemptive: the running process is never interrupted.
 *   - Tie-breaking: if two processes share the same priority, the one with the
 *     earlier arrival time wins; if that's also tied, process ID is used.
 *
 * PROS:  Allows important processes to run first.
 * CONS:  Starvation — low-priority processes may never get the CPU if
 *        high-priority processes keep arriving.
 *
 * TIME COMPLEXITY: O(n²) in the selection loop (fine for small n).
 */
public class PriorityScheduler extends Scheduler {

    @Override
    public List<GanttEntry> schedule(List<Process> processes) {
        resetProcesses(processes);

        List<Process> remaining = new ArrayList<>(processes);
        remaining.sort(Comparator.comparingInt(Process::getArrivalTime)
                                 .thenComparing(Process::getProcessId));

        List<GanttEntry> gantt = new ArrayList<>();
        int currentTime = 0;
        int completed = 0;
        int n = processes.size();

        while (completed < n) {
            // Collect all processes that have arrived by currentTime
            List<Process> ready = new ArrayList<>();
            for (Process p : remaining) {
                if (p.getArrivalTime() <= currentTime) {
                    ready.add(p);
                }
            }

            if (ready.isEmpty()) {
                // CPU idle — jump to the next arriving process
                int nextArrival = remaining.stream()
                        .mapToInt(Process::getArrivalTime).min().orElse(currentTime + 1);
                gantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;
                continue;
            }

            // Select the highest-priority process (lowest priority number)
            ready.sort(Comparator.comparingInt(Process::getPriority)
                                 .thenComparingInt(Process::getArrivalTime)
                                 .thenComparing(Process::getProcessId));
            Process selected = ready.get(0);

            // Run the selected process to completion
            selected.setStartTime(currentTime);
            int finishTime = currentTime + selected.getBurstTime();
            selected.setFinishTime(finishTime);
            gantt.add(new GanttEntry(selected.getProcessId(), currentTime, finishTime));

            currentTime = finishTime;
            remaining.remove(selected);
            completed++;
        }

        calculateTimes(processes);
        return gantt;
    }
}

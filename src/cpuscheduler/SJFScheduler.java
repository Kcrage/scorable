package cpuscheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shortest Job First (SJF) Scheduling Algorithm — Non-Preemptive.
 *
 * ALGORITHM:
 *   - At each scheduling decision point, the process with the shortest burst
 *     time among all arrived (ready) processes is selected next.
 *   - Non-preemptive: the running process is never interrupted.
 *   - If multiple processes have the same burst time, arrival order is used
 *     as the tiebreaker.
 *
 * PROS:  Minimises average waiting time (optimal for non-preemptive case).
 * CONS:  Starvation — long processes may never run if short ones keep arriving.
 *        Burst time must be known in advance (not practical in real OSes).
 *
 * TIME COMPLEXITY: O(n²) in the naive selection loop (acceptable for small n).
 */
public class SJFScheduler extends Scheduler {

    @Override
    public List<GanttEntry> schedule(List<Process> processes) {
        resetProcesses(processes);

        // Work on a copy so we can remove completed processes
        List<Process> remaining = new ArrayList<>(processes);
        // Sort initially by arrival time so we can correctly add to the ready queue
        remaining.sort(Comparator.comparingInt(Process::getArrivalTime)
                                 .thenComparing(Process::getProcessId));

        List<GanttEntry> gantt = new ArrayList<>();
        int currentTime = 0;
        int completed = 0;
        int n = processes.size();

        while (completed < n) {
            // Build the ready queue: all processes that have arrived by currentTime
            List<Process> ready = new ArrayList<>();
            for (Process p : remaining) {
                if (p.getArrivalTime() <= currentTime) {
                    ready.add(p);
                }
            }

            if (ready.isEmpty()) {
                // No process ready — CPU is idle; advance time to next arrival
                int nextArrival = remaining.stream()
                        .mapToInt(Process::getArrivalTime).min().orElse(currentTime + 1);
                gantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;
                continue;
            }

            // Pick the process with the shortest burst time
            ready.sort(Comparator.comparingInt(Process::getBurstTime)
                                 .thenComparingInt(Process::getArrivalTime)
                                 .thenComparing(Process::getProcessId));
            Process selected = ready.get(0);

            // Execute the selected process to completion (non-preemptive)
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

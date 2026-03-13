package cpuscheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Round Robin (RR) Scheduling Algorithm.
 *
 * ALGORITHM:
 *   - Each process is given a fixed slice of CPU time called the time quantum (Q).
 *   - Processes are maintained in a FIFO ready queue.
 *   - If a process does not finish within its time quantum, it is preempted and
 *     moved to the back of the queue.
 *   - New arrivals are added to the ready queue in arrival-time order.
 *
 * PROS:  Fair — every process gets equal CPU time slices.
 *        Good response time for interactive systems.
 * CONS:  High context-switch overhead if the quantum is too small.
 *        Average waiting time can be higher than SJF.
 *
 * TIME COMPLEXITY: O(n * (maxBurstTime / quantum)) in the worst case.
 *
 * @param timeQuantum  Length of each CPU time slice (must be > 0).
 */
public class RoundRobinScheduler extends Scheduler {

    private final int timeQuantum;

    public RoundRobinScheduler(int timeQuantum) {
        if (timeQuantum <= 0) throw new IllegalArgumentException("Time quantum must be > 0");
        this.timeQuantum = timeQuantum;
    }

    public int getTimeQuantum() { return timeQuantum; }

    @Override
    public List<GanttEntry> schedule(List<Process> processes) {
        resetProcesses(processes);

        // Sort a copy by arrival time so we can feed processes into the queue on time
        List<Process> sortedByArrival = new ArrayList<>(processes);
        sortedByArrival.sort(Comparator.comparingInt(Process::getArrivalTime)
                                       .thenComparing(Process::getProcessId));

        List<GanttEntry> gantt = new ArrayList<>();
        Queue<Process> readyQueue = new LinkedList<>();

        int currentTime = 0;
        int idx = 0; // pointer into sortedByArrival

        // Seed the queue with all processes arriving at time 0
        while (idx < sortedByArrival.size()
                && sortedByArrival.get(idx).getArrivalTime() <= currentTime) {
            readyQueue.add(sortedByArrival.get(idx));
            idx++;
        }

        // Track whether a process has been started (to record startTime correctly)
        java.util.Set<String> started = new java.util.HashSet<>();

        while (!readyQueue.isEmpty()) {
            Process current = readyQueue.poll();

            // Record the first time this process gets the CPU
            if (!started.contains(current.getProcessId())) {
                current.setStartTime(currentTime);
                started.add(current.getProcessId());
            }

            // Determine how long this process runs in this slice
            int runTime = Math.min(timeQuantum, current.getRemainingBurstTime());
            int sliceEnd = currentTime + runTime;

            gantt.add(new GanttEntry(current.getProcessId(), currentTime, sliceEnd));
            current.setRemainingBurstTime(current.getRemainingBurstTime() - runTime);
            currentTime = sliceEnd;

            // Add any processes that arrived during this time slice to the queue
            while (idx < sortedByArrival.size()
                    && sortedByArrival.get(idx).getArrivalTime() <= currentTime) {
                readyQueue.add(sortedByArrival.get(idx));
                idx++;
            }

            if (current.getRemainingBurstTime() > 0) {
                // Process not finished — put it back at the end of the queue
                readyQueue.add(current);
            } else {
                // Process finished
                current.setFinishTime(currentTime);
            }

            // If the ready queue is empty but there are still unqueued processes,
            // the CPU must be idle until the next process arrives.
            if (readyQueue.isEmpty() && idx < sortedByArrival.size()) {
                int nextArrival = sortedByArrival.get(idx).getArrivalTime();
                gantt.add(new GanttEntry("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;
                // Add all processes that arrive at this new time
                while (idx < sortedByArrival.size()
                        && sortedByArrival.get(idx).getArrivalTime() <= currentTime) {
                    readyQueue.add(sortedByArrival.get(idx));
                    idx++;
                }
            }
        }

        calculateTimes(processes);
        return gantt;
    }
}

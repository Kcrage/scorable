package cpuscheduler;

/**
 * Represents a single process in the CPU scheduling simulation.
 * Stores both input data (arrival time, burst time, priority)
 * and calculated results (waiting time, turnaround time).
 */
public class Process {

    // --- Input fields (provided by the user) ---
    private String processId;
    private int arrivalTime;   // Time at which process arrives in the ready queue
    private int burstTime;     // CPU time required by the process
    private int priority;      // Lower number = higher priority (for Priority Scheduling)

    // --- Calculated fields (filled in by the scheduler) ---
    private int startTime;       // Time when process first gets the CPU
    private int finishTime;      // Time when process completes execution
    private int waitingTime;     // Time spent waiting in the ready queue
    private int turnaroundTime;  // Total time from arrival to completion

    // Remaining burst time used by Round Robin and SJF (non-preemptive tracking)
    private int remainingBurstTime;

    public Process(String processId, int arrivalTime, int burstTime, int priority) {
        this.processId = processId;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingBurstTime = burstTime;
    }

    // --- Getters ---
    public String getProcessId()       { return processId; }
    public int    getArrivalTime()     { return arrivalTime; }
    public int    getBurstTime()       { return burstTime; }
    public int    getPriority()        { return priority; }
    public int    getStartTime()       { return startTime; }
    public int    getFinishTime()      { return finishTime; }
    public int    getWaitingTime()     { return waitingTime; }
    public int    getTurnaroundTime()  { return turnaroundTime; }
    public int    getRemainingBurstTime() { return remainingBurstTime; }

    // --- Setters ---
    public void setStartTime(int startTime)           { this.startTime = startTime; }
    public void setFinishTime(int finishTime)         { this.finishTime = finishTime; }
    public void setWaitingTime(int waitingTime)       { this.waitingTime = waitingTime; }
    public void setTurnaroundTime(int turnaroundTime) { this.turnaroundTime = turnaroundTime; }
    public void setRemainingBurstTime(int t)          { this.remainingBurstTime = t; }

    /**
     * Resets calculated fields so the process can be re-simulated.
     */
    public void reset() {
        startTime = 0;
        finishTime = 0;
        waitingTime = 0;
        turnaroundTime = 0;
        remainingBurstTime = burstTime;
    }

    @Override
    public String toString() {
        return String.format("Process{id=%s, arrival=%d, burst=%d, priority=%d}",
                processId, arrivalTime, burstTime, priority);
    }
}

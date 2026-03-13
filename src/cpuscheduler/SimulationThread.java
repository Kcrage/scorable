package cpuscheduler;

import java.util.List;

/**
 * SimulationThread runs the chosen scheduling algorithm on a background thread
 * so the Swing UI never freezes.
 *
 * It implements Runnable so it can be passed directly to {@code new Thread(...)}.
 * When the simulation completes, it calls {@link SimulationCallback#onComplete}
 * on the Swing Event Dispatch Thread (EDT) via SwingUtilities.invokeLater.
 */
public class SimulationThread implements Runnable {

    private final Scheduler scheduler;
    private final List<Process> processes;
    private final SimulationCallback callback;

    public SimulationThread(Scheduler scheduler,
                            List<Process> processes,
                            SimulationCallback callback) {
        this.scheduler  = scheduler;
        this.processes  = processes;
        this.callback   = callback;
    }

    @Override
    public void run() {
        try {
            // Execute the scheduling algorithm (may be computationally intensive)
            List<GanttEntry> gantt = scheduler.schedule(processes);

            // Build the result summary text
            String summary = scheduler.buildResultSummary(processes);

            // Deliver results back to the EDT so the GUI can update safely
            javax.swing.SwingUtilities.invokeLater(() -> callback.onComplete(gantt, summary));

        } catch (Exception ex) {
            // Pass any error back to the GUI thread
            String error = "Simulation error: " + ex.getMessage();
            javax.swing.SwingUtilities.invokeLater(() -> callback.onError(error));
        }
    }

    /**
     * Callback interface implemented by the GUI to receive simulation results.
     */
    public interface SimulationCallback {
        void onComplete(List<GanttEntry> gantt, String summary);
        void onError(String errorMessage);
    }
}

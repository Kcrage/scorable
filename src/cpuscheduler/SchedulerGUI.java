package cpuscheduler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * SchedulerGUI — Main Swing window for the CPU Scheduling Simulator.
 *
 * Layout (top → bottom):
 *   1. Process input table  — users enter PID, Arrival, Burst, Priority
 *   2. Controls panel       — algorithm selector, quantum field, buttons
 *   3. Gantt chart panel    — colour-coded horizontal bars
 *   4. Results text area    — per-process metrics + averages
 */
public class SchedulerGUI extends JFrame {

    // ── Process table ───────────────────────────────────────────────────────
    private final DefaultTableModel tableModel;
    private final JTable processTable;

    // ── Controls ────────────────────────────────────────────────────────────
    private final JComboBox<String> algorithmCombo;
    private final JTextField        quantumField;
    private final JButton           addRowButton;
    private final JButton           removeRowButton;
    private final JButton           runButton;
    private final JButton           clearButton;

    // ── Output panels ───────────────────────────────────────────────────────
    private final GanttChartPanel ganttPanel;
    private final JTextArea       resultArea;

    // Palette used to colour Gantt blocks (cycles through these colours)
    private static final Color[] COLORS = {
        new Color(0x4A90D9), new Color(0xE67E22), new Color(0x27AE60),
        new Color(0x9B59B6), new Color(0xE74C3C), new Color(0x1ABC9C),
        new Color(0xF39C12), new Color(0x2980B9), new Color(0x8E44AD),
        new Color(0xD35400)
    };

    // ── Constructor ─────────────────────────────────────────────────────────

    public SchedulerGUI() {
        super("CPU Scheduling Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 700));

        // ── Table model (non-editable column headers, editable cells) ───────
        String[] columns = {"Process ID", "Arrival Time", "Burst Time", "Priority"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return true; }
        };
        processTable = new JTable(tableModel);
        processTable.setRowHeight(24);
        processTable.getTableHeader().setReorderingAllowed(false);

        // Pre-populate with sample data so users can try immediately
        addSampleData();

        // ── Algorithm selector ───────────────────────────────────────────────
        algorithmCombo = new JComboBox<>(new String[]{
            "First Come First Serve (FCFS)",
            "Shortest Job First (SJF)",
            "Priority Scheduling",
            "Round Robin (RR)"
        });

        quantumField = new JTextField("2", 4);
        quantumField.setToolTipText("Time quantum used by Round Robin");

        // Enable / disable quantum field depending on algorithm choice
        algorithmCombo.addActionListener(e -> {
            boolean isRR = algorithmCombo.getSelectedIndex() == 3;
            quantumField.setEnabled(isRR);
        });
        quantumField.setEnabled(false); // disabled initially (FCFS selected)

        // ── Buttons ──────────────────────────────────────────────────────────
        addRowButton    = new JButton("Add Process");
        removeRowButton = new JButton("Remove Selected");
        runButton       = new JButton("▶  Run Simulation");
        clearButton     = new JButton("Clear Results");

        runButton.setBackground(new Color(0x27AE60));
        runButton.setForeground(Color.WHITE);
        runButton.setFocusPainted(false);
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD, 13f));

        // ── Gantt chart panel ────────────────────────────────────────────────
        ganttPanel = new GanttChartPanel();
        ganttPanel.setPreferredSize(new Dimension(800, 80));

        // ── Result text area ──────────────────────────────────────────────────
        resultArea = new JTextArea(10, 60);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setBackground(new Color(0xF8F8F8));
        resultArea.setBorder(new EmptyBorder(6, 8, 6, 8));

        // ── Wire up button actions ─────────────────────────────────────────
        addRowButton.addActionListener(this::onAddRow);
        removeRowButton.addActionListener(this::onRemoveRow);
        runButton.addActionListener(this::onRun);
        clearButton.addActionListener(e -> {
            ganttPanel.clear();
            resultArea.setText("");
        });

        // ── Compose the layout ─────────────────────────────────────────────
        setLayout(new BorderLayout(8, 8));
        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null); // centre on screen
    }

    // ── Layout helpers ────────────────────────────────────────────────────────

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 0, 8));

        // Process table inside a scroll pane
        JScrollPane tableScroll = new JScrollPane(processTable);
        tableScroll.setPreferredSize(new Dimension(800, 160));
        TitledBorder tb = BorderFactory.createTitledBorder("Processes");
        tb.setTitleFont(tb.getTitleFont().deriveFont(Font.BOLD));
        tableScroll.setBorder(tb);

        // Row management buttons
        JPanel rowButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        rowButtons.add(addRowButton);
        rowButtons.add(removeRowButton);

        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(rowButtons,  BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(0, 8, 8, 8));

        // Controls row
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        controls.add(new JLabel("Algorithm:"));
        controls.add(algorithmCombo);
        controls.add(Box.createHorizontalStrut(10));
        controls.add(new JLabel("Time Quantum:"));
        controls.add(quantumField);
        controls.add(Box.createHorizontalStrut(20));
        controls.add(runButton);
        controls.add(clearButton);
        TitledBorder ctb = BorderFactory.createTitledBorder("Configuration");
        ctb.setTitleFont(ctb.getTitleFont().deriveFont(Font.BOLD));
        controls.setBorder(ctb);

        // Gantt chart
        JScrollPane ganttScroll = new JScrollPane(ganttPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ganttScroll.setPreferredSize(new Dimension(800, 100));
        TitledBorder gtb = BorderFactory.createTitledBorder("Gantt Chart");
        gtb.setTitleFont(gtb.getTitleFont().deriveFont(Font.BOLD));
        ganttScroll.setBorder(gtb);

        // Results
        JScrollPane resultScroll = new JScrollPane(resultArea);
        TitledBorder rtb = BorderFactory.createTitledBorder("Results");
        rtb.setTitleFont(rtb.getTitleFont().deriveFont(Font.BOLD));
        resultScroll.setBorder(rtb);

        // Stack controls / Gantt / results vertically
        JPanel inner = new JPanel(new BorderLayout(6, 6));
        inner.add(controls,    BorderLayout.NORTH);
        inner.add(ganttScroll, BorderLayout.CENTER);
        inner.add(resultScroll, BorderLayout.SOUTH);

        panel.add(inner, BorderLayout.CENTER);
        return panel;
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    private void onAddRow(ActionEvent e) {
        int rowCount = tableModel.getRowCount() + 1;
        tableModel.addRow(new Object[]{"P" + rowCount, 0, 1, 1});
    }

    private void onRemoveRow(ActionEvent e) {
        int selectedRow = processTable.getSelectedRow();
        if (selectedRow >= 0) {
            tableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to remove.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onRun(ActionEvent e) {
        // Parse the process table
        List<Process> processes;
        try {
            processes = parseProcesses();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid input: " + ex.getMessage() + "\nPlease enter integers only.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (processes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one process.",
                    "No Processes", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build the chosen scheduler
        Scheduler scheduler;
        int algoIndex = algorithmCombo.getSelectedIndex();
        try {
            scheduler = buildScheduler(algoIndex);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable run button while the simulation is in progress
        runButton.setEnabled(false);
        resultArea.setText("Running simulation…");
        ganttPanel.clear();

        // Run on a background thread
        SimulationThread sim = new SimulationThread(scheduler, processes,
                new SimulationThread.SimulationCallback() {
            @Override
            public void onComplete(List<GanttEntry> gantt, String summary) {
                ganttPanel.setGantt(gantt, COLORS);
                resultArea.setText(summary);
                runButton.setEnabled(true);
            }
            @Override
            public void onError(String errorMessage) {
                resultArea.setText(errorMessage);
                runButton.setEnabled(true);
            }
        });
        new Thread(sim, "SimulationThread").start();
    }

    // ── Helper: parse table rows into Process objects ──────────────────────

    private List<Process> parseProcesses() {
        List<Process> list = new ArrayList<>();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String pid     = tableModel.getValueAt(row, 0).toString().trim();
            int arrival    = Integer.parseInt(tableModel.getValueAt(row, 1).toString().trim());
            int burst      = Integer.parseInt(tableModel.getValueAt(row, 2).toString().trim());
            int priority   = Integer.parseInt(tableModel.getValueAt(row, 3).toString().trim());

            if (pid.isEmpty()) {
                throw new NumberFormatException("Process ID in row " + (row + 1) + " is empty");
            }
            if (burst <= 0) {
                throw new NumberFormatException("Burst time must be > 0 (row " + (row + 1) + ")");
            }
            if (arrival < 0) {
                throw new NumberFormatException("Arrival time must be >= 0 (row " + (row + 1) + ")");
            }
            list.add(new Process(pid, arrival, burst, priority));
        }
        return list;
    }

    // ── Helper: instantiate the correct scheduler ──────────────────────────

    private Scheduler buildScheduler(int algoIndex) {
        switch (algoIndex) {
            case 0: return new FCFSScheduler();
            case 1: return new SJFScheduler();
            case 2: return new PriorityScheduler();
            case 3: {
                String qtStr = quantumField.getText().trim();
                int qt;
                try {
                    qt = Integer.parseInt(qtStr);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Time quantum must be an integer.");
                }
                if (qt <= 0) throw new IllegalArgumentException("Time quantum must be > 0.");
                return new RoundRobinScheduler(qt);
            }
            default: return new FCFSScheduler();
        }
    }

    // ── Sample data pre-loaded on startup ──────────────────────────────────

    private void addSampleData() {
        tableModel.addRow(new Object[]{"P1", 0, 6, 3});
        tableModel.addRow(new Object[]{"P2", 1, 4, 1});
        tableModel.addRow(new Object[]{"P3", 2, 8, 4});
        tableModel.addRow(new Object[]{"P4", 3, 3, 2});
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Inner class: GanttChartPanel
    //  Renders the Gantt chart as colour-coded horizontal bars with labels.
    // ═══════════════════════════════════════════════════════════════════════

    private static class GanttChartPanel extends JPanel {

        private List<GanttEntry> gantt = new ArrayList<>();
        private Color[] palette        = new Color[0];

        // Maps process ID → colour index for consistent colours across slices
        private final java.util.Map<String, Integer> colorMap = new java.util.HashMap<>();
        private int colorCounter = 0;

        GanttChartPanel() {
            setBackground(Color.WHITE);
        }

        void setGantt(List<GanttEntry> gantt, Color[] palette) {
            this.gantt   = gantt;
            this.palette = palette;
            colorMap.clear();
            colorCounter = 0;
            // Assign stable colours
            for (GanttEntry entry : gantt) {
                colorMap.computeIfAbsent(entry.getProcessId(), k -> {
                    if (k.equals("IDLE")) return -1; // special colour for IDLE
                    int idx = colorCounter % palette.length;
                    colorCounter++;
                    return idx;
                });
            }
            // Resize panel to fit all bars
            if (!gantt.isEmpty()) {
                int totalTime = gantt.get(gantt.size() - 1).getEndTime();
                int width = Math.max(800, totalTime * 50 + 60);
                setPreferredSize(new Dimension(width, 80));
                revalidate();
            }
            repaint();
        }

        void clear() {
            gantt = new ArrayList<>();
            colorMap.clear();
            colorCounter = 0;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (gantt == null || gantt.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Dimensions
            int barHeight = 36;
            int barY      = 10;
            int labelY    = barY + barHeight + 14; // y-position for time labels

            int totalTime = gantt.get(gantt.size() - 1).getEndTime();
            int panelW    = getWidth() - 20;
            double scale  = (double) panelW / totalTime; // pixels per time unit

            Font barFont   = new Font(Font.SANS_SERIF, Font.BOLD, 11);
            Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
            g2.setFont(barFont);

            for (GanttEntry entry : gantt) {
                int x1 = 10 + (int) (entry.getStartTime() * scale);
                int x2 = 10 + (int) (entry.getEndTime()   * scale);
                int w  = Math.max(x2 - x1, 1);

                // Background colour
                Color fill;
                if ("IDLE".equals(entry.getProcessId())) {
                    fill = new Color(0xBDC3C7); // grey for idle
                } else {
                    int idx = colorMap.getOrDefault(entry.getProcessId(), 0);
                    fill = palette[idx % palette.length];
                }

                g2.setColor(fill);
                g2.fillRect(x1, barY, w, barHeight);

                // Border
                g2.setColor(Color.WHITE);
                g2.drawRect(x1, barY, w, barHeight);

                // Process label inside the bar
                g2.setColor(Color.WHITE);
                g2.setFont(barFont);
                FontMetrics fm = g2.getFontMetrics();
                String label = entry.getProcessId();
                int textX = x1 + (w - fm.stringWidth(label)) / 2;
                int textY = barY + (barHeight + fm.getAscent() - fm.getDescent()) / 2;
                if (w > fm.stringWidth(label) + 4) {
                    g2.drawString(label, textX, textY);
                }

                // Start time tick below the bar
                g2.setFont(labelFont);
                g2.setColor(Color.DARK_GRAY);
                String startStr = String.valueOf(entry.getStartTime());
                g2.drawString(startStr, x1, labelY);
            }

            // Draw the last end-time tick
            if (!gantt.isEmpty()) {
                GanttEntry last = gantt.get(gantt.size() - 1);
                int xLast = 10 + (int) (last.getEndTime() * scale);
                g2.setFont(labelFont);
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(String.valueOf(last.getEndTime()), xLast, labelY);
            }
        }
    }

    // ── Entry point ────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Run on the Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Use the system look and feel for a native appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }

            SchedulerGUI gui = new SchedulerGUI();
            gui.setVisible(true);
        });
    }
}

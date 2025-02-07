package tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The pullSimulator class simulates a "gacha" pull system with pity rules for 4★ and 5★ items.
 * - 5★ has a soft pity escalation starting from pull #66.
 * - 4★ has a hard pity at every 10th pull if no 4★ or 5★ has been obtained in the previous 9 pulls.
 * - A 50-50 system for a "featured" 5★: if you fail once (non-featured 5★),
 *   the next 5★ is guaranteed featured.
 *
 * This class also provides a Swing UI with:
 *   - "Resonate 1"
 *   - "Resonate 10"
 *   - "History" (opens a new window with highlighted history and statistics).
 *
 * All "4★" results are shown in bold purple, and "5★"/"up!5★" in bold orange.
 * Implements the 'tool' interface from your project.
 */
public class pullSimulator implements tool {

    // ========== Original Fields and Logic ==========

    // Base rates for 4★ and 5★
    private double base_4_rate = 0.085;
    private double base_5_rate = 0.008;

    // Probability that a 5★ is featured. If you lose once (non-featured 5★),
    // the next 5★ is guaranteed featured (featured_rate=1).
    // After pulling a featured 5★, featured_rate goes back to 0.5.
    private double featured_rate = 0.5;

    // Pity counters:
    //  - counter_4: Number of consecutive pulls with no 4★ or 5★
    //  - counter_5: Number of consecutive pulls with no 5★
    private int counter_4;
    private int counter_5;

    // Random instance for generating probabilities
    private Random random = new Random();

    // Record of all pull outcomes (cumulative)
    private List<String> history = new ArrayList<>();

    // Record of only the most recent batch of pulls
    private List<String> lastPullResults = new ArrayList<>();

    // ========== New UI Fields ==========

    /** Main panel to be inserted in a tab of your larger application. */
    private JPanel mainPanel;

    /**
     * Default constructor that initializes all counters to zero
     * and sets up the UI.
     */
    public pullSimulator() {
        this.counter_4 = 0;
        this.counter_5 = 0;
        setupUI();
    }

    // ========== Original Core Methods (unchanged) ==========

    /**
     * Increments both counters (4★, 5★). Used when a pull results in 3★.
     */
    private void addCount() {
        this.counter_4++;
        this.counter_5++;
    }

    /**
     * Resets the 4★ pity counter to zero.
     */
    private void reset_4() {
        this.counter_4 = 0;
    }

    /**
     * Resets the 5★ pity counter to zero.
     */
    private void reset_5() {
        this.counter_5 = 0;
    }

    /**
     * Calculates the current 5★ probability based on "soft pity".
     */
    private double get5Rate() {
        int softPity = counter_5;

        if (softPity <= 65) {
            return base_5_rate;
        } else if (softPity < 76) {
            // from 66 to 75
            double rate = base_5_rate + 0.08 * (softPity - 65);
            return Math.min(1.0, rate);
        } else if (softPity < 79) {
            // from 76 to 78
            // 0.8 = 0.08 * (75 - 65) is the total increment from the previous range
            double rate = base_5_rate + 0.8 + 0.1 * (softPity - 75);
            return Math.min(1.0, rate);
        } else {
            // Pull #79 guaranteed
            return 1.0;
        }
    }

    /**
     * Calculates the current 4★ probability based on "hard pity".
     */
    private double get4Rate() {
        // If we are at 9 consecutive misses, then the next (10th) is forced 4★ if not 5★
        return (counter_4 < 9) ? base_4_rate : 1.0;
    }

    /**
     * Simulates a number of pulls and updates the history.
     *
     * @param pulls the number of pulls to simulate
     */
    public void pull(int pulls) {
        // Clear the previous pull batch record
        lastPullResults.clear();

        for (int i = 0; i < pulls; i++) {
            double probability = random.nextDouble();
            double chance5 = get5Rate();

            if (probability < chance5) {
                // We pulled a 5★
                double probFeatured = random.nextDouble();
                if (probFeatured < featured_rate) {
                    // Featured 5★
                    lastPullResults.add("up!5★");
                    history.add("up!5★");

                    // Reset featured rate to 0.5
                    featured_rate = 0.5;
                } else {
                    // Non-featured 5★, next 5★ is guaranteed featured
                    lastPullResults.add("5★");
                    history.add("5★");

                    featured_rate = 1.0;
                }

                // Reset 5★ and 4★ counters
                reset_5();
                reset_4();

            } else {
                // Check 4★
                double chance4 = get4Rate();

                if (probability < (chance5 + chance4)) {
                    // 4★
                    lastPullResults.add("4★");
                    history.add("4★");

                    reset_4();
                    // We got a 4★, so keep counting up for 5★ pity
                    counter_5++;

                } else {
                    // 3★
                    lastPullResults.add("3★");
                    history.add("3★");

                    addCount();
                }
            }
        }
    }

    /**
     * Returns the record of the most recent batch of pulls.
     */
    public List<String> result() {
        return lastPullResults;
    }

    /**
     * Clears all pulling history and resets pity counters and featured rate.
     */
    public void resetHistory() {
        history.clear();
        lastPullResults.clear();
        counter_4 = 0;
        counter_5 = 0;
        featured_rate = 0.5;
    }

    /**
     * Returns the cumulative record of all pulls so far.
     */
    public List<String> getHistory() {
        return history;
    }

    // ========== New UI-Related Methods ==========

    /**
     * Sets up the Swing UI for this simulator: three buttons ("Resonate 1", "Resonate 10", "History").
     */
    private void setupUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Resonate Simulation");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button 1: Resonate 1
        JButton resonate1Btn = new JButton("Resonate 1");
        resonate1Btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resonate1Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pull(1);  // use existing logic

                // For the popup, highlight the single result with HTML
                if (!lastPullResults.isEmpty()) {
                    String highlighted = getHighlightedResult(lastPullResults.get(0));
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "<html>You pulled: " + highlighted + "</html>",
                            "Resonate 1",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
                else {
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "No pull result.",
                            "Resonate 1",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        // Button 2: Resonate 10
        JButton resonate10Btn = new JButton("Resonate 10");
        resonate10Btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resonate10Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pull(10); // use existing logic

                // Show a popup with all 10 results, each highlighted
                StringBuilder sb = new StringBuilder("<html>");
                for (String item : lastPullResults) {
                    sb.append(getHighlightedResult(item)).append("<br/>");
                }
                sb.append("</html>");

                JOptionPane.showMessageDialog(
                        mainPanel,
                        sb.toString(),
                        "Resonate 10",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        // Button 3: History (opens a new window)
        JButton historyBtn = new JButton("History");
        historyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        historyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHistoryWindow();
            }
        });

        // Add everything to the panel
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(resonate1Btn);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(resonate10Btn);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(historyBtn);
    }

    /**
     * Opens a new window showing the entire pull history with:
     *  - 4★ in bold purple
     *  - 5★ or up!5★ in bold orange
     * Also shows total statistics (total pulls, # of 3★, 4★, 5★, up!5★) on the left,
     * while the history list is on the right in a scrollable panel.
     */
    private void showHistoryWindow() {
        JFrame historyFrame = new JFrame("Pull History");
        historyFrame.setSize(600, 400);
        historyFrame.setLocationRelativeTo(mainPanel);
        historyFrame.setLayout(new BorderLayout());

        // ========== Statistics Panel on the LEFT ==========

        // Gather stats
        int total = history.size();
        int count3 = 0, count4 = 0, count5 = 0, countUp5 = 0;
        for (String pullResult : history) {
            switch (pullResult) {
                case "3★":    count3++;   break;
                case "4★":    count4++;   break;
                case "5★":    count5++;   break;
                case "up!5★": countUp5++; break;
            }
        }

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel totalLabel = new JLabel("Total Pulls: " + total);
        JLabel threeLabel = new JLabel("3-Star: " + count3);
        JLabel fourLabel  = new JLabel("4-Star: " + count4);
        JLabel fiveLabel  = new JLabel("5-Star: " + count5);
        JLabel up5Label   = new JLabel("up!5-Star: " + countUp5);

        Font statsFont = new Font("Arial", Font.PLAIN, 14);
        totalLabel.setFont(statsFont);
        threeLabel.setFont(statsFont);
        fourLabel.setFont(statsFont);
        fiveLabel.setFont(statsFont);
        up5Label.setFont(statsFont);

        statsPanel.add(totalLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(threeLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(fourLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(fiveLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(up5Label);

        historyFrame.add(statsPanel, BorderLayout.WEST);

        // ========== History List on the RIGHT (Scrollable) ==========

        DefaultListModel<String> historyModel = new DefaultListModel<>();
        for (String pullResult : history) {
            // Convert each raw result into HTML-labeled text for highlighting
            historyModel.addElement(getHighlightedResultHTML(pullResult));
        }

        JList<String> historyList = new JList<>(historyModel);
        historyList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                // Wrap the partial HTML in <html> so it renders
                label.setText("<html>" + (String) value + "</html>");
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyList);
        historyFrame.add(scrollPane, BorderLayout.CENTER);

        // ========== "Reset History" Button at the BOTTOM ==========

        JButton resetBtn = new JButton("Reset History");
        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetHistory();
                historyModel.clear();

                // Also update the stats labels
                totalLabel.setText("Total Pulls: 0");
                threeLabel.setText("3★: 0");
                fourLabel.setText("4★: 0");
                fiveLabel.setText("5★: 0");
                up5Label.setText("up!5★: 0");
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(resetBtn);
        historyFrame.add(bottomPanel, BorderLayout.SOUTH);

        historyFrame.setVisible(true);
    }

    /**
     * Returns an **HTML snippet** (without <html> wrapper) that highlights
     * 4★ in purple/bold, 5★ or up!5★ in orange/bold.
     *
     * Example return: "<b><font color='orange'>5★</font></b>"
     */
    private String getHighlightedResultHTML(String result) {
        if (result.contains("5★")) {
            // covers both "5★" and "up!5★"
            return "<b><font color='orange'>" + result + "</font></b>";
        } else if (result.contains("4★")) {
            return "<b><font color='purple'>" + result + "</font></b>";
        }
        // 3★ or anything else remains plain
        return result;
    }

    /**
     * Same logic as getHighlightedResultHTML, but we often want to embed the
     * result in a bigger <html> message (like the single-line popups).
     *
     * For single/10 pulls, we can call getHighlightedResultHTML() and wrap it with <html>.
     */
    private String getHighlightedResult(String result) {
        return getHighlightedResultHTML(result);
    }

    /**
     * Returns the UI panel so it can be embedded in a tab (e.g. "Resonate Simulation").
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    // ========== Implementation of 'tool' Interface ==========

    @Override
    public String getToolName() {
        return "pullSimulator";
    }

    @Override
    public void launch() {
        // If you have a console version or a separate usage scenario, put code here.
        // Otherwise, the main UI usage is through getMainPanel().
        System.out.println("pullSimulator launched (console mode).");
    }
}

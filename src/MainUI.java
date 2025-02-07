import tools.pullSimulator;
import tools.tool;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class MainUI {
    public static void main(String[] args) {
        // Make sure GUI creation is done on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new MainUI().createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        // Create the main window
        JFrame frame = new JFrame("WuWa Integrated Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create a tabbed pane to hold different tools
        JTabbedPane tabbedPane = new JTabbedPane();

        // 1) Pull Simulator Tool
        tool pullSimTool = new pullSimulator();
        tabbedPane.addTab(
                "Pull Simulator",
                ((pullSimulator) pullSimTool).getMainPanel()
        );

        // 2) Placeholder for other tool(s)
        //    For example: "Pull Analysis" or any other tool you have.
        //    If you create a class PullAnalysis implements Tool,
        //    then do something like:
        /*
        Tool pullAnalysisTool = new PullAnalysis();
        tabbedPane.addTab(
            "Pull Analysis", 
            ((PullAnalysis) pullAnalysisTool).getMainPanel()
        );
        */

        // Add the tabbed pane to the frame
        frame.add(tabbedPane, BorderLayout.CENTER);

        // Set the window size and location
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
package tools;

public interface tool {

    String getToolName();

    void launch();

    /**
     * Common interface for all tools in WuWa Integrated Tool.
     * Each tool must implement a way to launch or run itself.
     */
    public interface Tool {
        /**
         * @return A short identifier or name for the tool.
         */
        String getToolName();

        /**
         * Launch or run the tool.
         */
        void launch();
    }
}

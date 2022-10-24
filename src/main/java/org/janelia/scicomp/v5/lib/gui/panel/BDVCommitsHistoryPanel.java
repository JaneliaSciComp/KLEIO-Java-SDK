package org.janelia.scicomp.v5.lib.gui.panel;


import javax.swing.*;
import java.awt.*;

public class BDVCommitsHistoryPanel extends BDVCardPanel {
    private JPanel mainPanel;

    public BDVCommitsHistoryPanel() {
        super("History", "History", new GridLayout(0, 1), true);

        setSize(new Dimension(180, 600));
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 1));

        JScrollPane scrollFrame = new JScrollPane(mainPanel);
        add(scrollFrame);

        mainPanel.setAutoscrolls(true);

        setAutoscrolls(true);
    }

    @Override
    protected void updateView() {
        revalidate();
        repaint();
    }

    public static void main(String[] args) {

        BDVCommitsHistoryPanel bdvCommitsHistoryPanel = new BDVCommitsHistoryPanel();
        JFrame frame = new JFrame("Test");
        frame.setLayout(new GridLayout(0, 1));
        frame.setSize(new Dimension(200, 600));
        frame.add(bdvCommitsHistoryPanel);
        frame.setVisible(true);
    }
}

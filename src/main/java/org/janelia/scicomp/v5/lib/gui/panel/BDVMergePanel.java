package org.janelia.scicomp.v5.lib.gui.panel;


import org.janelia.scicomp.v5.lib.gui.panel.confict.ConflictTableView;
import org.janelia.scicomp.v5.lib.vc.merge.entities.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BDVMergePanel extends BDVCardPanel {
    private final ConflictTableView table;
    private MergeBranches mergeBranches;

//    public BDVMergePanel(List<ConflictBlockViewModel> conflicts) {
//        super("Merge", "Merge", new GridBagLayout(), true);
//        GridBagConstraints c = new GridBagConstraints();
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx = 1;
//        c.gridx = 0;
//        c.gridy = 0;
//        c.ipady = 20;
//        JPanel headerPanel = new JPanel(new GridLayout(0,2));
//
//        JButton button = new JButton("Set All");
//
//
//        add(headerPanel,c);
//
//        this.conflicts = conflicts;
//        this.table = new ConflictTableView(this.conflicts);
//        JScrollPane scrollFrame = new JScrollPane( this.table);
//        c.gridx = 0;
//        c.weighty = 1;
//        c.gridy = 1;
//        add(scrollFrame,c);
//    }

    public BDVMergePanel(String[] branches, AbstractBranchMergeController controller) {
        super("Merge", "Merge", new GridBagLayout(), true);

        this.table = new ConflictTableView();
        JLabel messageLabel = new JLabel("");

        JButton fixConflict = new JButton("Fix conflicts");
        fixConflict.setEnabled(false);

        addBranchesMergePanel(this, branches, mergeBranches -> {
            System.out.println(mergeBranches);
            this.mergeBranches = mergeBranches;
            ImgMergeResult imgMergeResult = controller.merge(mergeBranches);
            String resultMessage = imgMergeResult.getResult().getMessage();
            messageLabel.setText(resultMessage);
            table.updateConflicts(imgMergeResult);
            if (imgMergeResult.getResult().equals(ImgMergeResult.Case.CONFLICT_NEED_MANUAL_SELECTION)) {
                fixConflict.setEnabled(true);
            } else {
                fixConflict.setEnabled(false);
            }
            repaint();
            revalidate();
        });

        JScrollPane scrollFrame = new JScrollPane(this.table);
        scrollFrame.setAutoscrolls(true);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 3;
        c.ipady = 20;
        add(messageLabel, c);

        c.gridy = 4;
        c.weighty = 1;
        add(scrollFrame, c);
        fixConflict.addActionListener(e -> {
            if (mergeBranches == null) {
                System.out.println("ERROR | Invalid Merge branches !");
                return;
            }
            try {
                boolean success = controller.mergeConflicts(mergeBranches, table.getConflicts());
                if (success) {
                    messageLabel.setText("Success !");
                }
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
                throw new RuntimeException(ex);
            }
            repaint();
            revalidate();
        });
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.ipady = 20;
        c.gridy = 5;
//        c.weighty = 1;
        add(fixConflict, c);
    }

    private static void addBranchesMergePanel(BDVMergePanel mainPanel, String[] branches, AbstractCallBack<MergeBranches> callback) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 20;
        JComboBox<String> sourceInput = new JComboBox<>(branches);
        sourceInput.setSelectedIndex(branches.length - 1);
        JPanel panel = getComboInputPanel("Source Branch", sourceInput);
        mainPanel.add(panel, c);

        JComboBox<String> targetInput = new JComboBox<>(branches);
        panel = getComboInputPanel("Target Branch", targetInput);
        c.gridy = 1;
        mainPanel.add(panel, c);

        JButton mergeButton = new JButton("MERGE");
        mergeButton.addActionListener(e -> {
            String sourceBranch = (String) sourceInput.getSelectedItem();
            String targetBranch = (String) targetInput.getSelectedItem();
            MergeBranches mergeBranches = new MergeBranches(sourceBranch, targetBranch);
            try {
                callback.call(mergeBranches);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        c.gridy = 2;
        mainPanel.add(mergeButton, c);
    }

    private static JPanel getComboInputPanel(String title, JComboBox<String> comboInput) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(title));
        panel.add(comboInput);
        return panel;
    }

    @Override
    protected void updateView() {
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        List<BlockConflictEntry> conflicts = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            conflicts.add(new BlockConflictEntry(new long[]{2, 3, 4}));
        }
        BDVMergePanel bdvCommitsHistoryPanel = new BDVMergePanel(new String[]{"master", "branch1"}, new TestBranchMergeController());
//        BDVMergePanel bdvCommitsHistoryPanel = new BDVMergePanel(conflicts);
        JFrame frame = new JFrame("Test");
        frame.setLayout(new GridLayout(0, 1));
        frame.setSize(new Dimension(200, 300));
        frame.add(bdvCommitsHistoryPanel);
        frame.setVisible(true);
    }
}

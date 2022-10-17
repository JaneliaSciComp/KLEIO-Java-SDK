/*
 * *
 *  * Copyright (c) 2022, Janelia
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice,
 *  *    this list of conditions and the following disclaimer.
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.janelia.scicomp.v5.lib.gui.panel.confict;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ConflictTableView extends JScrollPane {

    public ConflictTableView(List<ConflictBlockViewModel> conflicts) {
        super(getTableView(conflicts));
    }

    private static Component getTableView(List<ConflictBlockViewModel> conflicts) {
        JTable table = new JTable(new ConflictTableModel(conflicts));
        table.addMouseListener(new JTableButtonMouseListener(table));
        TableColumn branchColumn = table.getColumnModel().getColumn(0);
        branchColumn.setCellRenderer(new JTableButtonRenderer());
        branchColumn.setPreferredWidth(20);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        return table;
    }

    private static class JTableButtonMouseListener extends MouseAdapter {
        private final JTable table;

        public JTableButtonMouseListener(JTable table) {
            this.table = table;
        }

        public void mouseClicked(MouseEvent e) {
            int column = table.getColumnModel().getColumnIndexAtX(e.getX());
            int row = e.getY() / table.getRowHeight();
            if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                Object value = table.getValueAt(row, column);
                if (value instanceof JButton) {
                    ((JButton) value).doClick();
                    table.repaint();
                }
            }
        }
    }

    private static class JTableButtonRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            JButton button = (JButton) value;
            panel.add(button);
            return panel;
        }
    }

    public static void main(String[] args) {
        List<ConflictBlockViewModel> conflicts = new ArrayList<>();
        conflicts.add(new ConflictBlockViewModel(new long[]{2, 3, 4}));
        conflicts.add(new ConflictBlockViewModel(new long[]{0, 6, 4}));
        conflicts.add(new ConflictBlockViewModel(new long[]{0, 8, 4}));
        conflicts.add(new ConflictBlockViewModel(new long[]{9, 4, 4}));
        ConflictTableView table = new ConflictTableView(conflicts);

        JFrame frame = new JFrame("Test");
        frame.setSize(new Dimension(300, 600));

        frame.add(table);
        frame.setVisible(true);
    }

}

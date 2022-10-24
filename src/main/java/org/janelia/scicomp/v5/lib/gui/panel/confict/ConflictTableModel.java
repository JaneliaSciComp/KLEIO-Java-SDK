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

import org.janelia.scicomp.v5.lib.vc.merge.entities.BlockConflictEntry;
import org.janelia.scicomp.v5.lib.vc.merge.entities.ImgMergeResult;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;


public class ConflictTableModel extends AbstractTableModel {
    private final static String[] COLUMNS = {"Branch", "Block"};
    private ImgMergeResult imgMergeResult= new ImgMergeResult();;

    public ConflictTableModel() {
    }

    public void setElms(ImgMergeResult imgMergeResult) {
        this.imgMergeResult = imgMergeResult;
    }

    public List<BlockConflictEntry> getElms() {
        return imgMergeResult.getConflicts();
    }

    @Override
    public int getRowCount() {
        return imgMergeResult.getConflicts().size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        BlockConflictEntry elm = imgMergeResult.getConflicts().get(rowIndex);
        switch (columnIndex) {
            case 0:

                final JButton button = new JButton(elm.getBranchString());
                button.setPreferredSize(new Dimension(20, 20));
                button.setSize(new Dimension(20, 20));
                if (elm.isMerged()){
                    button.setVisible(false);
                }else{
                    button.addActionListener(arg0 -> {
                        elm.getNextBranch();
                        button.setText(elm.getBranchString());
                    });
                }
                return button;
            case 1:
                return elm.getStringGridPosition();
            default:
                throw new RuntimeException("Column " + columnIndex + " not implemented!");
        }
    }


    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0)
            return JButton.class;
        else return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}

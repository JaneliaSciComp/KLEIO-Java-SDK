package org.janelia.scicomp.v5.lib.gui.panel;

import javax.swing.*;
import java.awt.*;

public abstract class BDVCardPanel extends JPanel {
    final private String key;
    final private String title;
    final private boolean expend;

    public BDVCardPanel(String key, String title, LayoutManager layout, boolean expend) {
        super(layout);
        this.key = key;
        this.title = title;
        this.expend = expend;
    }

    public BDVCardPanel(String key, String title, LayoutManager layout) {
        this(key, title, layout, false);
    }

    public void setSource(int sourceId) {
    }

    public void onNotify() {
        updateView();
    }

    protected void updateView() {
    }


    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public boolean isExpend() {
        return expend;
    }
}

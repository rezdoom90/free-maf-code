package com.freemaf.agent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
public final class CalibrationPanel extends JPanel {
    public CalibrationPanel(Runnable onCalibrate) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 4));
        setBackground(DarkTheme.BG);
        JLabel hint = new JLabel("Calibration:");
        hint.setForeground(DarkTheme.FG);
        JButton run = new JButton("Run Calibration");
        run.addActionListener(e -> { if (onCalibrate != null) onCalibrate.run(); });
        add(hint);
        add(run);
    }
}

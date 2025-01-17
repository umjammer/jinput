/*
 * https://github.com/bozjator/JInput-Joystick
 */

package bozjator;

import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JPanel;

import net.java.games.input.Component;
import vavi.util.Debug;


/**
 * Joystick Test Window
 *
 * @author <a href="http://theuzo007.wordpress.com">TheUzo007</a>
 * @version 22 Oct 2013
 */
public class JFrameWindow extends javax.swing.JFrame {
    
    /**
     * Creates new form JFrameWindow
     */
    public JFrameWindow() {
        initComponents();

        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelAxes = new javax.swing.JPanel();
        jLabelXYAxis = new javax.swing.JLabel();
        jPanelXYAxis = new javax.swing.JPanel();
        jPanel_forAxis = new javax.swing.JPanel();
        jPanelButtons = new javax.swing.JPanel();
        jPanelHatSwitch = new javax.swing.JPanel();
        jComboBox_controllers = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JInput Joystick Test");

        jPanelAxes.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Axes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 51, 204)));

        jLabelXYAxis.setText("X Axis / Y Axis");

        jPanelXYAxis.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanelXYAxis.setPreferredSize(new java.awt.Dimension(111, 111));

        javax.swing.GroupLayout jPanelXYAxisLayout = new javax.swing.GroupLayout(jPanelXYAxis);
        jPanelXYAxis.setLayout(jPanelXYAxisLayout);
        jPanelXYAxisLayout.setHorizontalGroup(
            jPanelXYAxisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 109, Short.MAX_VALUE)
        );
        jPanelXYAxisLayout.setVerticalGroup(
            jPanelXYAxisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 109, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel_forAxisLayout = new javax.swing.GroupLayout(jPanel_forAxis);
        jPanel_forAxis.setLayout(jPanel_forAxisLayout);
        jPanel_forAxisLayout.setHorizontalGroup(
            jPanel_forAxisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 202, Short.MAX_VALUE)
        );
        jPanel_forAxisLayout.setVerticalGroup(
            jPanel_forAxisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanelAxesLayout = new javax.swing.GroupLayout(jPanelAxes);
        jPanelAxes.setLayout(jPanelAxesLayout);
        jPanelAxesLayout.setHorizontalGroup(
            jPanelAxesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAxesLayout.createSequentialGroup()
                .addGroup(jPanelAxesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelAxesLayout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(jLabelXYAxis))
                    .addGroup(jPanelAxesLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jPanelXYAxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel_forAxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelAxesLayout.setVerticalGroup(
            jPanelAxesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAxesLayout.createSequentialGroup()
                .addComponent(jLabelXYAxis)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelXYAxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 16, Short.MAX_VALUE))
            .addComponent(jPanel_forAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanelButtons.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Buttons", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 51, 204)));

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 248, Short.MAX_VALUE)
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 112, Short.MAX_VALUE)
        );

        jPanelHatSwitch.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Hat Switch", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 51, 204)));

        javax.swing.GroupLayout jPanelHatSwitchLayout = new javax.swing.GroupLayout(jPanelHatSwitch);
        jPanelHatSwitch.setLayout(jPanelHatSwitchLayout);
        jPanelHatSwitchLayout.setHorizontalGroup(
            jPanelHatSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 121, Short.MAX_VALUE)
        );
        jPanelHatSwitchLayout.setVerticalGroup(
            jPanelHatSwitchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jComboBox_controllers.addActionListener(this::jComboBox_controllersActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanelHatSwitch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jPanelAxes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jComboBox_controllers, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(88, 88, 88))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jComboBox_controllers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelAxes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelHatSwitch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox_controllersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_controllersActionPerformed
        // When another controller is selected we have to remove old stuff.
        jPanelButtons.removeAll();
        jPanelButtons.repaint();
        jPanel_forAxis.removeAll();
        jPanel_forAxis.repaint();
    }//GEN-LAST:event_jComboBox_controllersActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jComboBox_controllers;
    private javax.swing.JLabel jLabelXYAxis;
    private javax.swing.JPanel jPanelAxes;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JPanel jPanelHatSwitch;
    private javax.swing.JPanel jPanelXYAxis;
    private javax.swing.JPanel jPanel_forAxis;
    // End of variables declaration//GEN-END:variables

    
    
    
    /* Methods for setting components on the window. */
    
    public int getSelectedControllerName(){
//Debug.println("combobox::size: " + jComboBox_controllers.getItemCount());
        return jComboBox_controllers.getSelectedIndex();
    }
    
    public void addControllerName(String controllerName){
        jComboBox_controllers.addItem(controllerName);
Debug.println("combobox::add: " + jComboBox_controllers.getItemCount());
    }
    
    public void showControllerDisconnected(){
        jComboBox_controllers.removeAllItems();
        jComboBox_controllers.addItem("Controller disconnected!");
    }
    
    public void setXYAxis(int xPercentage, int yPercentage){
        Graphics2D g2d = (Graphics2D)jPanelXYAxis.getGraphics();
        g2d.clearRect(1, 1, jPanelXYAxis.getWidth() - 2, jPanelXYAxis.getHeight() - 2);
        g2d.fillOval(xPercentage, yPercentage, 10, 10);
    }
    
    public void setControllerButtons(JPanel buttonsPanel){
        jPanelButtons.removeAll();
        jPanelButtons.add(buttonsPanel);
        jPanelButtons.validate();
    }

    public void setHatSwitch(float hatSwitchPosition) {
        int circleSize = 100;
        
        Graphics2D g2d = (Graphics2D)jPanelHatSwitch.getGraphics();
        g2d.clearRect(5, 15, jPanelHatSwitch.getWidth() - 10, jPanelHatSwitch.getHeight() - 22);
        g2d.drawOval(20, 22, circleSize, circleSize);
        
        if(Float.compare(hatSwitchPosition, Component.POV.OFF) == 0)
            return;
        
        int smallCircleSize = 10;
        int upCircleX = 65;
        int upCircleY = 17;
        int leftCircleX = 15;
        int leftCircleY = 68;
        int betweenX = 37;
        int betweenY = 17;
        
        int x = 0;
        int y = 0;
        
        g2d.setColor(Color.blue);
                        
        if(Float.compare(hatSwitchPosition, Component.POV.UP) == 0){
            x = upCircleX;
            y = upCircleY;
        }else if(Float.compare(hatSwitchPosition, Component.POV.DOWN) == 0){
            x = upCircleX;
            y = upCircleY + circleSize;
        }else if(Float.compare(hatSwitchPosition, Component.POV.LEFT) == 0){
            x = leftCircleX;
            y = leftCircleY;
        }else if(Float.compare(hatSwitchPosition, Component.POV.RIGHT) == 0){
            x = leftCircleX + circleSize;
            y = leftCircleY;
        }else if(Float.compare(hatSwitchPosition, Component.POV.UP_LEFT) == 0){
            x = upCircleX - betweenX;
            y = upCircleY + betweenY;
        }else if(Float.compare(hatSwitchPosition, Component.POV.UP_RIGHT) == 0){
            x = upCircleX + betweenX;
            y = upCircleY + betweenY;
        }else if(Float.compare(hatSwitchPosition, Component.POV.DOWN_LEFT) == 0){
            x = upCircleX - betweenX;
            y = upCircleY + circleSize - betweenY;
        }else if(Float.compare(hatSwitchPosition, Component.POV.DOWN_RIGHT) == 0){
            x = upCircleX + betweenX;
            y = upCircleY + circleSize - betweenY;
        }
        
        g2d.fillOval(x, y, smallCircleSize, smallCircleSize);
    }

    public void addAxisPanel(javax.swing.JPanel axesPanel){
        jPanel_forAxis.removeAll();
        jPanel_forAxis.add(axesPanel);
        jPanel_forAxis.validate();
    }
    
}
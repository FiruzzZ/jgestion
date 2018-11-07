
/*
 * JDStockGral.java
 *
 * Created on 12/04/2010, 10:39:52
 */
package jgestion.gui;

import java.awt.Dialog;
import java.awt.Window;

/**
 *
 * @author FiruzzZ
 */
public class JDStockGral extends javax.swing.JDialog {

    public JDStockGral(Window owner, boolean modal) {
        super(owner, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
        initComponents();
    }

    public JDStockGral(Dialog owner) {
        super(owner);
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Detalle de stock general");

        jTable1.setModel(new javax.swing.table.DefaultTableModel());
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    public javax.swing.JTable getjTable1() {
        return jTable1;
    }

    public javax.swing.table.DefaultTableModel getDtm() {
        return (javax.swing.table.DefaultTableModel) jTable1.getModel();
    }

}
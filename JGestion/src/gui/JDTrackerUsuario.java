package gui;

import entity.Usuario;
import entity.UsuarioAcciones;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jpa.controller.UsuarioAccionesJpaController;
import jpa.controller.UsuarioJpaController;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class JDTrackerUsuario extends javax.swing.JDialog {

    /**
     * Creates new form JDTrackerUsuario
     */
    public JDTrackerUsuario(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        UTIL.hideColumnTable(jXTable1, 0);
        List<Usuario> l = new UsuarioJpaController().findAll();
        UTIL.loadComboBox(cbUsuarios, JGestionUtils.getWrappedUsuarios(l), false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        cbUsuarios = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        dcDesde = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        dcHasta = new com.toedter.calendar.JDateChooser();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTable1 = new org.jdesktop.swingx.JXTable();
        btnBuscar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        taDetalle = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Usuarios");

        cbUsuarios.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel2.setText("Desde");

        jLabel3.setText("Hasta");

        jXTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Object", "Usuario", "Acción", "Descripción", "Detalle", "Fecha"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jXTable1.getTableHeader().setReorderingAllowed(false);
        jXTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jXTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jXTable1);
        jXTable1.getColumnModel().getColumn(0).setResizable(false);
        jXTable1.getColumnModel().getColumn(1).setResizable(false);
        jXTable1.getColumnModel().getColumn(1).setPreferredWidth(50);
        jXTable1.getColumnModel().getColumn(2).setResizable(false);
        jXTable1.getColumnModel().getColumn(2).setPreferredWidth(20);
        jXTable1.getColumnModel().getColumn(3).setPreferredWidth(100);
        jXTable1.getColumnModel().getColumn(4).setPreferredWidth(300);
        jXTable1.getColumnModel().getColumn(5).setPreferredWidth(80);

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        taDetalle.setColumns(20);
        taDetalle.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        taDetalle.setRows(5);
        jScrollPane2.setViewportView(taDetalle);

        jLabel4.setText("Seleccionar una fila para ver la acción + detalle");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(56, 56, 56)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBuscar))
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(btnBuscar)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(cbUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel1)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        String query = armarQuery();
        List<UsuarioAcciones> l = new UsuarioAccionesJpaController().findByQuery(query);
        cargarTabla(l);
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void jXTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXTable1MouseClicked
        if (jXTable1.getSelectedRow() > -1) {
            UsuarioAcciones ua = (UsuarioAcciones) jXTable1.getModel().getValueAt(jXTable1.getSelectedRow(), 0);
            taDetalle.setText(ua.getDescripcion() + "\n" + ua.getDetalle());
        }
    }//GEN-LAST:event_jXTable1MouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JComboBox cbUsuarios;
    private com.toedter.calendar.JDateChooser dcDesde;
    private com.toedter.calendar.JDateChooser dcHasta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXTable jXTable1;
    private javax.swing.JTextArea taDetalle;
    // End of variables declaration//GEN-END:variables

    private String armarQuery() {
        StringBuilder sb = new StringBuilder("SELECT o FROM " + UsuarioAcciones.class.getSimpleName() + " o "
                + " WHERE o.id is not null ");
        @SuppressWarnings("unchecked")
        Usuario u = ((ComboBoxWrapper<Usuario>) cbUsuarios.getSelectedItem()).getEntity();
        sb.append(" AND o.usuario.id = ").append(u.getId());
        if (dcDesde.getDate() != null) {
            sb.append(" AND o.fechasistema >='").append(UTIL.yyyy_MM_dd.format(dcDesde.getDate())).append("'");
        }
        if (dcHasta.getDate() != null) {
            sb.append(" AND o.fechasistema <='").append(UTIL.yyyy_MM_dd.format(dcHasta.getDate())).append("'");
        }
        return sb.toString();
    }

    private void cargarTabla(List<UsuarioAcciones> l) {
        DefaultTableModel dtm = (DefaultTableModel) jXTable1.getModel();
        dtm.setRowCount(0);
        for (UsuarioAcciones ua : l) {
            dtm.addRow(new Object[]{
                        ua,
                        ua.getUsuario().getNick(),
                        ua.getAccion(),
                        ua.getDescripcion(),
                        ua.getDetalle(),
                        UTIL.TIMESTAMP_FORMAT.format(ua.getFechasistema())
                    });
        }
    }
}

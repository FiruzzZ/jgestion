package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import entity.Banco;
import entity.Caja;
import entity.ChequeTerceros;
import entity.Cliente;
import entity.enums.ChequeEstado;
import gui.JDABM;
import gui.JDChequesManager;
import gui.PanelABMCheques;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jpa.controller.ChequeTercerosJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author Administrador
 */
public class ChequeTercerosController implements ActionListener {

    private static Logger LOG = Logger.getLogger(ChequePropioController.class);
    public final static int MAX_LENGHT_DIGITS_QUANTITY = 20;
    private ChequeTerceros EL_OBJECT;
    private JDABM abm;
    private PanelABMCheques panelABM;
    private ChequeTercerosJpaController jpaController;
    //exclusively related to the GUI
    private JDChequesManager jdChequeManager;
    private final String[] columnNames = {"id", "Nº Cheque", "F. Cheque", "Emisor", "F. Cobro", "Banco", "Importe", "Estado", "Cruzado", "Endosatario", "F. Endoso", "C. Ingreso", "C. Egreso", "Observacion", "Usuario"};
    private final int[] columnWidths = {1, 80, 80, 100, 80, 100, 100, 100, 50, 100, 100, 150, 150, 100, 80};
    private final Class[] columnClassTypes = {Integer.class, Number.class, null, null, null, null, Number.class, null, Boolean.class, null};
    //Este se pone en el comboBox
    private final String[] orderByToComboBoxList = {"N° Cheque", "Fecha de Emisión", "Fecha de Cobro", "Importe", "Banco", "Cliente", "Estado"};
    //Y este es el equivalente (de lo seleccionado en el combo) para el SQL.
    private final String[] orderByToQueryKeyList = {"numero", "fecha_cheque", "fecha_cobro", "importe", "banco.nombre", "cliente.nombre", "estado"};

    public ChequeTercerosController() {
        jpaController = new ChequeTercerosJpaController();
    }

    private ChequeTerceros initABMReemplazo(Window owner, ChequeTerceros toReplaced) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        initPanelABM();
//        setPanel(toReplaced);
        panelABM.getTfImporte().setText(UTIL.PRECIO_CON_PUNTO.format(toReplaced.getImporte()));
        panelABM.getTaObservacion().setText("Reemplazo de: " + toReplaced.getBanco().getNombre() + ", N°" + toReplaced.getNumero());
        panelABM.setPersistible(true); // <-- OJO ACA!!!
        abm = new JDABM(owner, null, true, panelABM);
        abm.setTitle("Reemplazo de Cheque N°" + toReplaced.getNumero());
        abm.setListener(this);
        abm.setVisible(true);
        return EL_OBJECT;
    }

    ChequeTerceros initABM(Window owner, boolean isEditing, Cliente cliente) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        if (isEditing && EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila de la tabla");
        }
        initPanelABM();
        UTIL.setSelectedItem(panelABM.getCbEmisor(), cliente);
        if (isEditing) {
            setPanel(EL_OBJECT);
        }
        abm = new JDABM(owner, null, true, panelABM);
        abm.setTitle("ABM - Cheque Terceros");
        abm.setListener(this);
        abm.setVisible(true);
        return EL_OBJECT;
    }

    private void initPanelABM() {
        panelABM = new PanelABMCheques();
        panelABM.setUIChequeTerceros();
        UTIL.loadComboBox(panelABM.getCbBancos(), new BancoController().findEntities(), true);
//        UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(panelABM.getCbEmisor(), new ClienteController().findEntities(), false);
    }

    private void setPanel(ChequeTerceros cheque) {
        UTIL.setSelectedItem(panelABM.getCbEmisor(), cheque.getCliente());
        UTIL.setSelectedItem(panelABM.getCbBancos(), cheque.getBanco());
        panelABM.getTfNumero().setText(cheque.getNumero().toString());
        panelABM.getTfImporte().setText(UTIL.PRECIO_CON_PUNTO.format(cheque.getImporte()));
        panelABM.getDcCheque().setDate(cheque.getFechaCheque());
        panelABM.getDcCobro().setDate(cheque.getFechaCobro());
        panelABM.getDcCheque().setDate(cheque.getFechaCheque());
        panelABM.getCheckCruzado().setSelected(cheque.isCruzado());
        panelABM.getTaObservacion().setText(cheque.getObservacion());
        panelABM.getTfEndosatario().setText(cheque.getEndosatario());
        panelABM.getDcEndoso().setDate(cheque.getFechaEndoso());
    }

    private ChequeTerceros getEntity() throws MessageException {
        Date fechaCheque, fechaCobro, fechaEndoso = null;
        Cliente cliente;
        Banco banco;
        Long numero = null;
        BigDecimal importe = null;
        boolean cruzado;
        String endosatario = null, observacion = null;

        if (panelABM.getDcCheque() == null) {
            throw new MessageException("Debe ingresar la Fecha del Cheque.");
        }
        if (panelABM.getDcCobro() == null) {
            throw new MessageException("Debe ingresar la Fecha de cobro del Cheque.");
        }
        fechaCheque = panelABM.getDcCheque().getDate();
        fechaCobro = panelABM.getDcCobro().getDate();

        if (UTIL.compararIgnorandoTimeFields(fechaCheque, fechaCobro) > 0) {
            throw new MessageException("Fecha de cobro no puede ser anterior a Fecha de cheque.");
        }
        try {
            numero = Long.valueOf(panelABM.getTfNumero().getText());
        } catch (NumberFormatException numberFormatException) {
            throw new MessageException("Número de cheque no válido");
        }
        try {
            importe = new BigDecimal(panelABM.getTfImporte().getText());
        } catch (NumberFormatException numberFormatException) {
            throw new MessageException("Importe no válido (ingrese solo números y utilice el punto como separador decimal");
        }
        if (panelABM.getCbBancos().getSelectedIndex() < 1) {
            throw new MessageException("Debe elegir un Banco");
        }
        banco = (Banco) panelABM.getCbBancos().getSelectedItem();
//        try {
//            sucursal = (BancoSucursal) panelABM.getCbBancoSucursales().getSelectedItem();
//        } catch (ClassCastException e) {
//            throw new MessageException("Sucursal de Banco no válida");
//        }
//        librado = (Librado) panelABM.getCbLibrado().getSelectedItem();
        cliente = (Cliente) panelABM.getCbEmisor().getSelectedItem();
        cruzado = panelABM.getCheckCruzado().isSelected();
        observacion = panelABM.getTaObservacion().getText();
        if (panelABM.getCheckEndosado().isSelected()) {
            fechaEndoso = panelABM.getDcEndoso().getDate();
            if (fechaEndoso == null) {
                throw new MessageException("Debe especificar un fecha de endoso si há seleccionado la opción de endosado.");
            }
            if (fechaEndoso.before(fechaCheque) || fechaEndoso.before(fechaCobro)) {
                throw new MessageException("La fecha de endoso no puede ser anterior a la fecha de Emisión o Cobro");
            }
            endosatario = panelABM.getTfEndosatario().getText().trim();
            if (endosatario == null || endosatario.length() < 1) {
                throw new MessageException("Debe especificar un Endosatario si há seleccionado la opción de endosado.");
            }
        }
        ChequeTerceros newCheque = new ChequeTerceros(cliente, numero, banco, null, importe, fechaCheque, fechaCobro, cruzado, observacion, ChequeEstado.CARTERA, endosatario, fechaEndoso, UsuarioController.getCurrentUser());
        return newCheque;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            // <editor-fold defaultstate="collapsed" desc="JButton">
            if (e.getSource().getClass().equals(JButton.class)) {
                JButton boton = (JButton) e.getSource();
                //<editor-fold defaultstate="collapsed" desc="abm & panelABM EVENTS">
                if (abm != null && panelABM != null) {
                    if (boton.equals(abm.getbAceptar())) {
                        try {
                            Integer id = null;
                            if (EL_OBJECT != null) {
                                id = EL_OBJECT.getId();
                            }
                            ChequeTerceros cheque = getEntity();
                            if (id != null) {
                                cheque.setId(id);
                            }
                            checkConstraints(cheque);
                            if (panelABM.persist()) {
                                String msg = cheque.getId() == null ? "Registrado" : "Modificado";
                                if (cheque.getId() == null) {
                                    jpaController.create(cheque);
                                } else {
                                    jpaController.merge(cheque);
                                }
                                abm.showMessage(msg, jpaController.getEntityClass().getSimpleName(), 1);
                            }
                            EL_OBJECT = cheque;
                            abm.dispose();
                        } catch (MessageException ex) {
                            abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                        } catch (Exception ex) {
                            abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                            LOG.error(ex, ex);
                        }
                    } else if (boton.equals(abm.getbCancelar())) {
                        abm.dispose();
                        panelABM = null;
                        abm = null;
                        EL_OBJECT = null;
                    } else if (boton.equals(panelABM.getbAddBanco())) {
                        try {
                            JDialog initABM = new BancoController().initABM(abm);
                            initABM.setLocationRelativeTo(abm);
                            initABM.setVisible(true);
                        } catch (MessageException ex) {
                            abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                        }
                        UTIL.loadComboBox(panelABM.getCbBancos(), new BancoController().findEntities(), true);
                    } else if (boton.equals(panelABM.getbAddEmisor())) {
                        JDialog initContenedor = new ClienteController().initContenedor(null, true);
                        initContenedor.setVisible(true);
                    }
                } //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="jdChequeManager EVENTS">
                if (jdChequeManager != null) {
                    if (boton.equals(jdChequeManager.getbBuscar())) {
                        try {
                            armarQuery(false);
                        } catch (DatabaseErrorException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error ejecutando consulta", JOptionPane.ERROR_MESSAGE);
                        }
                    } else if (boton.equals(jdChequeManager.getbLimpiar())) {
                        //no action
                    } else if (boton.equals(jdChequeManager.getbACaja())) {
                        //no action
                    } else if (boton.equals(jdChequeManager.getbAnular())) {
                        int row = jdChequeManager.getjTable1().getSelectedRow();
                        if (row > -1) {
                            ChequeTerceros cheque = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(row, 0));
                            if (cheque.getChequeEstado().equals(ChequeEstado.CARTERA)) {
                                cheque.setEstado(ChequeEstado.ANULADO.getId());
                                jpaController.merge(cheque);
                                armarQuery(false);
                            } else {
                                JOptionPane.showMessageDialog(jdChequeManager, "Solo los cheques en " + ChequeEstado.CARTERA + " pueden ser " + ChequeEstado.ANULADO, "Error", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    } else if (boton.equals(jdChequeManager.getbDeposito())) {
                        int row = jdChequeManager.getjTable1().getSelectedRow();
                        if (row > -1) {
                            ChequeTerceros cheque = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(row, 0));
                            if (cheque.getChequeEstado().equals(ChequeEstado.CARTERA)) {
                                new CuentabancariaController().initDepositoUI(cheque);
                            } else {
                                JOptionPane.showMessageDialog(jdChequeManager, "Solo los cheques en " + ChequeEstado.CARTERA + " pueden ser depositados", "Error", JOptionPane.WARNING_MESSAGE);
                            }
                            armarQuery(false);
                        }
                    } else if (boton.equals(jdChequeManager.getBtnReemplazar())) {
                        int row = jdChequeManager.getjTable1().getSelectedRow();
                        if (row > -1) {
                            ChequeTerceros toReplace = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(row, 0));
                            if (toReplace.getChequeEstado().equals(ChequeEstado.CARTERA)) {
                                ChequeTerceros reemplazo = initABMReemplazo(jdChequeManager, toReplace);
                                if (reemplazo != null) {
                                    toReplace.setEstado(ChequeEstado.REEMPLAZADO.getId());
                                    toReplace.setObservacion(toReplace.getObservacion() + " Reemplazado por: " + reemplazo.getBanco().getNombre() + " N° " + reemplazo.getNumero());
                                    if (toReplace.getObservacion().length() > 300) {
                                        toReplace.setObservacion(toReplace.getObservacion().substring(0, 300));
                                    }
                                    jpaController.merge(toReplace);
                                }
                                armarQuery(false);
                            } else {
                                JOptionPane.showMessageDialog(jdChequeManager, "Solo los cheques en cartera pueden ser depositados", "Error", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    } else if (boton.equals(jdChequeManager.getbImprimir())) {
                        try {
                            armarQuery(true);
                        } catch (DatabaseErrorException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error ejecutando consulta", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                //</editor-fold>
            }// </editor-fold>
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Error on actionPerformed", JOptionPane.ERROR_MESSAGE);
            LOG.error(ex, ex);
        }
    }

    private void checkConstraints(ChequeTerceros object) throws MessageException {
        ChequeTerceros cheque = jpaController.findBy(object.getBanco(), object.getNumero());
        if (cheque != null && !cheque.equals(object)) {
            throw new MessageException("Ya existe un Cheque del banco " + cheque.getBanco().getNombre() + " con el N° " + cheque.getNumero()
                    + "\nFecha emisión:" + UTIL.DATE_FORMAT.format(cheque.getFechaCheque())
                    + "\nFecha cobro: " + UTIL.DATE_FORMAT.format(cheque.getFechaCobro())
                    + "\nEstado:" + cheque.getChequeEstado()
                    + "\nIngreso:" + cheque.getComprobanteIngreso()
                    + "\nEgreso:" + cheque.getComprobanteEgreso());
        }
    }

    /**
     * Inicializa la vista encargada de administrar cheques (Propios o Terceros)
     *
     * @param owner JFrame padre.
     * @param listener Para los botones laterales. Si
     * <code>listener == null</code> se asignará <code>this</code> por defecto.
     * @param estado
     * @return una instancia de {@link JDChequesManager}.
     * @throws MessageException
     */
    public JDialog gettManagerTerceros(Window owner, ActionListener listener, ChequeEstado estado) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        initManager(owner);
        if (estado != null) {
            UTIL.setSelectedItem(jdChequeManager.getCbEstados(), estado.toString());
            jdChequeManager.getCbEstados().setEnabled(false);
        }
        if (listener != null) {
            jdChequeManager.addButtonListener(listener);
        } else {
            jdChequeManager.addButtonListener(this);
            jdChequeManager.getLabelEmisor().setText("Emisor");
            UTIL.loadComboBox(jdChequeManager.getCbEmisor(), JGestionUtils.getWrappedClientes(new ClienteController().findEntities()), true);
            jdChequeManager.setTitle("Administración de Cheques Terceros");
        }
        jdChequeManager.setLocationRelativeTo(owner);
        return jdChequeManager;
    }

    private void initManager(Window owner) {
        jdChequeManager = new JDChequesManager(owner, true);
        UTIL.loadComboBox(jdChequeManager.getCbBancos(), JGestionUtils.getWrappedBancos(new BancoController().findEntities()), true);
//        UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        jdChequeManager.getCbBancoSucursales().setVisible(false);
        jdChequeManager.getLabelSucursales().setVisible(false);
        jdChequeManager.getCbCuentaBancaria().setVisible(false);
        jdChequeManager.getLabelCuentaBancaria().setVisible(false);
        UTIL.loadComboBox(jdChequeManager.getCbEstados(), Arrays.asList(ChequeEstado.values()), true);
        UTIL.loadComboBox(jdChequeManager.getCbOrderBy(), Arrays.asList(orderByToComboBoxList), false);
        UTIL.getDefaultTableModel(jdChequeManager.getjTable1(), columnNames, columnWidths, columnClassTypes);
        jdChequeManager.getjTable1().getColumnModel().getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(jdChequeManager.getjTable1(), 0);
    }

    private void cargarTablaChequeManager(String query) throws DatabaseErrorException {
        DefaultTableModel dtm = UTIL.getDtm(jdChequeManager.getjTable1());
        dtm.setRowCount(0);
        List<?> l = DAO.getNativeQueryResultList(query);
        for (Object object : l) {
            //"id", "Nº Cheque", "F. Cheque", "Emisor", "F. Cobro", "Banco", "Importe", "Estado", "Cruzado", "Endosatario", "F. Endoso", "C. Ingreso", "C. Egreso", "Observacion", "Usuario"};
            dtm.addRow((Object[]) object);
        }
    }

    private void armarQuery(boolean imprimir) throws DatabaseErrorException {
        StringBuilder query = new StringBuilder("SELECT "
                + " c.id, c.numero, TO_CHAR(c.fecha_cheque,'DD/MM/YYYY') as fecha_cheque, cliente.nombre as cliente, TO_CHAR(c.fecha_cobro,'DD/MM/YYYY') as fecha_cobro,"
                + " banco.nombre as banco, c.importe, cheque_estado.nombre as estado, c.cruzado"
                + ", c.endosatario, c.fecha_endoso"
                + ", c.comprobante_ingreso, c.comprobante_egreso, c.observacion"
                + ", usuario.nick as usuario "
                + " FROM cheque_terceros c "
                + " JOIN banco ON (c.banco = banco.id) "
                + " JOIN cliente ON (c.cliente = cliente.id) "
                + " JOIN usuario ON (c.usuario = usuario.id) "
                + " JOIN cheque_estado  ON (c.estado  = cheque_estado.id)"
                //                + " LEFT JOIN banco_sucursal ON (c.banco_sucursal = banco_sucursal.id) "
                //                + " LEFT JOIN librado ON (c.librado = librado.id) "
                + " WHERE c.id > -1");
        if (jdChequeManager.getTfChequeNumero().getText().trim().length() > 0) {
            try {
                Long numero = Long.valueOf(jdChequeManager.getTfChequeNumero().getText());
                query.append(" AND c.numero='").append(numero).append("'");
            } catch (NumberFormatException numberFormatException) {
            }
        }
        if (jdChequeManager.getDcEmisionDesde() != null) {
            query.append(" AND c.fecha_cheque >='").append(jdChequeManager.getDcEmisionDesde()).append("'");
        }
        if (jdChequeManager.getDcEmisionHasta() != null) {
            query.append(" AND c.fecha_cheque <='").append(jdChequeManager.getDcEmisionHasta()).append("'");
        }
        if (jdChequeManager.getDcCobroDesde() != null) {
            query.append(" AND c.fecha_cobro >='").append(jdChequeManager.getDcCobroDesde()).append("'");
        }
        if (jdChequeManager.getDcCobroHasta() != null) {
            query.append(" AND c.fecha_cobro <='").append(jdChequeManager.getDcCobroHasta()).append("'");
        }
        if (jdChequeManager.getTfImporte().getText().trim().length() > 0) {
            try {
                Double importe = Double.valueOf(jdChequeManager.getTfImporte().getText());
                query.append(" AND c.importe").append(jdChequeManager.getCbImporteCondicion().getSelectedItem().toString()).append(importe);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        if (jdChequeManager.getCbBancos().getSelectedIndex() > 0) {
            query.append(" AND c.banco=").append(((ComboBoxWrapper<Banco>) jdChequeManager.getCbBancos().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbEmisor().getSelectedIndex() > 0) {
            query.append(" AND c.cliente=").append(((Cliente) jdChequeManager.getCbEmisor().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbEstados().getSelectedIndex() > 0) {
            query.append(" AND c.estado=").append(((ChequeEstado) jdChequeManager.getCbEstados().getSelectedItem()).getId());
        }

        query.append(" ORDER BY ").append(orderByToQueryKeyList[jdChequeManager.getCbOrderBy().getSelectedIndex()]);
        LOG.debug(query.toString());
        cargarTablaChequeManager(query.toString());
        if (imprimir) {
            doChequeTercerosReport(query.toString());
        }
    }

    private void doChequeTercerosReport(String query) {
    }

    ChequeTerceros getChequeTerceroInstance() {
        return EL_OBJECT;
    }

    /**
     * Levanta una UI de selección de Caja, en la cual se va asentar el cheque.
     *
     * @return instance of Caja si seleccionó
     */
    private Caja initUIAsentarChequeToCaja() {
        Caja caja = null;
        JPanel p = new JPanel(new GridLayout(3, 1));
        p.add(new JLabel("Seleccionar Caja destino"));
        final JComboBox cbCajas = new JComboBox();
        UTIL.loadComboBox(cbCajas, new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), Boolean.TRUE), false);
        p.add(cbCajas);
        abm = new JDABM(jdChequeManager, "Asentar Cheque a Caja", true, p);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Caja selectedCaja = (Caja) cbCajas.getSelectedItem();
                    abm.setAceptarAsLastButtonPressed();
                    abm.dispose();
                } catch (ClassCastException ex) {
                    abm.showMessage("No ha seleccionado ninguna Caja", "Error", 2);
                }
            }
        });
        abm.setVisible(true);
        if (abm.isAceptarLastButtonPressed()) {
            caja = (Caja) cbCajas.getSelectedItem();
        }
        return caja;
    }
}

package controller;

import controller.exceptions.MessageException;
import entity.Banco;
import entity.BancoSucursal;
import entity.Caja;
import entity.ChequeTerceros;
import entity.Cliente;
import entity.Librado;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import jpa.controller.ChequeTercerosJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;

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
    private final String[] columnNames = {"id", "Nº Cheque", "F. Cheque", "Emisor", "F. Cobro", "Banco", "Sucursal", "Importe", "Estado", "Librado", "Cruzado", "Usuario"};
    private final int[] columnWidths = {1, 50, 50, 100, 50, 70, 70, 50, 30, 30, 20, 50};
    private final Class[] columnClassTypes = {Integer.class, Number.class, null, null, null, null, null, Number.class, null, null, Boolean.class, null};
    //Este se pone en el comboBox
    private final String[] orderByToComboBoxList = {"N° Cheque", "Fecha de Emisión", "Fecha de Cobro", "Importe", "Banco/Sucursal", "Cliente", "Estado"};
    //Y este es el equivalente (de lo seleccionado en el combo) para el SQL.
    private final String[] orderByToQueryKeyList = {"numero", "fecha_cheque", "fecha_cobro", "importe", "banco.nombre, banco_sucursal.nombre", "cliente.nombre", "estado"};

    public ChequeTercerosController() {
        jpaController = new ChequeTercerosJpaController();
    }

    ChequeTerceros initABM(Window owner, boolean isEditing, Cliente cliente) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        if (isEditing && EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila de la tabla");
        }

        panelABM = new PanelABMCheques();
        panelABM.setUIChequeTerceros();
        UTIL.loadComboBox(panelABM.getCbBancos(), new BancoController().findEntities(), true);
//        UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(panelABM.getCbEmisor(), new ClienteController().findEntities(), false);
        UTIL.setSelectedItem(panelABM.getCbEmisor(), cliente);
        UTIL.loadComboBox(panelABM.getCbLibrado(), new LibradoJpaController().findEntities(), false);

        panelABM.getCbBancos().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelABM.getCbBancos().getSelectedIndex() > 0) {
                    Banco banco = (Banco) panelABM.getCbBancos().getSelectedItem();
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), new BancoSucursalController().findBy(banco), false);
                } else {
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        if (isEditing) {
            setPanel(EL_OBJECT);
        }
        abm = new JDABM(owner, panelABM);
        abm.setTitle("ABM - Cheque Terceros");
        abm.setListener(this);
        abm.setVisible(true);
        return EL_OBJECT;
    }

    private void setPanel(ChequeTerceros EL_OBJECT) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private ChequeTerceros getEntity() throws MessageException {
        Date fechaCheque, fechaCobro, fechaEndoso = null;
        Cliente cliente;
        Banco banco;
        Long numero = null;
        BigDecimal importe = null;
        BancoSucursal sucursal = null;
        Librado librado = null;
        boolean cruzado, endosado;
        String endosatario = null, observacion = null;

        if (panelABM.getDcCheque() == null) {
            throw new MessageException("Debe ingresar la Fecha del Cheque.");
        }
        if (panelABM.getDcCobro() == null) {
            throw new MessageException("Debe ingresar la Fecha de cobro del Cheque.");
        }
        fechaCheque = panelABM.getDcCheque();
        fechaCobro = panelABM.getDcCobro();

        if (UTIL.compararIgnorandoTimeFields(fechaCheque, fechaCobro) == 1) {
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
        try {
            sucursal = (BancoSucursal) panelABM.getCbBancoSucursales().getSelectedItem();
        } catch (ClassCastException e) {
//            throw new MessageException("Sucursal de Banco no válida");
        }
        librado = (Librado) panelABM.getCbLibrado().getSelectedItem();
        cliente = (Cliente) panelABM.getCbEmisor().getSelectedItem();
        cruzado = panelABM.getCheckCruzado().isSelected();
        observacion = panelABM.getTaObservacion().getText();
        endosado = panelABM.getCheckEndosado().isSelected();
        if (endosado) {
            endosatario = panelABM.getTfEndosatario().getText().trim();
            if (endosatario == null || endosatario.length() < 1) {
                throw new MessageException("Debe especificar un Endosatario si há seleccionado la opción de endosado.");
            }
            fechaEndoso = panelABM.getDcEndoso();
            if (fechaEndoso == null) {
                throw new MessageException("Debe especificar fecha de endoso si há seleccionado la opción de endosado.");
            }
        }
        ChequeTerceros newCheque = new ChequeTerceros(cliente, numero, banco, sucursal, importe, fechaCheque, fechaCobro, cruzado, observacion, ChequeEstado.CARTERA, endosatario, fechaEndoso, UsuarioController.getCurrentUser(), librado);
        return newCheque;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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
                } else if (boton.getName().equalsIgnoreCase("bAddBanco")) {
                    try {
                        JDialog initABM = new BancoController().initABM(abm);
                        initABM.setLocationRelativeTo(abm);
                        initABM.setVisible(true);
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                    }
                    UTIL.loadComboBox(panelABM.getCbBancos(), new BancoController().findEntities(), true);
                    UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar Banco>");
                } else if (boton.getName().equalsIgnoreCase("bAddSucursal")) {
                    Banco bancoSelected = (Banco) panelABM.getCbBancos().getSelectedItem();
                    try {
                        JDialog initABM = new BancoSucursalController().initABM(abm);
                        initABM.setLocationRelativeTo(abm);
                        initABM.setVisible(true);
                        UTIL.setSelectedItem(panelABM.getCbBancos(), bancoSelected);
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                    }
                } else if (boton.getName().equalsIgnoreCase("baAddEmisor")) {
                    JDialog initContenedor = new ClienteController().initContenedor(null, true);
                    initContenedor.setVisible(true);
                }
            } //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="jdChequeManager EVENTS">
            else if (jdChequeManager != null) {
                if (boton.equals(jdChequeManager.getbBuscar())) {
                    armarQuery(false);
                } else if (boton.equals(jdChequeManager.getbLimpiar())) {
                } else if (boton.equals(jdChequeManager.getbACaja())) {
                } else if (boton.equals(jdChequeManager.getbAnular())) {
                } else if (boton.equals(jdChequeManager.getbDeposito())) {
                } else if (boton.equals(jdChequeManager.getbImprimir())) {
                }
            }
            //</editor-fold>
        }// </editor-fold>
    }

    private void checkConstraints(ChequeTerceros object) throws MessageException {
        ChequeTerceros cheque = jpaController.findBy(object.getBanco(), object.getNumero());
        if (cheque != null && !cheque.equals(object)) {
            throw new MessageException("Ya existe un Cheque del banco " + cheque.getBanco().getNombre() + " con el N° " + cheque.getNumero()
                    + "\nFecha emisión:" + UTIL.DATE_FORMAT.format(cheque.getFechaCheque())
                    + "\nFecha cobro: " + UTIL.DATE_FORMAT.format(cheque.getFechaCobro())
                    + "\nEstado:" + cheque.getChequeEstado());
        }
    }

    /**
     * Inicializa la vista encargada de administrar cheques (Propios o Terceros)
     *
     * @param parent JFrame padre.
     * @param listener Para los botones laterales. Si
     * <code>listener == null</code> se asignará <code>this</code> por defecto.
     * @return una instancia de {@link JDChequesManager}.
     */
    public JDialog initManager(JFrame parent, ActionListener listener) {
        initManager(parent);
        if (listener != null) {
            jdChequeManager.addButtonListener(listener);
        } else {
            jdChequeManager.addButtonListener(this);
            jdChequeManager.getLabelEmisor().setText("Emisor por");
            UTIL.loadComboBox(jdChequeManager.getCbEmisor(), new ClienteController().findEntities(), true);
            jdChequeManager.setTitle("Administración de Cheques Terceros");
        }
        return jdChequeManager;
    }

    private JDialog initManager(JFrame parent) {
        jdChequeManager = new JDChequesManager(parent, true);
        UTIL.loadComboBox(jdChequeManager.getCbBancos(), new BancoController().findEntities(), true);
        UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(jdChequeManager.getCbLibrado(), new LibradoJpaController().findEntities(), true);
        UTIL.loadComboBox(jdChequeManager.getCbEstados(), Arrays.asList(ChequeEstado.values()), true);
        UTIL.loadComboBox(jdChequeManager.getCbOrderBy(), Arrays.asList(orderByToComboBoxList), false);
        UTIL.getDefaultTableModel(jdChequeManager.getjTable1(), columnNames, columnWidths, columnClassTypes);
        UTIL.hideColumnTable(jdChequeManager.getjTable1(), 0);
        jdChequeManager.getCbBancos().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jdChequeManager.getCbBancos().getSelectedIndex() > 0) {
                    Banco banco = (Banco) jdChequeManager.getCbBancos().getSelectedItem();
                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), new BancoSucursalController().findBy(banco), true);
                } else {
                    UTIL.loadComboBox(jdChequeManager.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
                }
            }
        });
        return jdChequeManager;
    }

    private void cargarTablaChequeManager(String query) {
        DefaultTableModel dtm = UTIL.getDtm(jdChequeManager.getjTable1());
        UTIL.limpiarDtm(dtm);
    }

    private void armarQuery(boolean imprimir) {
        StringBuilder query = new StringBuilder("SELECT "
                + " c.id, c.numero, TO_CHAR(c.fecha_cheque,'DD/MM/YYYY'), cliente.nombre as cliente, TO_CHAR(c.fecha_cobro,'DD/MM/YYYY'),"
                + " banco.nombre as banco, banco_sucursal.nombre as sucursal,"
                + " c.importe, cheque_estado.nombre  as estado, librado.nombre as librado, c.cruzado,"
                + " usuario.nick as usuario, "
                + " c.endosatario, c.fecha_endoso, "
                + " c.bound, c.bound_id "
                + " FROM public.cheque_terceros c "
                + " JOIN public.banco ON (c.banco = banco.id) "
                + " JOIN public.banco_sucursal ON (c.banco_sucursal = banco_sucursal.id) "
                + " JOIN public.librado ON (c.librado = librado.id) "
                + " JOIN public.cliente ON (c.cliente = cliente.id) "
                + " JOIN public.usuario ON (c.usuario = usuario.id) "
                + " JOIN cheque_estado  ON (c.estado  = cheque_estado.id)"
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
            query.append(" AND c.banco=").append(((Banco) jdChequeManager.getCbBancos().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbBancoSucursales().getSelectedIndex() > 0) {
            query.append(" AND c.banco_sucursal=").append(((BancoSucursal) jdChequeManager.getCbBancoSucursales().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbEmisor().getSelectedIndex() > 0) {
            query.append(" AND c.cliente=").append(((Cliente) jdChequeManager.getCbEmisor().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbLibrado().getSelectedIndex() > 0) {
            query.append(" AND c.librado=").append(((Librado) jdChequeManager.getCbLibrado().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbEstados().getSelectedIndex() > 0) {
            query.append(" AND c.estado=").append(((ChequeEstado) jdChequeManager.getCbEstados().getSelectedItem()).getId());
        }

        query.append(" ORDER BY ").append(orderByToQueryKeyList[jdChequeManager.getCbOrderBy().getSelectedIndex()]);
        System.out.println(query.toString());
        cargarTablaChequeManager(query.toString());
        if (imprimir) {
            doChequeTercerosReport(query.toString());
        }
    }

    private void doChequeTercerosReport(String query) {
        throw new UnsupportedOperationException("Not yet implemented");
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
        abm = new JDABM(true, jdChequeManager, p);
        abm.setTitle("Asentar Cheque a Caja");
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

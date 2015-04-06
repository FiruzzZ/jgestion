package jgestion.controller;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import jgestion.controller.exceptions.DatabaseErrorException;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.entity.Banco;
import jgestion.entity.Caja;
import jgestion.entity.ChequeTerceros;
import jgestion.entity.ChequeTercerosEntrega;
import jgestion.entity.ChequeTercerosEntregaDetalle;
import jgestion.entity.Cliente;
import jgestion.entity.CuentabancariaMovimientos;
import jgestion.entity.DetalleCajaMovimientos;
import jgestion.entity.Usuario;
import jgestion.entity.UsuarioAcciones;
import jgestion.entity.enums.ChequeEstado;
import generics.GenericBeanCollection;
import jgestion.gui.JDABM;
import jgestion.gui.JDChequesManager;
import jgestion.gui.PanelABMCheques;
import jgestion.gui.PanelChequesColumnsReport;
import jgestion.gui.PanelEntregaTerceros;
import jgestion.gui.PanelMovimientosVarios;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jgestion.jpa.controller.ChequeTercerosJpaController;
import jgestion.jpa.controller.CuentabancariaMovimientosJpaController;
import jgestion.jpa.controller.UsuarioAccionesJpaController;
import jgestion.jpa.controller.UsuarioJpaController;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.RowColorRender;
import utilities.general.EntityWrapper;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author Administrador
 */
public class ChequeTercerosController implements ActionListener {

    private static final Logger LOG = Logger.getLogger(ChequePropioController.class);
    public final static int MAX_LENGHT_DIGITS_QUANTITY = 20;
    private ChequeTerceros EL_OBJECT;
    private JDABM abm;
    private PanelABMCheques panelABM;
    private final ChequeTercerosJpaController jpaController = new ChequeTercerosJpaController();
    //exclusively related to the GUI
    private JDChequesManager jdChequeManager;
    //Este se pone en el comboBox
    private final String[] orderByToComboBoxList = {"N° Cheque", "Fecha de Emisión", "Fecha de Cobro", "Importe", "Banco", "Cliente", "Estado"};
    //Y este es el equivalente (de lo seleccionado en el combo) para el SQL.
    private final String[] orderByToQueryKeyList = {"numero", "fecha_cheque", "fecha_cobro", "importe", "banco.nombre", "cliente.nombre", "estado"};
    private PanelEntregaTerceros panelEntregaTerceros;

    public ChequeTercerosController() {

    }

    /**
     *
     * @param owner
     * @param toReplace
     * @return instance of the ChequeTerceros persisted in replacement
     * @throws MessageException
     */
    private ChequeTerceros initABMReemplazo(Window owner, ChequeTerceros toReplace) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        initPanelABM(true, false);
        panelABM.getTfImporte().setText(UTIL.PRECIO_CON_PUNTO.format(toReplace.getImporte()));
        panelABM.getTaObservacion().setText("Reemplazo de: " + toReplace.getBanco().getNombre() + ", N°" + toReplace.getNumero());
//        panelABM.setPersistible(true); // <-- OJO ACA!!!
        abm = new JDABM(owner, null, true, panelABM);
        abm.setTitle("Reemplazo de Cheque N°" + toReplace.getNumero());
        abm.setListener(this);
        abm.setVisible(true);
        return EL_OBJECT;
    }

    public ChequeTerceros displayABM(Window owner, ChequeTerceros toEdit, Cliente cliente) throws MessageException {
        initABM(owner, toEdit, cliente);
        abm.setVisible(true);
        return EL_OBJECT;
    }

    /**
     * Esta ventana permite la creación de Cheques sin tener permiso
     * {@link PermisosController#PermisoDe#TESORERIA}.
     * <i>Para que se puedan cargar desde un Recibo sin tener acceso al todo el módulo</i>
     *
     * @param owner
     * @param toEdit
     * @param cliente
     * @throws MessageException
     */
    public void initABM(Window owner, ChequeTerceros toEdit, Cliente cliente) throws MessageException {
        initPanelABM(toEdit != null, false);
        if (cliente != null) {
            UTIL.setSelectedItem(panelABM.getCbEmisor(), cliente);
        }
        String editingText = "";
        if (toEdit != null) {
            EL_OBJECT = toEdit;
            setPanel(EL_OBJECT);
            editingText = " (editando)";
        }
        abm = new JDABM(owner, null, true, panelABM);
        abm.setTitle("ABM - Cheque Terceros" + editingText);
        abm.setListener(this);

    }

    private void initPanelABM(boolean persistir, boolean selectableCliente) {
        panelABM = new PanelABMCheques();
        panelABM.setUIChequeTerceros();
        panelABM.setPersistible(persistir);
        panelABM.setSinComprobante(selectableCliente);
        UTIL.loadComboBox(panelABM.getCbBancos(), new BancoController().findEntities(), true);
//        UTIL.loadComboBox(panelABM.getCbBancoSucursales(), null, null, "<Seleccionar un Banco>");
        UTIL.loadComboBox(panelABM.getCbEmisor(), new ClienteController().findAll(), selectableCliente);
        panelABM.getbAddBanco().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JDialog initABM = new BancoController().initABM(abm);
                    initABM.setLocationRelativeTo(abm);
                    initABM.setVisible(true);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                }
                UTIL.loadComboBox(panelABM.getCbBancos(), new BancoController().findEntities(), true);
            }
        });
        panelABM.getbAddEmisor().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog initContenedor = new ClienteController().initContenedor(null, true);
                initContenedor.setVisible(true);
                UTIL.loadComboBox(panelABM.getCbEmisor(), new ClienteController().findAll(), true);
            }
        });
    }

    private ChequeTerceros displayABMSinComprobante(Window owner) throws MessageException {
        EL_OBJECT = null;
        initABM(owner, null, null);
        panelABM.setPersistible(true);
        abm.setVisible(true);
        return EL_OBJECT;
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
        Cliente cliente = null;
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
        try {
            cliente = (Cliente) panelABM.getCbEmisor().getSelectedItem();
        } catch (ClassCastException e) {
            if (!panelABM.isSinComprobante()) {
            }
        }

        cruzado = panelABM.getCheckCruzado().isSelected();
        if (!panelABM.getTaObservacion().getText().trim().isEmpty()) {
            observacion = panelABM.getTaObservacion().getText().trim();
        }
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
        if (EL_OBJECT != null) {
            //los objetos con herencia no puede ser inicializados con new porque el ORM no reconoce la asociación
            EL_OBJECT.setCliente(cliente);
            EL_OBJECT.setNumero(numero);
            EL_OBJECT.setBanco(banco);
            EL_OBJECT.setImporte(importe);
            EL_OBJECT.setFechaCheque(fechaCheque);
            EL_OBJECT.setFechaCobro(fechaCobro);
            EL_OBJECT.setObservacion(observacion);
            EL_OBJECT.setEndosatario(endosatario);
            EL_OBJECT.setFechaEndoso(fechaEndoso);
            EL_OBJECT.setCruzado(cruzado);
        } else {
            EL_OBJECT = new ChequeTerceros(null, cliente, numero, banco, null, importe, fechaCheque,
                    fechaCobro, cruzado, observacion, ChequeEstado.CARTERA, endosatario, fechaEndoso,
                    UsuarioController.getCurrentUser());
        }
        ChequeTerceros cheque = jpaController.findBy(EL_OBJECT.getBanco(), EL_OBJECT.getNumero());
        if (cheque != null && !cheque.equals(EL_OBJECT)) {
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(null, "Ya existe un Cheque del banco " + cheque.getBanco().getNombre() + " con el N° " + cheque.getNumero()
                    + "\nImporte: " + UTIL.DECIMAL_FORMAT.format(cheque.getImporte())
                    + "\nFecha emisión:" + UTIL.DATE_FORMAT.format(cheque.getFechaCheque())
                    + "\nFecha cobro: " + UTIL.DATE_FORMAT.format(cheque.getFechaCobro())
                    + "\nEstado:" + cheque.getChequeEstado()
                    + "\nIngreso:" + cheque.getComprobanteIngreso()
                    + "\nEgreso:" + cheque.getComprobanteEgreso()
                    + "\n¿Desea continuar?")) {
                throw new MessageException("Carga sospechosa cancelada.. que suerte!");
            }
        }
        return EL_OBJECT;
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
//                            Integer id = EL_OBJECT != null ? EL_OBJECT.getId() : null;
                            getEntity();
                            checkConstraints(EL_OBJECT);
                            if (panelABM.persist()) {
                                String msg = EL_OBJECT.getId() == null ? "Registrado" : "Modificado";
                                if (EL_OBJECT.getId() == null) {
                                    jpaController.persist(EL_OBJECT);
                                } else {
                                    jpaController.merge(EL_OBJECT);
                                }
                                abm.showMessage(msg, jpaController.getEntityClass().getSimpleName(), 1);
                            }
//                            EL_OBJECT = cheque;
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
                    } else if (boton.equals(jdChequeManager.getBtnNuevo())) {
                        displayABMSinComprobante(jdChequeManager);
                    } else if (boton.equals(jdChequeManager.getBtnModificar())) {
                        int row = jdChequeManager.getjTable1().getSelectedRow();
                        if (row > -1) {
                            ChequeTerceros cheque = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(row, 0));
                            jpaController.closeEntityManager();
//                            if (cheque.getChequeEstado().equals(ChequeEstado.CARTERA)) {
                            displayABM(jdChequeManager, cheque, null);
//                                initACajaUI(cheque);
                            armarQuery(false);
//                            } else {
//                                JOptionPane.showMessageDialog(jdChequeManager, "Solo los cheques en " + ChequeEstado.CARTERA + " pueden ser acreditados a una Caja", "Error", JOptionPane.WARNING_MESSAGE);
//                            }
                        }
                    } else if (boton.equals(jdChequeManager.getbAnular())) {
                        int row = jdChequeManager.getjTable1().getSelectedRow();
                        if (row > -1) {
                            ChequeTerceros cheque = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(row, 0));
                            if (cheque.getChequeEstado().equals(ChequeEstado.ANULADO)) {
                                //recuperar estado previo a la anulación!!
                                cheque.setEstado(cheque.getEstadoPrevio());
                                cheque.setEstadoPrevio(null);
                                jpaController.merge(cheque);
                                String desc = "Deshizo Anulación " + ChequeTerceros.class.getSimpleName() + " N° " + cheque.getNumero();
                                UsuarioAcciones ua = new UsuarioAcciones('u', desc, null, ChequeTerceros.class.getSimpleName(), cheque.getId(), null);
                                new UsuarioAccionesController().create(ua);
                                armarQuery(false);
                            } else {
                                String motivo = JOptionPane.showInputDialog(jdChequeManager, "Ingrese motivo de la anulación:");
                                if (motivo == null || motivo.isEmpty()) {
                                    throw new MessageException("Motivo de anulación no válido");
                                }
                                if (motivo != null) {
                                    motivo = motivo.trim();
                                }
                                if (motivo.length() > 200) {
                                    throw new MessageException("No es para escribir una novela, solo una breve descripción de la anulación."
                                            + "\nMáximo 200 caracteres");
                                }
                                cheque.setEstadoPrevio(cheque.getEstado());
                                cheque.setEstado(ChequeEstado.ANULADO.getId());
                                cheque.setObservacion(Objects.toString(cheque.getObservacion(), "") + "; ANULADO: " + motivo);
                                jpaController.merge(cheque);
                                String desc = "Anuló " + ChequeTerceros.class.getSimpleName() + " N° " + cheque.getNumero() + "; motivo: " + motivo;
                                UsuarioAcciones ua = new UsuarioAcciones('u', desc, null, ChequeTerceros.class.getSimpleName(), cheque.getId(), null);
                                new UsuarioAccionesController().create(ua);
                                armarQuery(false);
                            }
                        }
                    } else if (boton.equals(jdChequeManager.getbDepositar())) {
                        int row = jdChequeManager.getjTable1().getSelectedRow();
                        if (row > -1) {
                            ChequeTerceros cheque = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(row, 0));
                            if (!cheque.getChequeEstado().equals(ChequeEstado.CARTERA)) {
                                JOptionPane.showMessageDialog(jdChequeManager, "Solo los cheques en " + ChequeEstado.CARTERA + " pueden ser depositados", "Error", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            new CuentabancariaController().initDepositoUI(jdChequeManager, cheque);
                            armarQuery(false);
                        }
                    } else if (boton.equals(jdChequeManager.getBtnReemplazar())) {
                        int row = jdChequeManager.getjTable1().getSelectedRow();
                        if (row > -1) {
                            ChequeTerceros toReplace = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(row, 0));
                            if (toReplace.getChequeEstado().equals(ChequeEstado.REEMPLAZADO)) {
                                throw new MessageException("El cheque ya fue reemplazado, en las observaciones de este se encuentra la información del reemplazo");
                            }
                            boolean eliminarCBM = false;
                            if (toReplace.getChequeEstado().equals(ChequeEstado.DEPOSITADO)
                                    || toReplace.getChequeEstado().equals(ChequeEstado.ENDOSADO)) {
                                if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(jdChequeManager,
                                        "La modificación de un cheque " + ChequeEstado.DEPOSITADO + " o " + ChequeEstado.ENDOSADO + " implica"
                                        + "\nla eliminación del registro de movimiento de cuenta bancaria asociado a este."
                                        + "\nConfirmar para continuar..",
                                        "Reemplazo de cheque", JOptionPane.YES_NO_OPTION)) {
                                    return;
                                }
                                eliminarCBM = true;
                            }
                            ChequeTerceros reemplazo = initABMReemplazo(jdChequeManager, toReplace);
                            if (reemplazo != null) {
                                toReplace.setEstado(ChequeEstado.REEMPLAZADO.getId());
                                toReplace.setObservacion(Objects.toString(toReplace.getObservacion(), "") + " Reemplazado por: " + reemplazo.getBanco().getNombre() + " N° " + reemplazo.getNumero());
                                if (toReplace.getObservacion().length() > 300) {
                                    toReplace.setObservacion(toReplace.getObservacion().substring(0, 300));
                                }
                                jpaController.merge(toReplace);
                                if (eliminarCBM) {
                                    CuentabancariaMovimientosJpaController cmjc = new CuentabancariaMovimientosJpaController();
                                    CuentabancariaMovimientos cbm = cmjc.findBy(toReplace);
                                    cmjc.remove(cbm);
                                }
                            }
                            armarQuery(false);
                        }
                    } else if (boton.equals(jdChequeManager.getBtnRechazar())) {
                        int row = jdChequeManager.getjTable1().getSelectedRow();
                        if (row > -1) {
                            ChequeTerceros toReject = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(row, 0));
                            if (toReject.getChequeEstado().equals(ChequeEstado.RECHAZADO)) {
                                throw new MessageException("El cheque ya está rechazado");
                            }
                            if (toReject.getChequeEstado().equals(ChequeEstado.DEPOSITADO)
                                    || toReject.getChequeEstado().equals(ChequeEstado.CARTERA)) {
                                throw new MessageException("Solo un cheque " + ChequeEstado.DEPOSITADO + " o en " + ChequeEstado.CARTERA
                                        + " puede ser marcado como " + ChequeEstado.RECHAZADO);
                            }
                            boolean eliminarCBM = false;
                            if (toReject.getChequeEstado().equals(ChequeEstado.DEPOSITADO)
                                    || toReject.getChequeEstado().equals(ChequeEstado.ENDOSADO)) {
                                if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(jdChequeManager,
                                        "La modificación de un cheque " + ChequeEstado.DEPOSITADO + " o " + ChequeEstado.ENDOSADO + " implica"
                                        + "\nla eliminación del registro de movimiento de cuenta bancaria asociado a este."
                                        + "\nConfirmar para continuar..",
                                        "Reemplazo de cheque", JOptionPane.YES_NO_OPTION)) {
                                    return;
                                }
                                eliminarCBM = true;
                            }
                            toReject.setEstado(ChequeEstado.RECHAZADO.getId());
                            UsuarioAcciones ua = UsuarioAccionesController.build(toReject, toReject.getId(), "Rechazó cheque " + toReject.toString(), null, 'u');
                            new UsuarioAccionesController().create(ua);
                            jpaController.merge(toReject);
                            if (eliminarCBM) {
                                CuentabancariaMovimientosJpaController cmjc = new CuentabancariaMovimientosJpaController();
                                CuentabancariaMovimientos cbm = cmjc.findBy(toReject);
                                if (cbm != null) {
                                    //cuando está en cartera el cheque no tiene movimiento
                                    cmjc.remove(cbm);
                                }
                            }
                            armarQuery(false);
                        }
                    } else if (boton.equals(jdChequeManager.getBtnImprimir())) {
                        try {
                            armarQuery(true);
                        } catch (DatabaseErrorException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error ejecutando consulta", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                //</editor-fold>
            }// </editor-fold>
        } catch (MessageException ex) {
            ex.displayMessage(null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Error on actionPerformed", JOptionPane.ERROR_MESSAGE);
            LOG.error(ex, ex);
        }
    }

    private void checkConstraints(ChequeTerceros object) throws MessageException {
//        ChequeTerceros cheque = jpaController.findBy(object.getBanco(), object.getNumero());
//        if (cheque != null && !cheque.equals(object)) {
//            throw new MessageException("Ya existe un Cheque del banco " + cheque.getBanco().getNombre() + " con el N° " + cheque.getNumero()
//                    + "\nFecha emisión:" + UTIL.DATE_FORMAT.format(cheque.getFechaCheque())
//                    + "\nFecha cobro: " + UTIL.DATE_FORMAT.format(cheque.getFechaCobro())
//                    + "\nEstado:" + cheque.getChequeEstado()
//                    + "\nIngreso:" + cheque.getComprobanteIngreso()
//                    + "\nEgreso:" + cheque.getComprobanteEgreso());
//        }
    }

    /**
     * Inicializa la vista encargada de administrar cheques (Propios o Terceros)
     *
     * @param owner papi.
     * @return una instancia de {@link JDChequesManager}.
     * @throws MessageException
     */
    public JDialog getManager(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        initManager(owner);
        jdChequeManager.setLocationRelativeTo(owner);
        return jdChequeManager;
    }

    public ChequeTerceros initManagerBuscador(Window owner) {
        initManager(owner);
        EL_OBJECT = null;
        UTIL.setSelectedItem(jdChequeManager.getCbEstados(), ChequeEstado.CARTERA);
        jdChequeManager.getCbEstados().setEnabled(false);
        jdChequeManager.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1 && jdChequeManager.getjTable1().getSelectedRow() > -1) {
                    ChequeTerceros cheque = jpaController.find((Integer) jdChequeManager.getjTable1().getModel().getValueAt(jdChequeManager.getjTable1().getSelectedRow(), 0));
                    if (cheque.getChequeEstado().equals(ChequeEstado.CARTERA)) {
                        EL_OBJECT = cheque;
                        jdChequeManager.dispose();
                    } else {
                        JOptionPane.showMessageDialog(jdChequeManager, "Solo los cheques en " + ChequeEstado.CARTERA + " pueden ser utilizados como medio de pago", "Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });
        jdChequeManager.setLocationRelativeTo(owner);
        jdChequeManager.setVisible(true);
        return EL_OBJECT;
    }

    private void initManager(Window owner) {
        jdChequeManager = new JDChequesManager(owner, true);
        jdChequeManager.setTitle("Administración de Cheques Terceros");
        jdChequeManager.getLabelEmisor().setText("Emisor");
        jdChequeManager.getCbBancoSucursales().setVisible(false);
        jdChequeManager.getLabelSucursales().setVisible(false);
        jdChequeManager.getCbCuentaBancaria().setVisible(false);
        jdChequeManager.getLabelCuentaBancaria().setVisible(false);
        UTIL.loadComboBox(jdChequeManager.getCbBancos(), JGestionUtils.getWrappedBancos(new BancoController().findEntities()), true);
        UTIL.loadComboBox(jdChequeManager.getCbEstados(), Arrays.asList(ChequeEstado.values()), true);
        UTIL.loadComboBox(jdChequeManager.getCbOrderBy(), Arrays.asList(orderByToComboBoxList), false);
        jdChequeManager.getCbOrderBy().setSelectedIndex(2);
        UTIL.loadComboBox(jdChequeManager.getCbEmisor(), JGestionUtils.getWrappedClientes(new ClienteController().findAll()), true);
        UTIL.getDefaultTableModel(jdChequeManager.getjTable1(),
                // los valores de las columnas 4 (F. Cobro) y 7 (Estado) son usados en otros lados!!! ojo piojo!
                new String[]{"id", "Nº Cheque", "F. Cheque", "Emisor", "F. Cobro", "Banco", "Importe", "Estado", "Cruzado", "Endosatario", "F. Endoso", "C. Ingreso", "C. Egreso", "Observacion", "Tenedor/a"},
                new int[]{1, 80, 80, 100, 80, 100, 100, 100, 50, 100, 100, 150, 150, 100, 80},
                new Class<?>[]{Integer.class, Number.class, null, null, null, null, BigDecimal.class, null, Boolean.class, null});
        jdChequeManager.getjTable1().setAutoCreateRowSorter(true);
        jdChequeManager.getjTable1().getColumnModel().getColumn(1).setCellRenderer(NumberRenderer.getNumberRenderer());
        jdChequeManager.getjTable1().getColumnModel().getColumn(2).setCellRenderer(FormatRenderer.getDateRenderer());
        jdChequeManager.getjTable1().getColumnModel().getColumn(4).setCellRenderer(FormatRenderer.getDateRenderer());
        jdChequeManager.getjTable1().getColumnModel().getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        jdChequeManager.getjTable1().getColumnModel().getColumn(10).setCellRenderer(FormatRenderer.getDateRenderer());
        jdChequeManager.getjTable1().setDefaultRenderer(Object.class, new RowColorRender() {
            private static final long serialVersionUID = 1L;
            final Date hoy = new Date();

            @Override
            public Color condicionByRow(int row) {
                Color c = null;
                Date fechaCobro = (Date) jdChequeManager.getjTable1().getModel().getValueAt(row, 4);
                String estado = jdChequeManager.getjTable1().getModel().getValueAt(row, 7).toString();
                if (fechaCobro.before(hoy) && ChequeEstado.CARTERA.toString().equalsIgnoreCase(estado)) {
                    c = Color.RED;
                }
                return c;
            }
        });
        UTIL.hideColumnTable(jdChequeManager.getjTable1(), 0);
        jdChequeManager.addButtonsListener(this);
        jdChequeManager.getjTable1().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                Integer chequeID = (Integer) UTIL.getSelectedValue(jdChequeManager.getjTable1(), 0);
                if (chequeID != null) {
                    ChequeTerceros cheque = jpaController.find(chequeID);
                    if (cheque.getEstado() == ChequeEstado.ANULADO.getId()) {
                        jdChequeManager.getbAnular().setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/24px_undo_arrow.png")));
                        jdChequeManager.getbAnular().setText("Des-Anular");
                    } else {
                        jdChequeManager.getbAnular().setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cancelar.png")));
                        jdChequeManager.getbAnular().setText("Anular");
                    }
                }
            }
        });
    }

    private void cargarTablaChequeManager(String query) throws DatabaseErrorException {
        DefaultTableModel dtm = UTIL.getDtm(jdChequeManager.getjTable1());
        dtm.setRowCount(0);
        List<?> l = DAO.getNativeQueryResultList(query);
        if (l.isEmpty()) {
            JOptionPane.showMessageDialog(jdChequeManager, "La busqueda no produjo ningún resultado.\nUtilice otras opciones de filtro, también puede que no disponga de ningún en su cartera.");
        }
        for (Object object : l) {
            //"id", "Nº Cheque", "F. Cheque", "Emisor", "F. Cobro", "Banco", "Importe", "Estado", "Cruzado", "Endosatario", "F. Endoso", "C. Ingreso", "C. Egreso", "Observacion", "Usuario"};
            dtm.addRow((Object[]) object);
        }
        totalizarSegunFechaCobro();
    }

    @SuppressWarnings("unchecked")
    private void armarQuery(boolean imprimir) throws DatabaseErrorException {
        StringBuilder query = new StringBuilder("SELECT "
                + " c.id, c.numero, c.fecha_cheque, cliente.nombre as cliente, c.fecha_cobro, banco.nombre as banco, c.importe, cheque_estado.nombre as estado, c.cruzado"
                + ", c.endosatario, c.fecha_endoso, c.comprobante_ingreso, c.comprobante_egreso, c.observacion, usuario.nick as usuario "
                + " FROM cheque_terceros c "
                + " JOIN banco ON (c.banco = banco.id) "
                + " LEFT JOIN cliente ON (c.cliente = cliente.id) "
                + " JOIN usuario ON (c.usuario = usuario.id) "
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
            query.append(" AND c.fecha_cheque >='").append(UTIL.DATE_FORMAT.format(jdChequeManager.getDcEmisionDesde())).append("'");
        }
        if (jdChequeManager.getDcEmisionHasta() != null) {
            query.append(" AND c.fecha_cheque <='").append(UTIL.DATE_FORMAT.format(jdChequeManager.getDcEmisionHasta())).append("'");
        }
        if (jdChequeManager.getDcCobroDesde() != null) {
            query.append(" AND c.fecha_cobro >='").append(UTIL.DATE_FORMAT.format(jdChequeManager.getDcCobroDesde())).append("'");
        }
        if (jdChequeManager.getDcCobroHasta() != null) {
            query.append(" AND c.fecha_cobro <='").append(UTIL.DATE_FORMAT.format(jdChequeManager.getDcCobroHasta())).append("'");
        }
        try {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.CHEQUES_ADMINISTRADOR);
        } catch (MessageException ex) {
            query.append(" AND usuario.id=").append(UsuarioController.getCurrentUser().getId());
        }

        if (jdChequeManager.getTfImporte().getText().trim().length() > 0) {
            try {
                Double importe = Double.valueOf(jdChequeManager.getTfImporte().getText());
                query.append(" AND c.importe").append(jdChequeManager.getCbImporteCondicion().getSelectedItem().toString()).append(importe);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        if (jdChequeManager.getCbBancos().getSelectedIndex() > 0) {
            query.append(" AND c.banco=").append(((EntityWrapper<Banco>) jdChequeManager.getCbBancos().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbEmisor().getSelectedIndex() > 0) {
            query.append(" AND c.cliente=").append(((EntityWrapper<Cliente>) jdChequeManager.getCbEmisor().getSelectedItem()).getId());
        }
        if (jdChequeManager.getCbEstados().getSelectedIndex() > 0) {
            query.append(" AND c.estado=").append(((ChequeEstado) jdChequeManager.getCbEstados().getSelectedItem()).getId());
        }

        query.append(" ORDER BY ").append(orderByToQueryKeyList[jdChequeManager.getCbOrderBy().getSelectedIndex()]);
        LOG.debug(query.toString());
        cargarTablaChequeManager(query.toString());
        if (imprimir) {
            if (jdChequeManager.getjTable1().getModel().getRowCount() < 1) {
                JOptionPane.showMessageDialog(jdChequeManager, "No sea han filtrados cheques para crear el reporte");
            } else {
                doChequeTercerosReport();
            }
        }
    }

    private void doChequeTercerosReport() throws DatabaseErrorException {
        final PanelChequesColumnsReport p = new PanelChequesColumnsReport();
        final JDABM jd = new JDABM(null, "Informes: Cheques de Terceros", true, p);
        jd.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    Reportes r = new Reportes(null, true);
                    r.showWaitingDialog();
                    //"id",
                    //"Nº Cheque", "F. Cheque", "Emisor", "F. Cobro", "Banco", 
                    //"Importe", "Estado", "Cruzado", "Endosatario", "F. Endoso", 
                    //"C. Ingreso", "C. Egreso", "Observacion", "Usuario"};
                    DefaultTableModel dtm = (DefaultTableModel) jdChequeManager.getjTable1().getModel();
                    List<GenericBeanCollection> data = new ArrayList<>(dtm.getRowCount());
                    for (int row = 0; row < dtm.getRowCount(); row++) {
                        data.add(new GenericBeanCollection(
                                dtm.getValueAt(row, 1),
                                dtm.getValueAt(row, 2),
                                dtm.getValueAt(row, 3),
                                dtm.getValueAt(row, 4),
                                dtm.getValueAt(row, 5),
                                dtm.getValueAt(row, 6),
                                dtm.getValueAt(row, 7),
                                dtm.getValueAt(row, 8),
                                dtm.getValueAt(row, 11),
                                dtm.getValueAt(row, 12),
                                dtm.getValueAt(row, 13),
                                dtm.getValueAt(row, 14)));
                    }

                    DynamicReportBuilder drb = new DynamicReportBuilder();
                    Style currencyStyle = new Style();
                    currencyStyle.setFont(Font.ARIAL_MEDIUM);
                    currencyStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
                    Style textStyle = new Style();
                    textStyle.setFont(Font.ARIAL_MEDIUM);
                    drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o1", Object.class).setTitle("Número").setWidth(60).setStyle(currencyStyle).setFixedWidth(true).build());
                    drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o6", Object.class).setTitle("Importe").setWidth(80).setStyle(currencyStyle).setPattern("¤ #,##0.00").setFixedWidth(true).build());
                    if (p.getCheckFechaCheque()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o2", Object.class).setTitle("F. Cheque").setWidth(60).setPattern("dd/MM/yyyy").setFixedWidth(true).build());
                    }
                    if (p.getCheckEmisor()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o3", Object.class).setTitle("Emisor").setWidth(200).setStyle(textStyle).setFixedWidth(true).build());
                    }
                    if (p.getCheckFechaCobro()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o4", Object.class).setTitle("F. Cobro").setWidth(60).setPattern("dd/MM/yyyy").setFixedWidth(true).build());
                    }
                    if (p.getCheckBanco()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o5", Object.class).setTitle("Banco").setWidth(80).setStyle(textStyle).build());
                    }
                    if (p.getCheckEstado()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o7", Object.class).setTitle("Estado").setWidth(45).setStyle(textStyle).build());
                    }
                    if (p.getCheckCruzado()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o8", Object.class).setTitle("Cruzado").setWidth(40).setStyle(textStyle).build());
                    }
                    if (p.getCheckCompIngreso()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o9", Object.class).setTitle("Comp. Ingreso").setWidth(100).setStyle(textStyle).build());
                    }
                    if (p.getCheckCompEgreso()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o10", Object.class).setTitle("Comp. Egreso").setWidth(100).setStyle(textStyle).build());
                    }
                    if (p.getCheckObservacion()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o11", Object.class).setTitle("Observ.").setWidth(100).setStyle(textStyle).build());
                    }
                    if (p.getCheckUsuario()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("o12", Object.class).setTitle("Usuario").setWidth(50).setStyle(textStyle).setFixedWidth(true).build());
                    }
                    if (p.getCheckLandscapePage()) {
                        drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
                    }
                    drb.setTitle("Informe: Cheques de Terceros")
                            .setSubtitle(UTIL.TIMESTAMP_FORMAT.format(new Date()))
                            .setPrintBackgroundOnOddRows(true)
                            .setUseFullPageWidth(true);
                    //                    SubReportBuilder srb = new SubReportBuilder();
                    //                    srb.setPathToReport(Reportes.FOLDER_REPORTES + "JGestion_membrete.jasper");
                    //                    srb.setDataSource("");
                    //                    Subreport sr = srb.build();
                    //                    drb.addSubreportInGroupHeader(0, sr);
                    DynamicReport dr = drb.build();
                    JRDataSource ds = new JRBeanCollectionDataSource(data);
                    JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);
                    r.setjPrint(jp);
                    r.viewReport();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "algo salió mal");
                    LOG.error("Creando dynamic report chequeterceros", ex);
                }
            }
        });
        jd.getbCancelar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                jd.dispose();
            }
        });
        jd.setVisible(true);
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

    private void totalizarSegunFechaCobro() {
        BigDecimal $cobrables = BigDecimal.ZERO;
        BigDecimal $30 = BigDecimal.ZERO;
        BigDecimal $60 = BigDecimal.ZERO;
        BigDecimal $90 = BigDecimal.ZERO;
        BigDecimal $90mas = BigDecimal.ZERO;
        Date now = new Date();
        DefaultTableModel dtm = (DefaultTableModel) jdChequeManager.getjTable1().getModel();
        final long dayOnMilli = 24 * 60 * 60 * 1000;
        for (int row = 0; row < dtm.getRowCount(); row++) {
            if (dtm.getValueAt(row, 7).toString().equalsIgnoreCase(ChequeEstado.CARTERA.name())) {
                Date fechaCobro = (Date) dtm.getValueAt(row, 4);
                BigDecimal importe = (BigDecimal) dtm.getValueAt(row, 6);
                long diff = fechaCobro.getTime() - now.getTime();
                long diffDays = diff / (dayOnMilli);
                if (diffDays <= 0) {
                    $cobrables = $cobrables.add(importe);
                } else if (diffDays <= 30) {
                    $30 = $30.add(importe);
                } else if (diffDays <= 60) {
                    $60 = $60.add(importe);
                } else if (diffDays <= 90) {
                    $90 = $90.add(importe);
                } else if (diffDays > 90) {
                    $90mas = $90mas.add(importe);
                }
            }
        }
        jdChequeManager.getTfCobrables().setText(UTIL.DECIMAL_FORMAT.format($cobrables));
        jdChequeManager.getTf30().setText(UTIL.DECIMAL_FORMAT.format($30));
        jdChequeManager.getTf60().setText(UTIL.DECIMAL_FORMAT.format($60));
        jdChequeManager.getTf90().setText(UTIL.DECIMAL_FORMAT.format($90));
        jdChequeManager.getTf90mas().setText(UTIL.DECIMAL_FORMAT.format($90mas));
    }

    private void initACajaUI(final ChequeTerceros cheque) throws MessageException {
        final CajaMovimientosController cmController = new CajaMovimientosController();
        abm = (JDABM) cmController.getABMMovimientosVarios(jdChequeManager, true);
        final PanelMovimientosVarios panelMovVarios = (PanelMovimientosVarios) abm.getPanel();
        panelMovVarios.getTfDescripcion().setText("CH " + cheque.getNumero() + " - " + cheque.getCliente().getNombre());
        panelMovVarios.getTfMontoMovimiento().setText(cheque.getImporte().toString());
        panelMovVarios.getTfMontoMovimiento().setEditable(false);
        panelMovVarios.getRadioEgreso().setEnabled(false);

        //quita el actionListener para la creación de un MovimientoVario
        ActionListener[] actionListeners = abm.getbAceptar().getActionListeners();
        for (ActionListener o : actionListeners) {
            abm.getbAceptar().removeActionListener(o);
        }
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    abm.getbAceptar().setEnabled(false);
                    String descripcion = panelMovVarios.getTfDescripcion().getText().trim();
                    DetalleCajaMovimientos dcm = cmController.setMovimientoVarios();
                    dcm.setDescripcion(descripcion);
                    dcm.setNumero(cheque.getId());
                    dcm.setTipo(DetalleCajaMovimientosController.CHEQUE_TERCEROS);
                    new DetalleCajaMovimientosController().create(dcm);
                    cheque.setComprobanteEgreso("A Caja: " + dcm.getCajaMovimientos().getCaja().getNombre() + "(" + dcm.getCajaMovimientos().getId() + ")");
                    cheque.setEstado(ChequeEstado.ACREDITADO_EN_CAJA.getId());
                    jpaController.merge(cheque);
                    abm.showMessage("Movimientos Nº" + dcm.getId() + " realizado.", "Movimientos Nº" + dcm.getId(), 1);
                    abm.dispose();
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), "Error", 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), "Error", 0);
                } finally {
                    abm.getbAceptar().setEnabled(true);
                }
            }
        });
        abm.setLocationRelativeTo(jdChequeManager);
        abm.toFront();
        abm.setVisible(true);
    }

    public void showEntregas(Window owner) {
        panelEntregaTerceros = new PanelEntregaTerceros();
//        UTIL.loadComboBox(panelEntregaTerceros.getCbUsuarioEmisor(), JGestionUtils.getWrappedUsuarios(new UsuarioJpaController().findWithCheques()), true);
        UTIL.loadComboBox(panelEntregaTerceros.getCbUsuarioReceptor(), JGestionUtils.getWrappedUsuarios(new UsuarioJpaController().findByEstado(true)), false);
        UTIL.getDefaultTableModel(panelEntregaTerceros.getTableDisponibles(),
                new String[]{"ChequeTercero.object", "Número", "Banco", "Importe"}, new int[]{1, 100, 100, 100});
        panelEntregaTerceros.getTableDisponibles().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(panelEntregaTerceros.getTableDisponibles(), 0);
        UTIL.getDefaultTableModel(panelEntregaTerceros.getTableEntregados(),
                new String[]{"ChequeTercero.object", "Número", "Banco", "Importe"}, new int[]{1, 100, 100, 100});
        panelEntregaTerceros.getTableEntregados().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(panelEntregaTerceros.getTableEntregados(), 0);
//        panelEntregaTerceros.getCbUsuarioEmisor().addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (panelEntregaTerceros.getTableEntregados().getSelectedRows().length > 0) {
//                }
//                loadChequesEnCartera();
//                refreshTotales();
//            }
//        });
        panelEntregaTerceros.getBtnADD().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelEntregaTerceros.getTableDisponibles().getSelectedRows().length > 0) {
                    DefaultTableModel dtmDisponibles = (DefaultTableModel) panelEntregaTerceros.getTableDisponibles().getModel();
                    DefaultTableModel dtmEntregados = (DefaultTableModel) panelEntregaTerceros.getTableEntregados().getModel();
                    for (int row : panelEntregaTerceros.getTableDisponibles().getSelectedRows()) {
                        ChequeTerceros c = (ChequeTerceros) dtmDisponibles.getValueAt(row, 0);
                        dtmEntregados.addRow(new Object[]{c, c.getNumero(), c.getBanco().getNombre(), c.getImporte()});
                    }
                    loadChequesEnCartera();
                    refreshTotales();
                } else {
                    JOptionPane.showMessageDialog(abm, "Debe seleccionar al menos un cheque disponible");
                }
            }
        });
        panelEntregaTerceros.getBtnDEL().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelEntregaTerceros.getTableEntregados().getSelectedRows().length > 0) {
                    UTIL.removeSelectedRows(panelEntregaTerceros.getTableEntregados());
                    loadChequesEnCartera();
                    refreshTotales();
                } else {
                    JOptionPane.showMessageDialog(abm, "Seleccione los cheques que desea quitar");
                }
            }
        });
        abm = new JDABM(owner, "Entrega de Cheques", true, panelEntregaTerceros);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ChequeTercerosEntrega cte = createEntregaTerceros();
                    DAO.create(cte);
                    for (ChequeTercerosEntregaDetalle d : cte.getDetalle()) {
                        DAO.merge(d.getChequeTerceros());
                    }
                    JOptionPane.showMessageDialog(abm, "Entrega N°" + cte.getId() + " registrada");
                    loadChequesEnCartera();
                    DefaultTableModel dtm = (DefaultTableModel) panelEntregaTerceros.getTableEntregados().getModel();
                    dtm.setRowCount(0);
                    refreshTotales();
                    doReportRendicion(cte);
                } catch (MessageException ex) {
                    ex.displayMessage(abm);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(abm, "Algo salió mal\n" + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    LOG.error("creando entregaTerceros", ex);
                }
            }
        });
        loadChequesEnCartera();
        refreshTotales();
        abm.setVisible(true);
    }

    private void doReportRendicion(ChequeTercerosEntrega cte) throws MissingReportException, JRException {
        Reportes r = new Reportes("JGestion_ChequeTercerosRendicion.jasper", "Cheque Terceros Rendición N°" + cte.getId());
        r.addParameter("ENTITY_ID", cte.getId());
        r.addMembreteParameter();
        r.printReport(true);
    }

    @SuppressWarnings("unchecked")
    private ChequeTercerosEntrega createEntregaTerceros() throws MessageException {
//        if (panelEntregaTerceros.getCbUsuarioEmisor().getSelectedIndex() < 1) {
//            throw new MessageException("Usuario emisor no válido");
//        }
//        Usuario emisor = ((EntityWrapper<Usuario>) panelEntregaTerceros.getCbUsuarioEmisor().getSelectedItem()).getEntity();
        Usuario emisor = UsuarioController.getCurrentUser();
        Usuario receptor = ((EntityWrapper<Usuario>) panelEntregaTerceros.getCbUsuarioReceptor().getSelectedItem()).getEntity();
        if (emisor.equals(receptor)) {
            throw new MessageException("Emisor y Receptor no puede ser iguales, ¿no te parece?");
        }
        DefaultTableModel dtm = (DefaultTableModel) panelEntregaTerceros.getTableEntregados().getModel();
        if (dtm.getRowCount() < 1) {
            throw new MessageException("No ha agregado ningún cheque a la entrega");
        }
        ChequeTercerosEntrega o = new ChequeTercerosEntrega(emisor, receptor, null);
        List<ChequeTercerosEntregaDetalle> detalle = new ArrayList<>(dtm.getRowCount());
        for (int row = 0; row < dtm.getRowCount(); row++) {
            ChequeTerceros c = (ChequeTerceros) dtm.getValueAt(row, 0);
            c.setUsuario(receptor);
            detalle.add(new ChequeTercerosEntregaDetalle(c, o));
        }
        o.setDetalle(detalle);
        return o;
    }

    private void loadChequesEnCartera() {
        @SuppressWarnings("unchecked")
        Usuario u = UsuarioController.getCurrentUser();
        String query = "SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o"
                + " WHERE o.estado=" + ChequeEstado.CARTERA.getId() + " AND o.usuario.id=" + u.getId();
        DefaultTableModel dtm = (DefaultTableModel) panelEntregaTerceros.getTableEntregados().getModel();
        if (dtm.getRowCount() > 0) {
            for (int row = 0; row < dtm.getRowCount(); row++) {
                ChequeTerceros c = (ChequeTerceros) dtm.getValueAt(row, 0);
                query += " AND o.id <> " + c.getId();
            }
        }
        List<ChequeTerceros> l = jpaController.findAll(query);
        dtm = (DefaultTableModel) panelEntregaTerceros.getTableDisponibles().getModel();
        dtm.setRowCount(0);
        for (ChequeTerceros c : l) {
            dtm.addRow(new Object[]{c, c.getNumero(), c.getBanco().getNombre(), c.getImporte()});
        }
    }

    private void refreshTotales() {
        DefaultTableModel dtm = (DefaultTableModel) panelEntregaTerceros.getTableDisponibles().getModel();
        BigDecimal total = BigDecimal.ZERO;
        for (int row = 0; row < dtm.getRowCount(); row++) {
            ChequeTerceros c = (ChequeTerceros) dtm.getValueAt(row, 0);
            total = total.add(c.getImporte());
        }
        panelEntregaTerceros.getTfTotalDisponible().setText(UTIL.DECIMAL_FORMAT.format(total));

        total = BigDecimal.ZERO;
        dtm = (DefaultTableModel) panelEntregaTerceros.getTableEntregados().getModel();
        for (int row = 0; row < dtm.getRowCount(); row++) {
            ChequeTerceros c = (ChequeTerceros) dtm.getValueAt(row, 0);
            total = total.add(c.getImporte());
        }
        panelEntregaTerceros.getTfTotalRecepcion().setText(UTIL.DECIMAL_FORMAT.format(total));
    }
}

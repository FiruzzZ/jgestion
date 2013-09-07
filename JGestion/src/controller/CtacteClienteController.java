package controller;

import controller.exceptions.*;
import entity.*;
import utilities.general.UTIL;
import gui.JDBuscador;
import gui.JDResumenCtaCtes;
import gui.PanelCtaCteCheckVencimientos;
import gui.generics.JDialogTable;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jpa.controller.CreditoProveedorJpaController;
import jpa.controller.NotaCreditoJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Logger;
import utilities.swing.components.ComboBoxWrapper;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author Administrador
 */
public class CtacteClienteController implements ActionListener {

    public static final String CLASS_NAME = CtacteCliente.class.getSimpleName();
    private JDResumenCtaCtes resumenCtaCtes;
    private double totalDebe;  //<----
    private double totalHaber; //<----
    private PanelCtaCteCheckVencimientos panelCCCheck;
    private JDBuscador buscador;
    private static Logger LOG = Logger.getLogger(CtacteClienteController.class);

    // <editor-fold defaultstate="collapsed" desc="CRUD...">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(CtacteCliente ctacteCliente) throws PreexistingEntityException, Exception {

        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(ctacteCliente);
            em.getTransaction().commit();
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(CtacteCliente ctacteCliente) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ctacteCliente = em.merge(ctacteCliente);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CtacteCliente ctacteCliente;
            try {
                ctacteCliente = em.getReference(CtacteCliente.class, id);
                ctacteCliente.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The ctacteCliente with id " + id + " no longer exists.", enfe);
            }
            em.remove(ctacteCliente);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<CtacteCliente> findCtacteClienteEntities() {
        return findCtacteClienteEntities(true, -1, -1);
    }

    public List<CtacteCliente> findCtacteClienteEntities(int maxResults, int firstResult) {
        return findCtacteClienteEntities(false, maxResults, firstResult);
    }

    private List<CtacteCliente> findCtacteClienteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from CtacteCliente as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public CtacteCliente findCtacteCliente(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(CtacteCliente.class, id);
        } finally {
            em.close();
        }
    }

    public int getCtacteClienteCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from CtacteCliente as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    void nuevaCtaCte(FacturaVenta facturaVenta) throws Exception {
        LOG.trace("adding CCC Nº" + facturaVenta);
        CtacteCliente ccp = new CtacteCliente();
        ccp.setDias((short) facturaVenta.getDiasCtaCte());
        ccp.setEntregado(0.0); //monto $$
        ccp.setEstado((Valores.CtaCteEstado.PENDIENTE.getId()));
        ccp.setFactura(facturaVenta);
        ccp.setFechaCarga(facturaVenta.getFechaalta());
        ccp.setImporte(facturaVenta.getImporte());
        create(ccp);
    }

    public CtacteCliente find(Integer id) {
        return DAO.getEntityManager().find(CtacteCliente.class, id);
    }

    public CtacteCliente findByFactura(Integer id) {
        return (CtacteCliente) DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o "
                + " where o.factura.id = " + id).getSingleResult();
    }

    List<CtacteCliente> findByCliente(Cliente cliente, short estadoCtaCte) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        List<CtacteCliente> l = em.createQuery(
                "SELECT o FROM " + CtacteCliente.class.getSimpleName() + " o"
                + " WHERE o.estado = " + estadoCtaCte + " AND o.factura.cliente.id =" + cliente.getId()
                + " ORDER BY o.factura.sucursal.puntoVenta, o.factura.numero",
                CtacteCliente.class).getResultList();
        return l;
    }

    /**
     * Data structure returned: { {@link Cliente#id}, {@link Cliente#nombre},
     * sumatoria de todos los saldos}
     *
     * @param desde optional filter
     * @param hasta optional filter
     * @return
     */
    List<Object[]> findSaldos(Date desde, Date hasta) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        List<CtacteCliente> l;
        l = em.createQuery(
                "SELECT o FROM " + CtacteCliente.class.getSimpleName() + " o"
                + " WHERE o.factura.anulada = FALSE AND o.estado = 1"
                + (desde != null ? " AND o.factura.fechaVenta >='" + UTIL.DATE_FORMAT.format(desde) + "'" : "")
                + (hasta != null ? " AND o.factura.fechaVenta <='" + UTIL.DATE_FORMAT.format(hasta) + "'" : "")
                + " ORDER BY o.factura.cliente.nombre",
                CtacteCliente.class).getResultList();
        if (l.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<Object[]> data = new ArrayList<>();
        BigDecimal importeCCC = BigDecimal.ZERO;
        Cliente c = l.get(0).getFactura().getCliente();
        for (CtacteCliente ccc : l) {
            if (!c.equals(ccc.getFactura().getCliente())) {
                data.add(new Object[]{c.getId(), c.getNombre(), importeCCC});
                importeCCC = BigDecimal.ZERO;
                c = ccc.getFactura().getCliente();
            }
            importeCCC = importeCCC.add(BigDecimal.valueOf(ccc.getImporte() - ccc.getEntregado()));
        }
        data.add(new Object[]{c.getId(), c.getNombre(), importeCCC});
        return data;
    }

    public JDialog getResumenCtaCte(Window owner, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        resumenCtaCtes = new JDResumenCtaCtes(owner, modal, true);
        resumenCtaCtes.getjTableResumen().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Integer selectedRow = resumenCtaCtes.getjTableResumen().getSelectedRow();
                if (selectedRow > 0) {
                    //selecciona una factura (a CtaCteCliente)
                    cargarComboBoxRecibosDeCtaCte(find((Integer) (resumenCtaCtes.getDtmResumen().getValueAt(selectedRow, 0))));
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Integer selectedRow = resumenCtaCtes.getjTableResumen().getSelectedRow();
                    CtacteCliente ccc = find((Integer) (resumenCtaCtes.getDtmResumen().getValueAt(selectedRow, 0)));
                    FacturaVenta factura = ccc.getFactura();
                    try {
                        FacturaVentaController fvc = new FacturaVentaController();
                        fvc.show(factura, false);
                    } catch (MessageException ex) {
                        ex.displayMessage(null);
                    }
                }
            }
        });
        resumenCtaCtes.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQuery(false);
                } catch (MessageException ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                    Logger.getLogger(CtacteClienteController.class.getName()).error(ex);
                }
            }
        });
        resumenCtaCtes.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQuery(true);
                } catch (MessageException ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                    Logger.getLogger(CtacteClienteController.class.getName()).error(ex);
                }
            }
        });
        resumenCtaCtes.getCbReRes().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (resumenCtaCtes.getCbReRes().isFocusOwner()) {
                    setDatosReciboSelected();
                }
            }
        });
        resumenCtaCtes.getBtnCuenta().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    @SuppressWarnings("unchecked")
                    Cliente c = ((ComboBoxWrapper<Cliente>) resumenCtaCtes.getCbClieProv().getSelectedItem()).getEntity();
                    initBuscadorNotaCredito(c);
                } catch (MessageException ex) {
                    ex.displayMessage(resumenCtaCtes);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(resumenCtaCtes, "Algo salió mal:\n" + ex.getLocalizedMessage());
                    LOG.error("Iniciando buscador de NotaCredito desde Resumen CCC", ex);
                }
            }
        });
        UTIL.loadComboBox(resumenCtaCtes.getCbClieProv(), JGestionUtils.getWrappedClientes(new ClienteController().findAll()), false);
        UTIL.loadComboBox(resumenCtaCtes.getCbReRes(), null, true);
        UTIL.getDefaultTableModel(
                resumenCtaCtes.getjTableResumen(),
                new String[]{"ctacteClienteID", "Detalle", "Fecha", "Vencimiento", "Debe", "Haber", "Saldo", "Acumulativo", "estadoCCC"},
                new int[]{1, 60, 50, 50, 30, 30, 30, 50, 1});
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(5).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(7).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnsTable(resumenCtaCtes.getjTableResumen(), new int[]{0, 8});
        UTIL.getDefaultTableModel(
                resumenCtaCtes.getjTableDetalle(),
                new String[]{"Nº Factura", "Observación", "Monton"},
                new int[]{60, 100, 50});
        resumenCtaCtes.getjTableDetalle().getColumnModel().getColumn(2).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        resumenCtaCtes.setListener(this);
        return resumenCtaCtes;
    }

    private void initBuscadorNotaCredito(Cliente c) throws MessageException {
        new NotaCreditoController().initBuscador(resumenCtaCtes, false, c, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //evoluciónnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
    }

    @SuppressWarnings("unchecked")
    private void armarQuery(boolean imprimirResumen) throws MessageException, MissingReportException, JRException {
        totalDebe = 0.0;
        totalHaber = 0.0;

        String query = "SELECT ccc.*, sucursal.puntoventa, fv.fecha_venta"
                + " FROM ctacte_cliente ccc, cliente , factura_venta fv JOIN sucursal ON (fv.sucursal = sucursal.id) "
                + " WHERE ccc.factura = fv.id AND fv.cliente = cliente.id ";
        //solo el filtro se envía al reporte! por eso están separados
        String filters = "";
        try {
            filters += " AND cliente.id =" + ((ComboBoxWrapper<Cliente>) resumenCtaCtes.getCbClieProv().getSelectedItem()).getId();
        } catch (ClassCastException ex) {
            throw new MessageException("Cliente no válido");
        }

        if (resumenCtaCtes.getDcDesde() != null) {
            //calcula los totales del DEBE / HABER / SALDO ACUMULATIVO de la CtaCte
            // anterior a la fecha desde la cual se eligió en el buscador
            setResumenHistorial(query + "AND fv.fecha_venta < '" + resumenCtaCtes.getDcDesde() + "'");

            filters += " AND fv.fecha_venta >= '" + resumenCtaCtes.getDcDesde() + "'";
        }
        if (resumenCtaCtes.getCheckExcluirPagadas().isSelected()) {
            filters += " AND (ccc.importe - ccc.entregado) > 0";
        }
        if (resumenCtaCtes.getCheckExcluirAnuladas().isSelected()) {
            filters += " AND fv.anulada = FALSE";
        }
        query += filters + " ORDER BY fv.fecha_venta";
        LOG.trace(query);
        cargarTablaResumen(query);
        if (imprimirResumen) {
            doReportResumenCCC(((ComboBoxWrapper<Cliente>) resumenCtaCtes.getCbClieProv().getSelectedItem()).getEntity(), resumenCtaCtes.getDcDesde(), filters);
        }
    }

    private void cargarTablaResumen(String query) {
        DefaultTableModel dtm = (DefaultTableModel) resumenCtaCtes.getjTableResumen().getModel();
        dtm.setRowCount(0);
        List<CtacteCliente> cccList = DAO.getEntityManager().createNativeQuery(query, CtacteCliente.class).getResultList();

        //agregar la 1er fila a la tabla
        BigDecimal saldoAcumulativo = BigDecimal.valueOf(totalDebe - totalHaber).setScale(2, RoundingMode.HALF_EVEN);
        dtm.addRow(new Object[]{null, "RESUMEN PREVIO", null, null, BigDecimal.valueOf(totalDebe), BigDecimal.valueOf(totalHaber), null, saldoAcumulativo});
        for (CtacteCliente ctaCte : cccList) {
            FacturaVenta factura = ctaCte.getFactura();
            //checkea que no esté anulada la ccc
            boolean isAnulada = (ctaCte.getEstado() == 3);
            if (!isAnulada) {
                saldoAcumulativo = saldoAcumulativo.add(BigDecimal.valueOf(ctaCte.getImporte() - ctaCte.getEntregado()));
            }

            dtm.addRow(new Object[]{
                ctaCte.getId(), // <--------- No es visible desde la GUI
                JGestionUtils.getNumeracion(factura),
                UTIL.DATE_FORMAT.format(factura.getFechaVenta()),
                UTIL.DATE_FORMAT.format(UTIL.customDateByDays(factura.getFechaVenta(), ctaCte.getDias())),
                BigDecimal.valueOf(ctaCte.getImporte()),
                isAnulada ? "ANULADA" : BigDecimal.valueOf(ctaCte.getEntregado()),
                isAnulada ? "ANULADA" : BigDecimal.valueOf(ctaCte.getImporte() - ctaCte.getEntregado()),
                isAnulada ? "ANULADA" : saldoAcumulativo,
                ctaCte.getEstado()
            });
        }
    }

    private void doReportResumenCCC(Cliente cliente, Date filterDate, String filters) throws MissingReportException, JRException {
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_ResumenCCC.jasper", "Resumen CCC");
        r.addCurrent_User();
        r.addParameter("CLIENTE_ID", cliente.getId());
        r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
        r.addParameter("FILTER_DATE", filterDate);
        r.addParameter("FILTERS", filters == null ? "" : filters);
        r.viewReport();
    }

    /**
     * Carga el combo con los Recibo's que tenga esta CtaCteCliente.
     *
     * @param ctacteCliente
     */
    private void cargarComboBoxRecibosDeCtaCte(CtacteCliente ctacteCliente) {
        List<Recibo> recibosList = new ReciboController().findRecibosByFactura(ctacteCliente.getFactura());
        List<ComboBoxWrapper<Recibo>> wrapped = new ArrayList<>(recibosList.size());
        for (Recibo recibo : recibosList) {
            wrapped.add(new ComboBoxWrapper<>(recibo, recibo.getId(), JGestionUtils.getNumeracion(recibo, true)));
        }
        UTIL.loadComboBox(resumenCtaCtes.getCbReRes(), wrapped, false);
        setDatosReciboSelected();
    }

    private void setDatosReciboSelected() {
        try {
            @SuppressWarnings("unchecked")
            ComboBoxWrapper<Recibo> cbw = (ComboBoxWrapper<Recibo>) resumenCtaCtes.getCbReRes().getSelectedItem();
            Recibo recibo = cbw.getEntity();
            resumenCtaCtes.setTfReciboFecha(UTIL.DATE_FORMAT.format(recibo.getFechaRecibo()));
            resumenCtaCtes.setTfReciboMonto(UTIL.DECIMAL_FORMAT.format(recibo.getMonto()));
            resumenCtaCtes.getLabelReciboAnulado().setVisible(!recibo.getEstado());
            cargarTablaDetallesDeCtaCte(recibo);
        } catch (ClassCastException ex) {
            // si el comboBox está vacio
            System.out.println("Recibo NULL!");
            resumenCtaCtes.setTfReciboFecha("");
            resumenCtaCtes.setTfReciboMonto("");
            resumenCtaCtes.getLabelReciboAnulado().setVisible(false);
            UTIL.limpiarDtm(resumenCtaCtes.getDtmDetalle());
        }
    }

    private void cargarTablaDetallesDeCtaCte(Recibo recibo) {
        DefaultTableModel dtm = (DefaultTableModel) resumenCtaCtes.getjTableDetalle().getModel();
        dtm.setRowCount(0);
        List<DetalleRecibo> detalleReciboList = recibo.getDetalle();
        for (DetalleRecibo detalleRecibo : detalleReciboList) {
            dtm.addRow(new Object[]{
                detalleRecibo.getFacturaVenta() != null
                ? JGestionUtils.getNumeracion(detalleRecibo.getFacturaVenta())
                : JGestionUtils.getNumeracion(detalleRecibo.getNotaDebito()),
                detalleRecibo.getObservacion(),
                detalleRecibo.getMontoEntrega()
            });
        }
    }

    /**
     * Calcula el total del DEBE, HABER y SALDO ACUMULATIVO de la Cta. cte. del
     * Cliente anterior a la fecha desde especificada en el Buscador.
     *
     * @param query
     */
    private void setResumenHistorial(String query) {
        List<CtacteCliente> lista = DAO.getEntityManager().createNativeQuery(query, CtacteCliente.class).getResultList();
        for (CtacteCliente ccc : lista) {
            if (ccc.getEstado() != 3) { // 3 == anulada
                totalDebe += ccc.getImporte();
                totalHaber += ccc.getEntregado();
            }
        }
    }

    /**
     * Inicia una UI de busqueda y chequeo de vencimientos de
     * {@link CtacteCliente} y {@link CtacteProveedor}
     *
     * @param owner el papi de la ventana
     */
    public void initCheckVencimientos(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        panelCCCheck = new PanelCtaCteCheckVencimientos();
        panelCCCheck.getCbEntidadElegida().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = panelCCCheck.getCbEntidadElegida().getSelectedIndex();
                if (index == 0) {
                    panelCCCheck.getCbClientesProveedores().removeAllItems();
                } else if (index == 1) {
                    UTIL.loadComboBox(panelCCCheck.getCbClientesProveedores(), new ClienteController().findAll(), "<Todos>");
                } else if (index == 2) {
                    UTIL.loadComboBox(panelCCCheck.getCbClientesProveedores(), new ProveedorController().findEntities(), "<Todos>");
                }
            }
        });
        buscador = new JDBuscador(owner, "Ctas. Ctes. vencimientos", false, panelCCCheck);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"C/P", "Cliente", "Tipo", "Nº factura", "Importe", "Saldo", "Fecha", "Vto."},
                new int[]{5, 150, 6, 60, 30, 30, 45, 45},
                new Class<?>[]{null, null, null, String.class, BigDecimal.class, BigDecimal.class, Date.class, Date.class});
        //alineando las columnas Importe y Saldo to RIGHT!!
        DefaultTableCellRenderer defaultTableCellRender = new DefaultTableCellRenderer();
        defaultTableCellRender.setHorizontalAlignment(JLabel.RIGHT);
        buscador.getjTable1().setDefaultRenderer(String.class, defaultTableCellRender);
        buscador.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    buscador.bloquearBotones(true);
                    armarQueryVencimientos(false);
                } catch (MissingReportException ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage(), "ERROR", 0);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage(), "ERROR CRÍTICO", 0);
                    Logger.getLogger(CtacteClienteController.class.getName()).error(ex);
                } finally {
                    buscador.bloquearBotones(false);
                }
            }
        });
        buscador.getBtnImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    buscador.bloquearBotones(true);
                    armarQueryVencimientos(true);
                } catch (MissingReportException ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage(), "ERROR", 0);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage(), "ERROR CRÍTICO", 0);
                    Logger.getLogger(CtacteClienteController.class.getName()).error(ex, ex);
                } finally {
                    buscador.bloquearBotones(false);
                }
            }
        });
        //agranda un poco el buscador.. porque se quedo chico
        buscador.setSize(buscador.getWidth() + 200, buscador.getHeight());
        buscador.setVisible(true);
    }

    private void armarQueryVencimientos(boolean imprimirReporte) throws MissingReportException, Exception {
        String query = "SELECT * FROM ";
        String sub_titulo_entidad = null;
        String sub_titulo_fecha = null;
        final String clientesQuery = "(SELECT 'C' as cp, c.nombre, fv.tipo, trim(to_char(sucursal.puntoventa, '0000')) || trim(to_char(fv.numero,'-00000000')) numero, fv.importe, (ccc.importe - ccc.entregado) as saldo, fv.fecha_venta as fecha, (fv.fecha_venta + fv.dias_cta_cte) as vto"
                + " FROM ctacte_cliente ccc JOIN factura_venta fv ON ccc.factura = fv.id JOIN cliente c ON fv.cliente = c.id JOIN sucursal ON fv.sucursal = sucursal.id"
                + " WHERE ccc.estado = 1)";
        final String proveedoresQuery = "(SELECT 'P' as cp , c.nombre, fv.tipo, to_char(fv.numero, '0000-00000000') numero, fv.importe, (ccc.importe - ccc.entregado) as saldo, fv.fecha_compra as fecha, (fv.fecha_compra + fv.dias_cta_cte) as vto"
                + " FROM ctacte_proveedor ccc JOIN factura_compra fv ON ccc.factura = fv.id JOIN proveedor c ON fv.proveedor = c.id"
                + " WHERE ccc.estado = 1)";
        int index = panelCCCheck.getCbEntidadElegida().getSelectedIndex();
        if (index == 0) {
            query += " (" + clientesQuery + " UNION " + proveedoresQuery + " ) as c";
            sub_titulo_entidad = "CLIENTES Y PROVEEDORES";
        } else if (index == 1) {
            query += clientesQuery;
            if (panelCCCheck.getCbClientesProveedores().getSelectedIndex() > 0) {
                Cliente c = (Cliente) panelCCCheck.getCbClientesProveedores().getSelectedItem();
                query += " AND c.id= " + c.getId();
                sub_titulo_entidad = "Cliente: (" + c.getCodigo() + ") " + c.getNombre();
            }
            query += ") as c";
        } else {
            query += proveedoresQuery;
            if (panelCCCheck.getCbClientesProveedores().getSelectedIndex() > 0) {
                Proveedor p = (Proveedor) panelCCCheck.getCbClientesProveedores().getSelectedItem();
                query += " AND c.id= " + p.getId();
                sub_titulo_entidad = "Proveedor: (" + p.getCodigo() + ") " + p.getNombre();

            }
            query += ") as c";
        }
        query += " WHERE vto IS NOT NULL";
        if (panelCCCheck.getDcDesde().getDate() != null) {
            query += " AND vto >= '" + UTIL.yyyy_MM_dd.format(panelCCCheck.getDcDesde().getDate()) + "'";
            //dato para el reporte
            sub_titulo_fecha = "DESDE: " + UTIL.DATE_FORMAT.format(panelCCCheck.getDcDesde().getDate());
        }
        if (panelCCCheck.getDcHasta().getDate() != null) {
            query += " AND vto <= '" + UTIL.yyyy_MM_dd.format(panelCCCheck.getDcHasta().getDate()) + "'";
            //dato para el reporte
            if (sub_titulo_fecha == null) {
                sub_titulo_fecha = "HASTA: " + UTIL.DATE_FORMAT.format(panelCCCheck.getDcHasta().getDate());
            } else {
                sub_titulo_fecha += "HASTA: " + UTIL.DATE_FORMAT.format(panelCCCheck.getDcHasta().getDate());
            }
        }
        query += " ORDER BY vto";
        cargarTablaVencimientosCC(DAO.getEntityManager().createNativeQuery(query).getResultList());
        if (imprimirReporte) {
            doReporteVencimientosCC(query, sub_titulo_entidad, sub_titulo_fecha);
        }
    }

    private void doReporteVencimientosCC(String query, String sub_titulo_entidad, String sub_titulo_fecha) throws MissingReportException, Exception {
        Reportes r = new Reportes("JGestion_VencimientosCC.jasper", "Vencimientos de Ctas. Ctes.");
        r.addCurrent_User();
        r.addParameter("SUB_TITULO_ENTIDAD", sub_titulo_entidad);
        r.addParameter("SUB_TITULO_FECHA", sub_titulo_fecha);
        r.addParameter("QUERY", query);
        r.viewReport();
    }

    private void cargarTablaVencimientosCC(List<Object[]> resultList) {
        DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
        dtm.setRowCount(0);
        for (Object[] o : resultList) {
            dtm.addRow(o);
        }
    }

    /**
     * Busca la Cta. Cte. relacionada al comprobante y retorna (si existe).
     *
     * @param facturaVenta
     * @return instance or {@code null} does not exist.
     */
    public CtacteCliente findBy(FacturaVenta facturaVenta) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<CtacteCliente> query = cb.createQuery(CtacteCliente.class);
        Root<CtacteCliente> from = query.from(CtacteCliente.class);
        query.select(from).where(cb.equal(from.get(CtacteCliente_.factura), facturaVenta));
        try {
            return getEntityManager().createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    JDialog getResumenCtaCte(Window owner, boolean modal, Cliente cliente) throws MessageException, MissingReportException, JRException {
        getResumenCtaCte(owner, modal);
        if (cliente != null) {
            UTIL.setSelectedItem(resumenCtaCtes.getCbClieProv(), new ComboBoxWrapper<>(cliente, cliente.getId(), cliente.getNombre()));
            armarQuery(false);
        }
        return resumenCtaCtes;
    }
}

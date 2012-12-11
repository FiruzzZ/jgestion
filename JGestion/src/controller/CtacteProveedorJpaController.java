package controller;

import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.CtacteProveedor;
import entity.DetalleRemesa;
import entity.FacturaCompra;
import entity.Proveedor;
import entity.Remesa;
import utilities.general.UTIL;
import gui.JDResumenCtaCtes;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.text.NumberFormatter;
import jpa.controller.RemesaJpaController;
import net.sf.jasperreports.engine.JRException;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author Administrador
 */
public class CtacteProveedorJpaController implements ActionListener {

    public static final String CLASS_NAME = CtacteProveedor.class.getSimpleName();
    private JDResumenCtaCtes resumenCtaCtes;
    private Double totalDebe;
    private Double totalHaber;

    // <editor-fold defaultstate="collapsed" desc="CRUD...">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(CtacteProveedor ctacteProveedor) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(ctacteProveedor);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(CtacteProveedor ctacteProveedor) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ctacteProveedor = em.merge(ctacteProveedor);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = ctacteProveedor.getId();
                if (findCtacteProveedor(id) == null) {
                    throw new NonexistentEntityException("The ctacteProveedor with id " + id + " no longer exists.");
                }
            }
            throw ex;
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
            CtacteProveedor ctacteProveedor;
            try {
                ctacteProveedor = em.getReference(CtacteProveedor.class, id);
                ctacteProveedor.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The ctacteProveedor with id " + id + " no longer exists.", enfe);
            }
            em.remove(ctacteProveedor);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<CtacteProveedor> findCtacteProveedorEntities() {
        return findCtacteProveedorEntities(true, -1, -1);
    }

    public List<CtacteProveedor> findCtacteProveedorEntities(int maxResults, int firstResult) {
        return findCtacteProveedorEntities(false, maxResults, firstResult);
    }

    private List<CtacteProveedor> findCtacteProveedorEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from CtacteProveedor as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public CtacteProveedor findCtacteProveedor(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(CtacteProveedor.class, id);
        } finally {
            em.close();
        }
    }

    public int getCtacteProveedorCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from CtacteProveedor as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    void nuevaCtaCte(FacturaCompra facturaCompra) {
        CtacteProveedor ccp = new CtacteProveedor();
        ccp.setDias(facturaCompra.getDiasCtaCte());
        ccp.setEntregado(BigDecimal.ZERO); //monto $$
        ccp.setEstado(Valores.CtaCteEstado.PENDIENTE.getId());
        ccp.setFactura(facturaCompra);
        ccp.setFechaCarga(facturaCompra.getFechaCompra());
        ccp.setImporte(BigDecimal.valueOf(facturaCompra.getImporte()));
        create(ccp);
    }

    List<CtacteProveedor> findCtacteProveedorByProveedor(Integer idProveedor, int estadoCtaCte) {
        EntityManager em = getEntityManager();
        List<CtacteProveedor> listaCtaCteProveedor = em.createQuery(
                "SELECT o FROM " + CtacteProveedor.class.getSimpleName() + " o"
                + " WHERE o.estado=" + estadoCtaCte + " AND o.factura.proveedor.id =" + idProveedor
                + " ORDER BY o.factura.numero",
                CtacteProveedor.class).getResultList();
        return listaCtaCteProveedor;
    }

    List<CtacteProveedor> findCtacteProveedorByProveedor(Integer idProveedor) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        List<CtacteProveedor> listaCtaCteProveedor = null;
        listaCtaCteProveedor = em.createNativeQuery(
                "SELECT o.* FROM ctacte_proveedor o, factura_compra f, proveedor p"
                + " WHERE p.id = f.proveedor AND f.id = o.factura "
                + " AND p.id =" + idProveedor,
                CtacteProveedor.class).getResultList();
        return listaCtaCteProveedor;
    }

    public CtacteProveedor findCtacteProveedorByFactura(Integer idFacturaCompra) throws NoResultException {
        return (CtacteProveedor) DAO.getEntityManager().createNativeQuery("select * from ctacte_proveedor o "
                + "where o.factura = " + idFacturaCompra, CtacteProveedor.class).getSingleResult();

    }

    public void iniResumenCtaCte(JFrame owner, boolean modal) {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        } catch (MessageException ex) {
            javax.swing.JOptionPane.showMessageDialog(owner, ex.getMessage());
            return;
        }// </editor-fold>

        resumenCtaCtes = new JDResumenCtaCtes(owner, modal, false);
        resumenCtaCtes.getjTableResumen().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                jTableResumenMouseReleased(e);
            }
        });
        UTIL.loadComboBox(resumenCtaCtes.getCbClieProv(), new ProveedorController().findEntities(), false);
        UTIL.loadComboBox(resumenCtaCtes.getCbReRes(), null, true);
        UTIL.getDefaultTableModel(
                resumenCtaCtes.getjTableResumen(),
                new String[]{"ctacteProveedorID", "Detalle", "Fecha", "Vencimiento", "Debe", "Haber", "Saldo", "Acumulativo"},
                new int[]{1, 60, 50, 50, 30, 30, 30, 50});
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(2).setCellRenderer(FormatRenderer.getDateRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(3).setCellRenderer(FormatRenderer.getDateRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(5).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        resumenCtaCtes.getjTableResumen().getColumnModel().getColumn(7).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        UTIL.hideColumnTable(resumenCtaCtes.getjTableResumen(), 0);
        UTIL.getDefaultTableModel(
                resumenCtaCtes.getjTableDetalle(),
                new String[]{"Nº Factura", "Observación", "Monton"},
                new int[]{60, 100, 50});
        resumenCtaCtes.getjTableDetalle().getColumnModel().getColumn(2).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        
        resumenCtaCtes.setListener(this);
        resumenCtaCtes.setLocationRelativeTo(owner);
        resumenCtaCtes.setVisible(true);
    }

    private void jTableResumenMouseReleased(MouseEvent e) {
        Integer selectedRow = resumenCtaCtes.getjTableResumen().getSelectedRow();
        if (selectedRow > 0) {
            //selecciona una factura CtaCteCliente
            cargarComboBoxRecibosDeCtaCte((CtacteProveedor) DAO.getEntityManager().find(CtacteProveedor.class,
                    Integer.valueOf((resumenCtaCtes.getDtmResumen().getValueAt(selectedRow, 0)).toString())));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
            JButton btn = (JButton) e.getSource();

            // <editor-fold defaultstate="collapsed" desc="verResumenCCC">
            if (btn.equals(resumenCtaCtes.getbBuscar())) {
                try {
                    armarQuery(false);
                } catch (MessageException ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                    Logger.getLogger(CtacteClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }// </editor-fold>
            else if (btn.equals(resumenCtaCtes.getbImprimir())) {
                try {
                    armarQuery(true);
                } catch (MessageException ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
                    Logger.getLogger(CtacteClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }// </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="JComboBox">
        else if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
            javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();
            if (combo.getName().equalsIgnoreCase("cbReRes")) {
                if (combo.isFocusOwner()) {
                    setDatosReciboSelected();
                }
            }
        }// </editor-fold>
    }

    private void armarQuery(boolean imprimirResumen) throws MessageException, Exception {
        totalDebe = 0.0;
        totalHaber = 0.0;

        String query = "SELECT ccc.* "
                + " FROM ctacte_proveedor ccc, proveedor c, factura_compra fv "
                + " WHERE ccc.factura = fv.id "
                + " AND fv.proveedor = c.id ";
        try {
            query += "AND c.id =" + ((Proveedor) resumenCtaCtes.getCbClieProv().getSelectedItem()).getId();
        } catch (ClassCastException ex) {
            throw new MessageException("Proveedor no válido");
        }

        if (resumenCtaCtes.getDcDesde() != null) {
            //calcula los totales del DEBE / HABER / SALDO ACUMULATIVO de la CtaCte
            // anterior a la fecha desde la cual se eligió en el buscador
            setResumenHistorial(query + "AND ccc.fecha_carga < '" + resumenCtaCtes.getDcDesde() + "'");

            query += "AND ccc.fecha_carga >= '" + resumenCtaCtes.getDcDesde() + "'";
        }

        query += " ORDER BY ccc.id";
        cargarDtmResumen(query);
        if (imprimirResumen) {
            imprimirResumenCCC(((Proveedor) resumenCtaCtes.getCbClieProv().getSelectedItem()), resumenCtaCtes.getDcDesde() != null ? " AND ccc.fecha_carga >= '" + UTIL.DATE_FORMAT.format(resumenCtaCtes.getDcDesde()) + "'" : "");
        }
    }

    private void setResumenHistorial(String query) {
        List<CtacteProveedor> lista = DAO.getEntityManager().createNativeQuery(query, CtacteProveedor.class).getResultList();
        for (CtacteProveedor ccc : lista) {
            if (ccc.getEstado() != 3) { // 3 == anulado
                totalDebe += ccc.getImporte().doubleValue();
                totalHaber += ccc.getEntregado().doubleValue();
            }
        }
    }

    private void cargarDtmResumen(String query) {
        javax.swing.table.DefaultTableModel dtm = resumenCtaCtes.getDtmResumen();
        UTIL.limpiarDtm(dtm);
        List<CtacteProveedor> lista = DAO.getEntityManager().createNativeQuery(query, CtacteProveedor.class).getResultList();

        //agregar la 1er fila a la tabla
        BigDecimal saldoAcumulativo = BigDecimal.valueOf(totalDebe - totalHaber);
        dtm.addRow(new Object[]{null, "RESUMEN PREVIOS", null, null, totalDebe, totalHaber, null, saldoAcumulativo});
        for (CtacteProveedor ctaCte : lista) {
            FacturaCompra factura = ctaCte.getFactura();
            //checkea que no esté anulada la ccc
            boolean isAnulada = (ctaCte.getEstado() == 3);
            if (!isAnulada) {
                saldoAcumulativo = saldoAcumulativo.add(ctaCte.getImporte().subtract(ctaCte.getEntregado()));
            }

            dtm.addRow(new Object[]{
                        ctaCte.getId(), // <--------- No es visible desde la GUI
                        factura.getTipo() + UTIL.AGREGAR_CEROS(factura.getNumero(), 12),
                        factura.getFechaCompra(),
                        UTIL.customDateByDays(factura.getFechaCompra(), ctaCte.getDias()),
                        ctaCte.getImporte(),
                        isAnulada ? "ANULADA" : ctaCte.getEntregado(),
                        isAnulada ? "ANULADA" : ctaCte.getImporte().subtract(ctaCte.getEntregado()),
                        isAnulada ? "ANULADA" : saldoAcumulativo,
                        ctaCte.getEstado()
                    });
        }
    }

    private void imprimirResumenCCC(Proveedor proveedor, String filter_date) throws JRException, Exception {
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_ResumenCCP.jasper", "Resumen Cta. Cte. Proveedor");
        r.addCurrent_User();
        r.addParameter("PROVEEDOR_ID", proveedor.getId());
        r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
        if (filter_date == null) {
            filter_date = "";
        }
        r.addParameter("FILTER_DATE", filter_date);
        r.viewReport();
    }

    private void cargarComboBoxRecibosDeCtaCte(CtacteProveedor ctacteProveedor) {
        List<Remesa> recibosList = new RemesaJpaController().findByFactura(ctacteProveedor.getFactura());
        UTIL.loadComboBox(resumenCtaCtes.getCbReRes(), recibosList, false);
        setDatosReciboSelected();
    }

    private void setDatosReciboSelected() {
        try {
            Remesa remesa = (Remesa) resumenCtaCtes.getCbReRes().getSelectedItem();
            resumenCtaCtes.setTfReciboFecha(UTIL.DATE_FORMAT.format(remesa.getFechaRemesa()));
            resumenCtaCtes.setTfReciboMonto(UTIL.DECIMAL_FORMAT.format(remesa.getMonto()));
            cargarDtmDetallesDeCtaCte(remesa);
        } catch (ClassCastException ex) {
            // si el comboBox está vacio
            System.out.println("Remesa NULL!");
            resumenCtaCtes.setTfReciboFecha("");
            resumenCtaCtes.setTfReciboMonto("");
            UTIL.limpiarDtm(resumenCtaCtes.getDtmDetalle());
        }
    }

    private void cargarDtmDetallesDeCtaCte(Remesa remesa) {
        javax.swing.table.DefaultTableModel dtm = resumenCtaCtes.getDtmDetalle();
        UTIL.limpiarDtm(dtm);
        List<DetalleRemesa> detalleReList = remesa.getDetalleRemesaList();
        for (DetalleRemesa detalleRe : detalleReList) {
            dtm.addRow(new Object[]{
                        UTIL.AGREGAR_CEROS(detalleRe.getFacturaCompra().getNumero(), 12),
                        detalleRe.getObservacion(),
                        UTIL.PRECIO_CON_PUNTO.format(detalleRe.getMontoEntrega())
                    });
        }
    }

    List<Object[]> findSaldosCtacte(Date desde, Date hasta) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        List<CtacteProveedor> l;
        l = em.createQuery(
                "SELECT o FROM " + CtacteProveedor.class.getSimpleName() + " o"
                + " WHERE o.factura.anulada = FALSE AND o.estado = 1"
                + (desde != null ? " AND o.factura.fechaCompra >='" + UTIL.DATE_FORMAT.format(desde) + "'" : "")
                + (hasta != null ? " AND o.factura.fechaCompra <='" + UTIL.DATE_FORMAT.format(hasta) + "'" : "")
                + " ORDER BY o.factura.proveedor.nombre",
                CtacteProveedor.class).getResultList();
        if (l.isEmpty()) {
            return new ArrayList<Object[]>(0);
        }
        List<Object[]> data = new ArrayList<Object[]>();
        BigDecimal importeCCC = BigDecimal.ZERO;
        Proveedor c = l.get(0).getFactura().getProveedor();
        for (CtacteProveedor ccc : l) {
            if (!c.equals(ccc.getFactura().getProveedor())) {
                data.add(new Object[]{c.getNombre(), importeCCC});
                importeCCC = BigDecimal.ZERO;
                c = ccc.getFactura().getProveedor();
            }
            importeCCC = importeCCC.add(ccc.getImporte().subtract(ccc.getEntregado()));
        }
        data.add(new Object[]{c.getNombre(), importeCCC});
        return data;
    }
}

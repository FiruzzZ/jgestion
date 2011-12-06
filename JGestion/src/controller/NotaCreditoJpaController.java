package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import controller.exceptions.MissingReportException;
import controller.exceptions.PreexistingEntityException;
import entity.Cliente;
import entity.DetalleAcreditacion;
import entity.DetalleRecibo;
import entity.NotaCredito;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import entity.DetalleNotaCredito;
import entity.FacturaVenta;
import entity.Recibo;
import entity.Sucursal;
import utilities.general.UTIL;
import gui.JDBuscadorReRe;
import gui.JDFacturaVenta;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class NotaCreditoJpaController {

    private static final String CLASS_NAME = NotaCredito.class.getSimpleName();
    private JDFacturaVenta jdFacturaVenta;
    private FacturaVentaJpaController facturaVentaController;
    private JDBuscadorReRe buscador;
    private NotaCredito EL_OBJECT;
    private static int OBSERVACION_PROPERTY_LIMIT_LENGHT = 200;

    public NotaCreditoJpaController() {
    }

    // <editor-fold defaultstate="collapsed" desc="CRUD">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(NotaCredito notaCredito) throws PreexistingEntityException, Exception {
        Collection<DetalleNotaCredito> toAttach = notaCredito.getDetalleNotaCreditoCollection();
        notaCredito.setDetalleNotaCreditoCollection(new ArrayList<DetalleNotaCredito>());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(notaCredito);
            em.getTransaction().commit();
            for (DetalleNotaCredito detalleNotaCredito : toAttach) {
                detalleNotaCredito.setNotaCredito(notaCredito);
                DAO.create(detalleNotaCredito);
            }
        } catch (Exception ex) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<NotaCredito> findNotaCreditoEntities() {
        return findNotaCreditoEntities(true, -1, -1);
    }

    public List<NotaCredito> findNotaCreditoEntities(int maxResults, int firstResult) {
        return findNotaCreditoEntities(false, maxResults, firstResult);
    }

    private List<NotaCredito> findNotaCreditoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from NotaCredito as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public NotaCredito findNotaCredito(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(NotaCredito.class, id);
        } finally {
            em.close();
        }
    }

    public int getNotaCreditoCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from NotaCredito as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    public void initABMNotaCredito(JFrame owner, boolean modal, boolean setVisible, boolean loadDefaultData) throws MessageException {
        facturaVentaController = new FacturaVentaJpaController();
        facturaVentaController.initFacturaVenta(owner, modal, null, 2, false, loadDefaultData);
        facturaVentaController.getContenedor().getBtnAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //deshabilita botón para no darle fruta
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(false);
                    if (!facturaVentaController.getContenedor().isViewMode()) {
                        NotaCredito notaCreditoToPersist = setAndPersist();
                        create(notaCreditoToPersist);
                        reporte(notaCreditoToPersist);
                        facturaVentaController.setNumeroFactura(getNextNumeroNotaCredito());
                        facturaVentaController.borrarDetalles();
                    } else {
                        if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(jdFacturaVenta, "¿Desea reimprimir la Nota de crédito?", "Re imprimir", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)) {
                            reporte(EL_OBJECT);
                        }
                    }
                } catch (MessageException ex) {
                    facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
                    Logger.getLogger(NotaCreditoJpaController.class).log(Level.ERROR, ex, ex);
                } finally {
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(true);
                }
            }
        });
        if (loadDefaultData) {
            facturaVentaController.setNumeroFactura(getNextNumeroNotaCredito());
        }
        facturaVentaController.getContenedor().setUIToNotaCredito();
        jdFacturaVenta = facturaVentaController.getContenedor();
        jdFacturaVenta.setVisible(setVisible);
    }

    private Long getNextNumeroNotaCredito() {
        EntityManager em = getEntityManager();
        Long nextRemitoNumero = 100000001L;
        try {
            nextRemitoNumero = 1 + (Long) em.createQuery("SELECT MAX(o.numero) FROM " + CLASS_NAME + " o").getSingleResult();
        } catch (NullPointerException ex) {
            // primero!! 0001-00000001
        } finally {
            em.close();
        }
        return nextRemitoNumero;
    }

    private NotaCredito setAndPersist() throws MessageException, Exception {
        if (jdFacturaVenta.getDcFechaFactura() == null) {
            throw new MessageException("Fecha de factura no válida");
        }
        Date fechaNotaCredito = jdFacturaVenta.getDcFechaFactura();
        Sucursal sucursal;
        Cliente cliente = (Cliente) jdFacturaVenta.getCbCliente().getSelectedItem();
        String observacion = null;
        try {
            sucursal = (Sucursal) jdFacturaVenta.getCbSucursal().getSelectedItem();
        } catch (ClassCastException e) {
            throw new MessageException("Debe crear una Sucursal."
                    + "\nMenú: Datos Generales -> Sucursales");
        }
        if (jdFacturaVenta.getTfObservacion().getText().trim().length() > 0) {
            observacion = jdFacturaVenta.getTfObservacion().getText().trim();
            if (observacion.length() >= OBSERVACION_PROPERTY_LIMIT_LENGHT) {
                throw new MessageException("La obsevación no puede tener mas de 250 caracteres (tamaño actual=" + observacion.length() + ").");
            }
        }
        DefaultTableModel dtm = jdFacturaVenta.getDTM();
        if (dtm.getRowCount() < 1) {
            throw new MessageException("No ha agregado ningún item al detalle.");
        } else if (dtm.getRowCount() > FacturaVentaJpaController.LIMITE_DE_ITEMS) {
            throw new MessageException("El límite del detalle son " + FacturaVentaJpaController.LIMITE_DE_ITEMS + " items.");
        }

        NotaCredito newNotaCredito = new NotaCredito();
        newNotaCredito.setNumero(Long.valueOf(jdFacturaVenta.getTfFacturaCuarto() + jdFacturaVenta.getTfFacturaOcteto()));
        newNotaCredito.setFechaNotaCredito(fechaNotaCredito);
        newNotaCredito.setImporte(Double.valueOf(jdFacturaVenta.getTfTotal()));
        newNotaCredito.setGravado(Double.valueOf(jdFacturaVenta.getTfGravado()));
        newNotaCredito.setIva10(Double.valueOf(jdFacturaVenta.getTfTotalIVA105()));
        newNotaCredito.setIva21(Double.valueOf(jdFacturaVenta.getTfTotalIVA21()));
        newNotaCredito.setCliente(cliente);
        newNotaCredito.setSucursal(sucursal);
        newNotaCredito.setUsuario(UsuarioJpaController.getCurrentUser());
        newNotaCredito.setObservacion(observacion);
        newNotaCredito.setDetalleNotaCreditoCollection(new ArrayList<DetalleNotaCredito>(dtm.getRowCount()));
        DetalleNotaCredito detalleVenta;
        ProductoJpaController productoController = new ProductoJpaController();
        for (int i = 0; i < dtm.getRowCount(); i++) {
            detalleVenta = new DetalleNotaCredito();
            detalleVenta.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
            detalleVenta.setPrecioUnitario(Double.valueOf(dtm.getValueAt(i, 4).toString()));
            detalleVenta.setProducto(productoController.findProducto((Integer) dtm.getValueAt(i, 9)));
            newNotaCredito.getDetalleNotaCreditoCollection().add(detalleVenta);
        }
        return newNotaCredito;
    }

    public void initBuscador(JFrame owner, final boolean paraAnular) {
        buscador = new JDBuscadorReRe(owner, "Buscador - Notas de crédito", true, "Cliente", "Nº");
        buscador.hideCaja();
        buscador.hideFactura();
        buscador.hideFormaPago();
        buscador.getbBuscar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cargarDtmBuscador(armarQuery());
                } catch (MessageException ex) {
                    Logger.getLogger(NotaCreditoJpaController.class.getName()).log(Level.WARN, null, ex);
                }
            }
        });
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteJpaController().findEntities(), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"NotaCreditoID", "Nº " + CLASS_NAME, "Cliente", "Importe", "Acreditado", "Fecha", "Sucursal", "Usuario", "Fecha (Sistema)"},
                new int[]{1, 60, 150, 50, 50, 50, 80, 50, 70});
        //escondiendo facturaID
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    if (buscador.getjTable1().getSelectedRow() > -1) {
                        EL_OBJECT = findNotaCredito(Integer.valueOf(buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
                        try {
                            setDatosEnUI(EL_OBJECT, paraAnular);
                            if (EL_OBJECT == null) {
                                cargarDtmBuscador(armarQuery());
                            }
                        } catch (MessageException ex) {
                            Logger.getLogger(NotaCreditoJpaController.class.getName()).log(Level.WARN, null, ex);
                        }
                    }
                }
            }
        });
        if (paraAnular) {
            buscador.getCheckAnulada().setEnabled(false);
            buscador.getCheckAnulada().setToolTipText("Buscador para ANULAR");
        }
        buscador.setLocationRelativeTo(owner);
        buscador.setVisible(true);
    }

    /**
     * Arma la consulta según los filtros de la UI {@link JDBuscadorReRe}
     * @return String SQL native query
     * @throws MessageException 
     */
    private String armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o.* FROM nota_credito o"
                + " WHERE o.anulada = " + buscador.isCheckAnuladaSelected());

        long numero;
        //filtro por nº de ReRe
        if (buscador.getTfCuarto().length() > 0 && buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfCuarto() + buscador.getTfOcteto());
                query.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de " + CLASS_NAME + " no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            query.append(" AND o.fecha_nota_credito >= '").append(buscador.getDcDesde()).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fecha_nota_credito <= '").append(buscador.getDcHasta()).append("'");
        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query.append(" AND o.sucursal = ").append(((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId());
        }
        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.cliente = ").append(((Cliente) buscador.getCbClieProv().getSelectedItem()).getId());
        }
        query.append(" ORDER BY o.id");
        Logger.getLogger(FacturaVentaJpaController.class).log(Level.TRACE, "queryBuscador=" + query.toString());
        return query.toString();
    }

    private void cargarDtmBuscador(String query) {
        DefaultTableModel dtm = buscador.getDtm();
        UTIL.limpiarDtm(dtm);
        List<NotaCredito> l = DAO.getEntityManager().createNativeQuery(query, NotaCredito.class).getResultList();
        for (NotaCredito o : l) {
            dtm.addRow(new Object[]{
                        o.getId(), // <--- no es visible
                        UTIL.AGREGAR_CEROS(o.getNumero(), 12),
                        o.getCliente(),
                        o.getImporte(),
                        o.getDesacreditado(),
                        UTIL.DATE_FORMAT.format(o.getFechaNotaCredito()),
                        o.getSucursal(),
                        o.getUsuario(),
                        UTIL.DATE_FORMAT.format(o.getFechaCarga()) + " (" + UTIL.TIME_FORMAT.format(o.getFechaCarga()) + ")"
                    });
        }
    }

    private void setDatosEnUI(final NotaCredito notaCredito, boolean paraAnular) throws MessageException {
        initABMNotaCredito(null, true, false, false);
        jdFacturaVenta.setViewMode(true);
        jdFacturaVenta.modoVista();
        // setting info on UI
        String numFactura = UTIL.AGREGAR_CEROS(notaCredito.getNumero(), 12);
        jdFacturaVenta.setTfFacturaCuarto(numFactura.substring(0, 4));
        jdFacturaVenta.setTfFacturaOcteto(numFactura.substring(4));
        jdFacturaVenta.getCbCliente().addItem(notaCredito.getCliente());
        jdFacturaVenta.getCbSucursal().addItem(notaCredito.getSucursal());
        jdFacturaVenta.setDcFechaFactura(notaCredito.getFechaNotaCredito());
        jdFacturaVenta.getTfObservacion().setText(notaCredito.getObservacion());
        Collection<DetalleNotaCredito> lista = notaCredito.getDetalleNotaCreditoCollection();
        DefaultTableModel dtm = jdFacturaVenta.getDTM();
        for (DetalleNotaCredito detalle : lista) {
            System.out.println(detalle.toString());
            double productoConIVA = detalle.getPrecioUnitario() + UTIL.getPorcentaje(detalle.getPrecioUnitario(), detalle.getProducto().getIva().getIva());
            //"IVA","Cód. Producto","Producto","Cantidad","P. Unitario","P. final","Desc","Sub total"
            dtm.addRow(new Object[]{
                        null,
                        detalle.getProducto().getCodigo(),
                        detalle.getProducto(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        UTIL.PRECIO_CON_PUNTO.format(productoConIVA),
                        0.0,
                        UTIL.PRECIO_CON_PUNTO.format((detalle.getCantidad() * productoConIVA))
                    });
        }
        //totales
        jdFacturaVenta.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(notaCredito.getImporte() - (notaCredito.getIva10() + notaCredito.getIva21())));
        jdFacturaVenta.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(notaCredito.getIva10()));
        jdFacturaVenta.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(notaCredito.getIva21()));
        jdFacturaVenta.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(notaCredito.getImporte()));
        jdFacturaVenta.setVisibleListaPrecio(false);// EXCLU NotaCredito
        if (paraAnular) {
            jdFacturaVenta.hidePanelCambio();
            jdFacturaVenta.getBtnCancelar().setVisible(false);
            jdFacturaVenta.getBtnAceptar().setVisible(false);
            jdFacturaVenta.getBtnFacturar().setVisible(false);
            jdFacturaVenta.getBtnAnular().setVisible(paraAnular);
            jdFacturaVenta.getBtnAnular().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        anular(notaCredito);
                        JOptionPane.showMessageDialog(jdFacturaVenta, "Nota de credito Nº" + UTIL.AGREGAR_CEROS(notaCredito.getNumero(), 12) + " anulada.");
                        jdFacturaVenta.dispose();
                        EL_OBJECT = null;
                        buscador.getbBuscar();
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(jdFacturaVenta, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        }
        jdFacturaVenta.setLocationRelativeTo(buscador);
        jdFacturaVenta.setVisible(true);
    }

    private void anular(NotaCredito notaCredito) throws MessageException {
        if (notaCredito.getDesacreditado() != 0) {
            throw new MessageException("La Nota de crédito Nº" + UTIL.AGREGAR_CEROS(notaCredito.getNumero(), 12) + " ha sido usada para acreditar pagos y no puede ser anulada.");
        }
        if (notaCredito.getAnulada()) {
            throw new MessageException("La Nota de crédito Nº" + UTIL.AGREGAR_CEROS(notaCredito.getNumero(), 12) + " ya está anulada.");
        }
        notaCredito.setAnulada(true);
        DAO.doMerge(notaCredito);
    }

    private void reporte(NotaCredito notaCredito) throws MissingReportException, JRException {
        Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_NotaCredito.jasper", "Nota de Crédito");
        r.addCurrent_User();
        r.addParameter("NOTA_CREDITO_ID", notaCredito.getId());
        r.printReport(true);
    }

    double getCreditoDisponible(Cliente cliente) {
        Object[] o = (Object[]) getEntityManager().createQuery(""
                + "SELECT sum(o.importe), sum (o.desacreditado) "
                + "FROM " + CLASS_NAME + " o "
                + "WHERE o.anulada = FALSE AND o.cliente.id =" + cliente.getId()).getSingleResult();
        double creditoDisponible;
        try {
            creditoDisponible = ((Double) o[0]) - ((Double) o[1]);
        } catch (NullPointerException e) {
            //Cliente hasn't got registers on table NotaCredito.. so o[0] and o[1] are null
            creditoDisponible = 0;
        }
        return creditoDisponible;
    }

    /**
     * Find entities of {@link NotaCredito#cliente} = cliente
     * @param cliente
     * @return
     * @throws DatabaseErrorException
     */
    List<NotaCredito> findNotaCreditoFrom(Cliente cliente) {
        return findNotaCreditoFrom(cliente, null);
    }

    List<NotaCredito> findNotaCreditoFrom(Cliente cliente, boolean anulada) {
        return findNotaCreditoFrom(cliente, Boolean.valueOf(anulada));
    }

    private List<NotaCredito> findNotaCreditoFrom(Cliente cliente, Boolean anulada) {
        String filtro = (anulada == null) ? "" : " AND o.anulada= " + anulada;
        return DAO.getEntityManager().createQuery(
                "SELECT o FROM " + CLASS_NAME + " o "
                + "WHERE o.cliente.id= " + cliente.getId()
                + filtro).getResultList();
    }

    /**
     * Resta crédito de las {@link NotaCredito} del {@link Cliente}
     * @param recibo El cual en su detalle ({@link Recibo#detalleReciboList}) contiene las facturas que fueron acreditadas
     * @param cliente Al cual se le van desacreditar
     * @param montoDesacreditar monto que se le va a desacreditar. Si es &lt=0, no se hace nada.
     * @return la diferencia entre (montoDesacreditar - lo que se pudo desacreditar)
     */
    double desacreditar(Recibo recibo, Cliente cliente, double montoDesacreditar) {
        List<NotaCredito> notaCreditoList = findNotaCreditoAcreditables(cliente);
        List<DetalleRecibo> acreditadosList = new ArrayList<DetalleRecibo>(5);
        for (DetalleRecibo detalleRecibo : recibo.getDetalleReciboList()) {
            if (detalleRecibo.isAcreditado()) {
                acreditadosList.add(detalleRecibo);
            }
        }
        Iterator<NotaCredito> notaCreditoIterator = notaCreditoList.iterator();
        Iterator<DetalleRecibo> detalleReciboIterator = acreditadosList.iterator();
        EntityManager em = getEntityManager();
        NotaCredito notaCredito = null;
        DetalleRecibo detalleRecibo = null;
        DetalleAcreditacion detalleAcreditacion;
        //flag, acumulador de desacreditacion
        double totalDesacreditado = 0.0;
        //monto del detalle del recibo que tiene que ser acreditado
        double montoEntrega = 0;
        //credito del cual dispone la nota de creditoy que va ir decreciendo a
        //medidad que se acrediten los montoEntrega
        double creditoRestante = 0;
        try {
            em.getTransaction().begin();
            while (totalDesacreditado < montoDesacreditar) {
                if (creditoRestante <= 0) {
                    notaCredito = notaCreditoIterator.next();
                    System.out.println("CRE:" + notaCredito);
                    creditoRestante = notaCredito.getImporte() - notaCredito.getDesacreditado();
                }
                if (montoEntrega <= 0) {
                    detalleRecibo = detalleReciboIterator.next();
                    System.out.println("DET:" + detalleRecibo);
                    montoEntrega = detalleRecibo.getMontoEntrega();
                }
                detalleAcreditacion = new DetalleAcreditacion();

                //si la notaCredito no es suficiente para cubrir la acreditación
                if (creditoRestante <= montoEntrega) {
                    notaCredito.setDesacreditado(notaCredito.getImporte());
                    montoEntrega -= creditoRestante;
                    detalleAcreditacion.setMonto(creditoRestante);
                    creditoRestante = 0.0; //renovar notaCredito
                } else {
                    notaCredito.setDesacreditado(notaCredito.getDesacreditado() + montoEntrega);
                    detalleAcreditacion.setMonto(montoEntrega);
                    montoEntrega = 0.0; // renovar detalleRecibo
                }
                detalleAcreditacion.setDetalleRecibo(detalleRecibo);
                detalleAcreditacion.setNotaCredito(notaCredito);
                em.merge(notaCredito);
                em.persist(detalleAcreditacion);
                totalDesacreditado += detalleAcreditacion.getMonto();
                System.out.println("total ->" + totalDesacreditado + "/" + montoDesacreditar);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            Logger.getLogger(NotaCreditoJpaController.class).log(Level.DEBUG, "Error desacreditar()", ex);
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return totalDesacreditado;
    }

    /**
     * Acredita el monto de {@link DetalleAcreditacion} a la {@link NotaCredito}
     * Este método se utiliza cuando se anula un {@link Recibo} o
     * {@link FacturaVenta#formaPago} == {@link Valores.FormaPago#CTA_CTE}
     * @param detalle {@link DetalleAcreditacion} que fue anulado.
     * @return instancia de {@link NotaCredito} que fue modificada (acreditada nuevamente).
     */
    NotaCredito acreditar(DetalleAcreditacion anular) {
        if (!anular.isAnulado()) {
            throw new IllegalArgumentException(DetalleAcreditacion.class + " must be anulado == TRUE");
        }
        NotaCredito notaCredito = anular.getNotaCredito();
        notaCredito.setDesacreditado(notaCredito.getDesacreditado() - anular.getMonto());
        DAO.doMerge(notaCredito);
        return notaCredito;
    }

    /**
     * NotaCredito cuyo...  
     *  {@link NotaCredito#anulada} == false && {@link NotaCredito#importe} &lt> {@link NotaCredito#desacreditado}
     * @param cliente Del cual se quieren las NotaCredito
     * @return chocolate..
     */
    List<NotaCredito> findNotaCreditoAcreditables(Cliente cliente) {
        return getEntityManager().createQuery(
                "SELECT o FROM " + CLASS_NAME + " o WHERE o.importe <> o.desacreditado AND o.anulada = FALSE AND o.cliente.id=" + cliente.getId()).getResultList();
    }
}

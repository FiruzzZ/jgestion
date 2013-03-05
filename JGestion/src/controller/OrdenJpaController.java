package controller;

import controller.exceptions.*;
import entity.Orden;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import entity.DetalleOrden;
import entity.Producto;
import entity.Stock;
import entity.Sucursal;
import generics.AutoCompleteComboBox;
import utilities.general.UTIL;
import gui.JDBuscador;
import gui.JDFacturaCompra;
import gui.PanelBuscadorOrdenes;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;
import jpa.controller.ProductoJpaController;
import net.sf.jasperreports.engine.JRException;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class OrdenJpaController {

    public static final String CLASS_NAME = Orden.class.getSimpleName();
    private JDFacturaCompra jdFactura;
    private Producto producto_selected;
    private JDBuscador buscador;
    private PanelBuscadorOrdenes panel;
    private Orden selectedOrden;

    public OrdenJpaController() {
    }

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(Orden orden) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.persist(orden);
        em.getTransaction().commit();
    }

    public void edit(Orden orden) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Orden persistentOrden = em.find(Orden.class, orden.getId());
            List<DetalleOrden> detalleOrdenListOld = persistentOrden.getDetalleOrdenList();
            List<DetalleOrden> detalleOrdenListNew = orden.getDetalleOrdenList();
            List<String> illegalOrphanMessages = null;
            for (DetalleOrden detalleOrdenListOldDetalleOrden : detalleOrdenListOld) {
                if (!detalleOrdenListNew.contains(detalleOrdenListOldDetalleOrden)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain DetalleOrden " + detalleOrdenListOldDetalleOrden + " since its orden field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<DetalleOrden> attachedDetalleOrdenListNew = new ArrayList<DetalleOrden>();
            for (DetalleOrden detalleOrdenListNewDetalleOrdenToAttach : detalleOrdenListNew) {
                detalleOrdenListNewDetalleOrdenToAttach = em.getReference(detalleOrdenListNewDetalleOrdenToAttach.getClass(), detalleOrdenListNewDetalleOrdenToAttach.getId());
                attachedDetalleOrdenListNew.add(detalleOrdenListNewDetalleOrdenToAttach);
            }
            detalleOrdenListNew = attachedDetalleOrdenListNew;
            orden.setDetalleOrdenList(detalleOrdenListNew);
            orden = em.merge(orden);
            for (DetalleOrden detalleOrdenListNewDetalleOrden : detalleOrdenListNew) {
                if (!detalleOrdenListOld.contains(detalleOrdenListNewDetalleOrden)) {
                    Orden oldOrdenOfDetalleOrdenListNewDetalleOrden = detalleOrdenListNewDetalleOrden.getOrden();
                    detalleOrdenListNewDetalleOrden.setOrden(orden);
                    detalleOrdenListNewDetalleOrden = em.merge(detalleOrdenListNewDetalleOrden);
                    if (oldOrdenOfDetalleOrdenListNewDetalleOrden != null && !oldOrdenOfDetalleOrdenListNewDetalleOrden.equals(orden)) {
                        oldOrdenOfDetalleOrdenListNewDetalleOrden.getDetalleOrdenList().remove(detalleOrdenListNewDetalleOrden);
                        oldOrdenOfDetalleOrdenListNewDetalleOrden = em.merge(oldOrdenOfDetalleOrdenListNewDetalleOrden);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = orden.getId();
                if (findOrden(id) == null) {
                    throw new NonexistentEntityException("The orden with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Orden> findOrdenEntities() {
        return findOrdenEntities(true, -1, -1);
    }

    public List<Orden> findOrdenEntities(int maxResults, int firstResult) {
        return findOrdenEntities(false, maxResults, firstResult);
    }

    private List<Orden> findOrdenEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Orden as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Orden findOrden(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Orden.class, id);
        } finally {
            em.close();
        }
    }

    public int getOrdenCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from Orden as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    private int getNextNumeroOrden() {
        try {
            Query q = getEntityManager().createQuery("select MAX(o.numero) from Orden as o");
            return 1 + Integer.valueOf(q.getSingleResult().toString());
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public void initOrden(JFrame owner, boolean visible) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ORDENES_IO);
        jdFactura = new JDFacturaCompra(owner, true);
        jdFactura.getBtnAnular().setVisible(false);
        jdFactura.setUIToOrden();
        jdFactura.setTitle("ORDEN de Entrada/Salida");
        UTIL.getDefaultTableModel(jdFactura.getjTable1(),
                new String[]{"entity", "CÓDIGO", "PRODUCTO", "CANT."},
                new int[]{1, 80, 150, 20});
        UTIL.hideColumnTable(jdFactura.getjTable1(), 0);
        if (visible) {
            UTIL.loadComboBox(jdFactura.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), false);
            UTIL.loadComboBox(jdFactura.getCbProductos(), new ProductoController().findWrappedProductoToCombo(true), false);
            // <editor-fold defaultstate="collapsed" desc="ajuste de foco, problemas de GUI">
            jdFactura.getTfCantidad().addFocusListener(new FocusAdapter() {

                @Override
                public void focusLost(FocusEvent e) {
                    jdFactura.getBtnADD().requestFocus();
                }
            });// </editor-fold>
            jdFactura.getCbProductos().setEditable(true);
            JTextComponent editor = (JTextComponent) jdFactura.getCbProductos().getEditor().getEditorComponent();
            // change the editor's documenteishon
            editor.setDocument(new AutoCompleteComboBox(jdFactura.getCbProductos()));
            editor.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        @SuppressWarnings("unchecked")
                        ComboBoxWrapper<Producto> wrapper = (ComboBoxWrapper<Producto>) jdFactura.getCbProductos().getSelectedItem();
                        producto_selected = new ProductoJpaController().find(wrapper.getId());
                        setProducto(producto_selected);
                        //problemas de GUI design, sino el foco se va a la mierda..
                        if (producto_selected != null) {
                            jdFactura.getTfCantidad().requestFocus();
                        }
                    } catch (ClassCastException ex) {
                        //cuando no seleccionó ningún Producto del combo
                        jdFactura.setTfPrecioUnitario("");
                        jdFactura.setTfProductoIVA("");
                    }
                }
            });
            editor.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == 10) {
                        try {
                            @SuppressWarnings("unchecked")
                            ComboBoxWrapper<Producto> wrapper = (ComboBoxWrapper<Producto>) jdFactura.getCbProductos().getSelectedItem();
                            producto_selected = new ProductoJpaController().find(wrapper.getId());
                            setProducto(producto_selected);
                            if (producto_selected != null) {
                                jdFactura.setTfProductoPrecioActual(new StockController().getStockGlobal(producto_selected.getId()).toString());
                            }
                        } catch (ClassCastException ex) {
                            jdFactura.setTfProductoPrecioActual("");
                            jdFactura.setTfPrecioUnitario("");
                            jdFactura.setTfProductoIVA("");
                        }
                    }
                }
            });
            jdFactura.getBtnADD().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        int cantidad = Integer.valueOf(jdFactura.getTfCantidad().getText());
                        if (producto_selected != null) {
                            UTIL.getDtm(jdFactura.getjTable1()).addRow(new Object[]{
                                        producto_selected,
                                        producto_selected.getCodigo(),
                                        producto_selected.getNombre(),
                                        cantidad
                                    });
                        }
                    } catch (NumberFormatException ex) {
                        jdFactura.showMessage("Cantidad no válida", null, 2);
                    }

                }
            });
            jdFactura.getBtnDEL().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (jdFactura.getjTable1().getSelectedRow() > -1) {
                        jdFactura.getDtm().removeRow(jdFactura.getjTable1().getSelectedRow());
                    }
                }
            });
            jdFactura.getTfProductoCodigo().addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        producto_selected = new ProductoController().findProductoByCodigo(jdFactura.getTfProductoCodigo().getText().trim());
                        setProducto(producto_selected);
                    }
                }
            });
            jdFactura.getBtnAceptar().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Orden orden = setAndPersist();
                        jdFactura.showMessage("Orden Nº" + orden.getNumero() + " registrada", null, 1);
                        doReport(orden.getId());
                        UTIL.limpiarDtm(jdFactura.getjTable1());
                        producto_selected = null;
                    } catch (MessageException ex) {
                        jdFactura.showMessage(ex.getMessage(), "Error", 2);
                    } catch (Exception ex) {
                        jdFactura.showMessage(ex.getMessage(), "Error", 2);
                        Logger.getLogger(OrdenJpaController.class.getSimpleName()).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    }
                }
            });
        }

        jdFactura.pack();
        jdFactura.setLocationRelativeTo(owner);
        jdFactura.setVisible(visible);
    }

    private void setProducto(Producto producto) {
        if (producto != null) {
            jdFactura.labelCodigoNoRegistrado(false);
            jdFactura.setTfProductoCodigo(producto.getCodigo());
            UTIL.setSelectedItem(jdFactura.getCbProductos(), producto.getNombre());
            jdFactura.setTfProductoPrecioActual(String.valueOf(producto.getStockactual()));
            jdFactura.getTfCantidad().requestFocus();
        } else {
            jdFactura.labelCodigoNoRegistrado(true);
            jdFactura.setTfProductoPrecioActual("");
        }
    }

    private Orden setAndPersist() throws MessageException, Exception {
        int rowCant = jdFactura.getDtm().getRowCount();
        if (rowCant < 1) {
            throw new MessageException("Y los productos a modificar?");
        }

        Orden orden = new Orden();
        try {
            @SuppressWarnings("unchecked")
            ComboBoxWrapper<Sucursal> wrapper = (ComboBoxWrapper<Sucursal>) jdFactura.getCbSucursal().getSelectedItem();
            orden.setSucursal(wrapper.getEntity());
        } catch (ClassCastException ex) {
            throw new MessageException("Sucursal no válida");
        }
        orden.setNumero(getNextNumeroOrden());
        orden.setUsuario(UsuarioController.getCurrentUser());
        orden.setDetalleOrdenList(new ArrayList<DetalleOrden>(rowCant));
        for (int i = 0; i < rowCant; i++) {
            DetalleOrden detalleOrden = new DetalleOrden();
            detalleOrden.setOrden(orden);
            detalleOrden.setProducto((Producto) jdFactura.getjTable1().getModel().getValueAt(i, 0));
            detalleOrden.setCantidad(Integer.valueOf(jdFactura.getjTable1().getModel().getValueAt(i, 3).toString()));
            orden.getDetalleOrdenList().add(detalleOrden);
        }
        create(orden);
        StockController stockController = new StockController();
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        for (DetalleOrden detalleOrden : orden.getDetalleOrdenList()) {
            try {
                stockController.modificarStockBySucursal(detalleOrden.getProducto(), orden.getSucursal(), detalleOrden.getCantidad());
            } catch (NoResultException ex) {
                System.out.println("!E: " + detalleOrden.getProducto() + " -> " + orden.getSucursal());
                Stock stock = new Stock();
                stock.setProducto(detalleOrden.getProducto());
                stock.setSucursal(orden.getSucursal());
                stock.setUsuario(orden.getUsuario());
                stock.setStockSucu(0);
                stock.setFechaCarga(orden.getFecha() != null ? orden.getFecha() : new Date());
                stockController.create(stock);
                stockController.modificarStockBySucursal(detalleOrden.getProducto(), orden.getSucursal(), detalleOrden.getCantidad());
            }
            Producto p = em.find(Producto.class, detalleOrden.getProducto().getId());
            p.setStockactual(p.getStockactual() + detalleOrden.getCantidad());
            em.merge(p);
        }
        em.getTransaction().commit();
        em.close();
        return orden;
    }

    public void initBuscador(JFrame owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ORDENES_IO);
        panel = new PanelBuscadorOrdenes();
        UTIL.loadComboBox(panel.getCbSucursales(), new UsuarioHelper().getWrappedSucursales(), "<Todas>");
        buscador = new JDBuscador(owner, "Buscador de " + CLASS_NAME, true, panel);
        UTIL.getDefaultTableModel(buscador.getjTable1(),
                new String[]{"entity", "Nº", "Sucursal", "Fecha", "Usuario"},
                new int[]{1, 40, 60, 60, 60});
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getbImprimir().setVisible(false);
        buscador.getbLimpiar().setVisible(false);
        buscador.getjTable1().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    try {
                        selectedOrden = (Orden) UTIL.getSelectedValue(buscador.getjTable1(), 0);
                        setDatos(selectedOrden);
                    } catch (MessageException ex) {
                        //ignored...
                    }
                }
            }
        });
        buscador.getbBuscar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                armarQuery();
            }
        });

        buscador.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private void armarQuery() {
        StringBuilder query = new StringBuilder("SELECT * FROM orden o WHERE o.id IS NOT NULL");

        if (panel.getCbSucursales().getSelectedIndex() > 0) {
            ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) panel.getCbSucursales().getSelectedItem();
            query.append(" AND o.sucursal= ").append(cbw.getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < panel.getCbSucursales().getItemCount(); i++) {
                ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) panel.getCbSucursales().getItemAt(i);
                query.append(" o.sucursal=").append(cbw.getId());
                if ((i + 1) < panel.getCbSucursales().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (panel.getDcDesde().getDate() != null) {
            query.append(" AND o.fecha >= '").append(panel.getDcDesde().getDate()).append("'");
        }
        if (panel.getDcHasta().getDate() != null) {
            query.append(" AND o.fecha <= '").append(panel.getDcHasta().getDate()).append("'");
        }

        query.append(" ORDER BY o.id");
        cargarTablaBuscador(query.toString());
    }

    private void cargarTablaBuscador(String query) {
        UTIL.limpiarDtm(buscador.getjTable1());
        List<Orden> l;
        try {
            l = (List<Orden>) DAO.getNativeQueryResultList(query, Orden.class);
            for (Orden orden : l) {
                buscador.getDtm().addRow(new Object[]{
                            orden,
                            orden.getNumero(),
                            orden.getSucursal().getNombre(),
                            UTIL.DATE_FORMAT.format(orden.getFecha()) + "(" + UTIL.TIME_FORMAT.format(orden.getFecha()) + ")",
                            orden.getUsuario()
                        });
            }
        } catch (DatabaseErrorException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            Logger.getLogger(OrdenJpaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setDatos(Orden orden) throws MessageException {
        if (jdFactura == null) {
            initOrden(null, false);
            jdFactura.getBtnADD().setEnabled(false);
            jdFactura.getBtnDEL().setEnabled(false);
            jdFactura.getBtnAceptar().setEnabled(false);
            jdFactura.getBtnCancelar().setEnabled(false);
            jdFactura.getBtnAnular().setVisible(false);
            jdFactura.getTfNumMovimiento().setVisible(true);
            jdFactura.getCbProductos().setEnabled(false);
            jdFactura.getTfProductoCodigo().setEnabled(false);
            jdFactura.getTfCantidad().setEnabled(false);
            jdFactura.pack();
        }
        jdFactura.getTfNumMovimiento().setText(orden.getNumero() + "");
        jdFactura.getCbSucursal().removeAllItems();
        jdFactura.getCbSucursal().addItem(new ComboBoxWrapper<Sucursal>(orden.getSucursal(), orden.getId(), orden.getSucursal().getNombre()));
        for (DetalleOrden detalleOrden : orden.getDetalleOrdenList()) {
            UTIL.getDtm(jdFactura.getjTable1()).addRow(new Object[]{
                        detalleOrden.getProducto(),
                        detalleOrden.getProducto().getCodigo(),
                        detalleOrden.getProducto().getNombre() + " " + detalleOrden.getProducto().getMarca().getNombre(),
                        detalleOrden.getCantidad()
                    });
        }
        jdFactura.setVisible(true);
    }

    private void doReport(int numeroOrden) throws MissingReportException, JRException {
        Reportes r = new Reportes("JGestion_ordenio.jasper", "Orden IO N°" + numeroOrden);
        r.addMembreteParameter();
        r.addParameter("ORDEN_ID", numeroOrden);
        r.printReport(true);
    }
}

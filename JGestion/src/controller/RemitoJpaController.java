package controller;

import controller.exceptions.*;
import entity.Cliente;
import entity.Remito;
import entity.DetalleRemito;
import entity.FacturaVenta;
import entity.Sucursal;
import gui.JDFacturaVenta;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import utilities.general.UTIL;
import gui.JDBuscadorReRe;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class RemitoJpaController implements ActionListener, KeyListener {

    public static final String CLASS_NAME = Remito.class.getSimpleName();
    private final FacturaVentaJpaController facturaVentaController;
    private JDFacturaVenta facturaVentaUI;
    private JDBuscadorReRe buscador;
    private boolean MODO_VISTA;
    private Remito selectedRemito;
    private JDFacturaVenta jdFacturaVenta;
    private boolean toFacturar;

    public RemitoJpaController() {
        facturaVentaController = new FacturaVentaJpaController();
    }

    // <editor-fold defaultstate="collapsed" desc="CRUD..">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public Remito create(Remito remito) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<DetalleRemito> detalleRemitoList = remito.getDetalleRemitoList();
            remito.setDetalleRemitoList(new ArrayList<DetalleRemito>());
            em.persist(remito);
            em.getTransaction().commit();
            //por que no respeta el orden en q fueron agregados los items a la List
            for (DetalleRemito detalleRemito : detalleRemitoList) {
                detalleRemito.setRemito(remito);
                DAO.create(detalleRemito);
            }
            remito.setDetalleRemitoList(detalleRemitoList);
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return remito;
    }

    public void edit(Remito remito) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.find(Remito.class, remito.getId());
            remito = em.merge(remito);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = remito.getId();
                if (findRemito(id) == null) {
                    throw new NonexistentEntityException("The remito " + remito.getNumero() + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Remito> findRemitoEntities() {
        return findRemitoEntities(true, -1, -1);
    }

    public List<Remito> findRemitoEntities(int maxResults, int firstResult) {
        return findRemitoEntities(false, maxResults, firstResult);
    }

    private List<Remito> findRemitoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Remito as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Remito findRemito(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Remito.class, id);
        } finally {
            em.close();
        }
    }

    public int getRemitoCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from Remito as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    public JDBuscadorReRe getBuscador() {
        return buscador;
    }

    public void initRemito(JFrame frame, boolean modal, boolean setVisible, boolean loadDefaultData) throws MessageException {
        facturaVentaController.initFacturaVenta(frame, modal, this, 3, setVisible, loadDefaultData);
        //implementación del boton Aceptar
        facturaVentaController.getContenedor().getBtnAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(false);
                    doRemito();
                } catch (MessageException ex) {
                    facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
                    ex.printStackTrace();
                } finally {
                    facturaVentaController.getContenedor().getBtnAceptar().setEnabled(true);
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //todo las demás acciones son manejadas (delegadas) -> FacturaVentaJpaController
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("filtrarReRe")) {
                try {
                    armarQuery();
                } catch (MessageException ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
                } catch (Exception ex) {
                    buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
                    ex.printStackTrace();
                }
            } else {
                facturaVentaController.actionPerformed(e);
            }
        }
    }

    public void keyTyped(KeyEvent e) {
        facturaVentaController.keyTyped(e);
    }

    public void keyReleased(KeyEvent e) {
        facturaVentaController.keyReleased(e);
    }

    @Deprecated
    public void keyPressed(KeyEvent e) {
    }

    private void doRemito() throws MessageException, Exception {
        if (MODO_VISTA) {
            doImprimir(selectedRemito);
        } else {
            //obtiene la Vista que contiene todos los datos
            facturaVentaUI = facturaVentaController.getContenedor();
            Cliente selectedCliente;
            Sucursal selectedSucursal;

            // <editor-fold defaultstate="collapsed" desc="CONTROLES">
            try {
                selectedCliente = (Cliente) facturaVentaUI.getCbCliente().getSelectedItem();
            } catch (ClassCastException ex) {
                throw new MessageException("Cliente no válido");
            }
            try {
                selectedSucursal = (Sucursal) facturaVentaUI.getCbSucursal().getSelectedItem();
            } catch (ClassCastException ex) {
                throw new MessageException("Sucursal no válido");
            }

            if (facturaVentaUI.getDcFechaFactura() == null) {
                throw new MessageException("Fecha no válida");
            }

            javax.swing.table.DefaultTableModel dtm = facturaVentaUI.getDTM();
            if (dtm.getRowCount() < 1) {
                throw new MessageException(CLASS_NAME + " debe tener al menos un item.");
            }
            // </editor-fold>

            Remito newRemito = new Remito();
            newRemito.setNumero(Long.valueOf(facturaVentaUI.getTfFacturaCuarto() + facturaVentaUI.getTfFacturaOcteto()));
            newRemito.setCliente(selectedCliente);
            newRemito.setSucursal(selectedSucursal);
            newRemito.setFechaRemito(facturaVentaUI.getDcFechaFactura());
            newRemito.setUsuario(UsuarioJpaController.getCurrentUser());
            newRemito.setDetalleRemitoList(new ArrayList<DetalleRemito>(dtm.getRowCount()));
            // carga de detalleVenta
            DetalleRemito detalleRemito;
            for (int i = 0; i < dtm.getRowCount(); i++) {
                detalleRemito = new DetalleRemito();
                detalleRemito.setProducto(new ProductoController().findProductoByCodigo(dtm.getValueAt(i, 1).toString()));
                detalleRemito.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
                detalleRemito.setRemito(newRemito);
                newRemito.getDetalleRemitoList().add(detalleRemito);
            }
            try {
                newRemito = create(newRemito);
            } catch (PreexistingEntityException ex) {
                org.apache.log4j.Logger.getLogger(PresupuestoController.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            } catch (Exception ex) {
                throw ex;
            }
            doImprimir(newRemito);
            limpiarPanel();
        }
    }

    public Long getNextNumero(Sucursal s) {
        EntityManager em = getEntityManager();
        Long nextRemitoNumero = 1L;
        try {
            nextRemitoNumero = 1 + (Long) em.createQuery("SELECT MAX(o.numero)"
                    + " FROM " + CLASS_NAME + " o"
                    + " WHERE o.sucursal.id=" + s.getId()).getSingleResult();
        } catch (NullPointerException ex) {
            System.out.println("pintó el 1er Remito ");
        } finally {
            em.close();
        }
        return nextRemitoNumero;
    }

    private void doImprimir(Remito p) {
        try {
            Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_Remito.jasper", "Remito");
            r.addParameter("REMITO_ID", p.getId());
            r.printReport(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            Logger.getLogger(this.getClass()).error("Error - Impresión de Remito.id=" + p.getId(), ex);
        }
    }

    private void limpiarPanel() {
        facturaVentaController.borrarDetalles();
        facturaVentaController.getContenedor().setTfNumMovimiento(String.valueOf(getRemitoCount() + 1));
        Sucursal s = (Sucursal) facturaVentaController.getContenedor().getCbSucursal().getSelectedItem();
        facturaVentaController.setNumeroFactura(s, getNextNumero(s));
    }

    public void initBuscador(JDialog dialog, boolean modal, boolean setVisible) {
        buscador = new JDBuscadorReRe(dialog, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
        initBuscador(setVisible);
    }

    /**
     * Inicia el buscador con el JFrame como padre
     *
     * @param frame
     * @param modal
     * @param setVisible
     */
    public void initBuscador(JFrame frame, boolean modal, boolean setVisible) {
        buscador = new JDBuscadorReRe(frame, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
        initBuscador(setVisible);
    }

    private void initBuscador(boolean setVisible) {
        buscador.getjTable1().addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() >= 2) {
                    selectedRemito = (Remito) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0);
                    if (toFacturar) {
                        //cuando se va relacionar un Remito a una FacturaVenta
                        buscador.dispose();
                    } else {
                        setDatos(selectedRemito);
                    }
                }
            }
        });

        //personalizando vista de Buscador
        buscador.setPanelInfoParaRemitos();
        UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteJpaController().findEntities(), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getSucursales(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Nº " + CLASS_NAME, "Nº Factura", "Cliente", "Fecha", "Sucursal", "Usuario"},
                new int[]{15, 20, 50, 50, 80, 50});
        MODO_VISTA = true;
        buscador.setListeners(this);
        buscador.setVisible(setVisible);
    }

    private void setDatos(Remito remito) {
        try {
            initRemito(null, true, false, false);
        } catch (MessageException ex) {
            // no va saltar nunca este
        }
        jdFacturaVenta = facturaVentaController.getContenedor();
        jdFacturaVenta.setTfNumMovimiento(remito.getId().toString());
        jdFacturaVenta.setLocationRelativeTo(buscador);
        String numFactura = UTIL.AGREGAR_CEROS(remito.getNumero(), 12);
        jdFacturaVenta.setTfFacturaCuarto(numFactura.substring(0, 4));
        jdFacturaVenta.setTfFacturaOcteto(numFactura.substring(4));
        jdFacturaVenta.getCbCliente().removeAllItems();
        jdFacturaVenta.getCbCliente().addItem(remito.getCliente());
        jdFacturaVenta.getCbSucursal().removeAllItems();
        jdFacturaVenta.getCbSucursal().addItem(remito.getSucursal());
//      jdFacturaVenta.getCbUsuario().addItem(remito.getUsuario());
        jdFacturaVenta.setDcFechaFactura(remito.getFechaRemito());

        List<DetalleRemito> lista = remito.getDetalleRemitoList();
        javax.swing.table.DefaultTableModel dtm = jdFacturaVenta.getDTM();
        for (DetalleRemito detallesPresupuesto : lista) {
            dtm.addRow(new Object[]{
                        null,
                        detallesPresupuesto.getProducto().getCodigo(),
                        detallesPresupuesto.getProducto(),
                        detallesPresupuesto.getCantidad(),
                        0,
                        0,
                        0,
                        0
                    });
        }

        jdFacturaVenta.modoVista();
        jdFacturaVenta.setLocation(jdFacturaVenta.getX() + 50, jdFacturaVenta.getY() + 50);
        jdFacturaVenta.setVisible(true);
    }

    private void armarQuery() throws MessageException {
        StringBuilder query = new StringBuilder("SELECT o.* FROM remito o"
                + " WHERE o.id > -1 ");

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
            query.append(" AND o.fecha_remito >='").append(buscador.getDcDesde()).append("'");
        }
        if (buscador.getDcHasta() != null) {
            query.append(" AND o.fecha_remito <='").append(buscador.getDcHasta()).append("'");
        }
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            query.append(" AND o.sucursal= ").append(((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                Sucursal s = (Sucursal) buscador.getCbSucursal().getItemAt(i);
                query.append(" o.sucursal=").append(s.getId());
                if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            query.append(" AND o.cliente =").append(((Cliente) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
            if (buscador.getCbFormasDePago().getSelectedIndex() == 1) {
                query.append(" AND o.factura_venta IS NULL");
            } else {
                query.append(" AND o.factura_venta IS NOT NULL");
            }
        }
        query.append(" ORDER BY o.id");
        org.apache.log4j.Logger.getLogger(this.getClass()).log(org.apache.log4j.Level.DEBUG, "QUERY: " + query.toString());
        cargarDtmBuscador(query.toString());
    }

    private void cargarDtmBuscador(String query) {
        buscador.dtmRemoveAll();
        javax.swing.table.DefaultTableModel dtm = buscador.getDtm();
        List<Remito> list = DAO.getEntityManager().createNativeQuery(query, Remito.class).getResultList();
        for (Remito remito : list) {
            dtm.addRow(new Object[]{
                        remito, // <--- no es visible
                        remito.getFacturaVenta(),
                        remito.getCliente(),
                        UTIL.DATE_FORMAT.format(remito.getFechaRemito()),
                        remito.getSucursal(),
                        remito.getUsuario()});
        }
    }

    /**
     * Para distingir cuando una selección es para visualizar un Remito y una de
     * para "relaciónar un Remito a una Factura Venta".
     *
     * @param toFacturar
     */
    public void setToFacturar() {
        this.toFacturar = true;
    }

    public Remito getSelectedRemito() {
        return selectedRemito;
    }

    /**
     * Inicializa y hace visible la UI (modal) de busqueda y selección de Remito
     * para relacionarlo con una {@link FacturaVenta}
     *
     * @param owner
     * @param cliente entity {@link Cliente} del cual se van a buscar el Remito
     */
    void initBuscadorToFacturar(JDialog owner, Cliente cliente) {
        buscador = new JDBuscadorReRe(owner, "Buscador - " + CLASS_NAME, true, "Cliente", "Nº " + CLASS_NAME);
        initBuscador(false);
        setToFacturar();
        UTIL.setSelectedItem(buscador.getCbClieProv(), cliente);
        buscador.getCbFormasDePago().setSelectedIndex(1); // los NO facturados
        buscador.getCbFormasDePago().setEnabled(false);
        buscador.getCbClieProv().setEnabled(false);
        buscador.setVisible(true);
    }
}

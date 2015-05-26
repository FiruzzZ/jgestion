package jgestion.controller;

import jgestion.entity.Rubro;
import jgestion.entity.Stock;
import jgestion.entity.Sucursal;
import jgestion.entity.Unidadmedida;
import jgestion.entity.Iva;
import jgestion.entity.Producto;
import jgestion.entity.ListaPrecios;
import jgestion.entity.Marca;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelProductoReporteOptions;
import jgestion.gui.JDABM;
import jgestion.gui.PanelProductoListados;
import jgestion.gui.PanelABMProductos;
import jgestion.gui.JDBuscador;
import jgestion.gui.PanelBuscadorMovimientosPro;
import jgestion.gui.JDStockGral;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import jgestion.controller.exceptions.DatabaseErrorException;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.controller.exceptions.NonexistentEntityException;
import generics.GenericBeanCollection;
import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.NoResultException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import jgestion.JGestionUtils;
import jgestion.jpa.controller.ProductoJpaController;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.general.EntityWrapper;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author FiruzzZ
 */
public class ProductoController implements ActionListener, KeyListener {

    private static final Logger LOG = Logger.getLogger(ProductoController.class.getName());
    public static final String CLASS_NAME = Producto.class.getSimpleName();
    private JDContenedor contenedor;
    private JDABM abm;
    private final String[] colsName = {"id", "Código", "Nombre", "Marca", "Stock Gral.", "Costo C.", "Precio V."};
    private final int[] colsWidth = {1, 50, 200, 50, 20, 50, 50};
    private PanelABMProductos panel;
    private Producto EL_OBJECT;
    /**
     * almacena temporalmente el archivo de la imagen del producto
     */
    private File fotoFile;
    /**
     * Formas de registrar el COSTO COMPRA de los productos
     */
    public static final int PPP = 3, ANTIGUO = 2, ULTIMA_COMPRA = 1;
    private PanelBuscadorMovimientosPro panelito;
    private JDBuscador buscador;
    private PanelProductoListados panelProductoListados;
    private boolean permitirFiltroVacio;
    private final ProductoJpaController jpaController;

    public ProductoController() {
        jpaController = new ProductoJpaController();
    }

    public void initContenedor(Window owner, boolean modal, boolean modoBuscador) throws DatabaseErrorException {
        contenedor = new JDContenedor(owner, modal, "ABM - " + CLASS_NAME);
        contenedor.setSize(contenedor.getWidth() + 200, contenedor.getHeight());
        contenedor.getTfFiltro().setToolTipText("Filtra por nombre del " + CLASS_NAME);
        contenedor.setModoBuscador(modoBuscador);
        contenedor.getbNuevo().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PRODUCTOS);
                    displayABM(null);
                } catch (IOException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                }
            }
        });
        contenedor.getbModificar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PRODUCTOS);
                    Integer productoID = (Integer) UTIL.getSelectedValueFromModel(contenedor.getjTable1(), 0);
                    if (productoID == null) {
                        throw new MessageException("Seleccione el producto que desea modificar");
                    }
                    displayABM(jpaController.find(productoID));
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            }
        });
        contenedor.getbBorrar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PRODUCTOS);
                    eliminarProducto();
                    contenedor.showMessage("Producto eliminado..", CLASS_NAME, 1);
                } catch (NonexistentEntityException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (DatabaseErrorException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                }
            }
        });
        contenedor.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    doDynamicReportProductos();
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), "Advertencia", 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), "Error REPORT!", 0);
                }
            }
        });
        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String tf = contenedor.getTfFiltro().getText().trim();
                try {
                    if (tf.length() > 0) {
                        permitirFiltroVacio = true;
                        armarQueryContenedor(tf);
                    } else {
                        if (permitirFiltroVacio) {
                            permitirFiltroVacio = false;
                            armarQueryContenedor(tf);

                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ProductoController.class).fatal("Error recuperando Productos en Contenedor", ex);
                    JOptionPane.showMessageDialog(null, ex);
                }
            }
        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
        contenedor.getjTable1().getColumnModel().getColumn(4).setCellRenderer(NumberRenderer.getIntegerRenderer());
        contenedor.getjTable1().getColumnModel().getColumn(5).setCellRenderer(NumberRenderer.getCurrencyRenderer(4));
        contenedor.getjTable1().getColumnModel().getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer(4));
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        //no permite filtro de vacio en el inicio
        permitirFiltroVacio = false;
        armarQueryContenedor(null);
        contenedor.setListener(this);
        contenedor.setVisible(true);
    }

    public void displayABM(Producto toEdit) throws IOException {
        EL_OBJECT = toEdit;
        fotoFile = null;
        initPanelABM();
        abm = new JDABM(contenedor, "ABM - " + CLASS_NAME + "s", true, panel);
        if (toEdit != null) {
            setPanelABM(EL_OBJECT);
        }
        abm.setLocationRelativeTo(contenedor);
        abm.setListener(this);
        abm.setVisible(true);
    }

    private void initPanelABM() {
        panel = new PanelABMProductos();
        panel.hideSucursal();
        panel.setListeners(this);
        panel.getbBuscarFoto().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cargarImagen();
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            }
        });
        panel.getbQuitarFoto().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.getjLabelFoto().setText("[ Sin imagen ]");
                panel.getjLabelFoto().setIcon(null);
                fotoFile = null;
            }
        });
        panel.getBtnAddRubros().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    RubroController rubroController = new RubroController();
                    JDialog jd = rubroController.getABM(abm);
                    jd.setLocationRelativeTo(abm);
                    jd.setVisible(true);
                    UTIL.loadComboBox(panel.getCbRubro(), rubroController.findRubros(), false);
                    UTIL.loadComboBox(panel.getCbSubRubro(), rubroController.findRubros(), true);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        panel.getbStockGral().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    initStockGral(abm, EL_OBJECT);
                } catch (MessageException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            }
        });
        UTIL.loadComboBox(panel.getCbIVA(), new IvaController().findIvaEntities(), false);
        UTIL.loadComboBox(panel.getCbMarcas(), JGestionUtils.getWrappedMarcas(new MarcaController().findAll()), false);
        UTIL.loadComboBox(panel.getCbMedicion(), new UnidadmedidaJpaController().findUnidadmedidaEntities(), false);
        UTIL.loadComboBox(panel.getCbRubro(), new RubroController().findRubros(), false);
        UTIL.loadComboBox(panel.getCbSubRubro(), new RubroController().findRubros(), true);
    }

    private void cargarContenedorTabla(String jpql) {
        if (contenedor != null) {
            DefaultTableModel dtm = contenedor.getDTM();
            dtm.setRowCount(0);
            List<Object[]> l = jpaController.findAttributes(jpql);
            for (Object[] o : l) {
                dtm.addRow(o);
            }
        }
    }

    private void setPanelABM(Producto producto) throws IOException {
        UTIL.setSelectedItem(panel.getCbMarcas(), producto.getMarca().getNombre());
        UTIL.setSelectedItem(panel.getCbIVA(), producto.getIva());
        UTIL.setSelectedItem(panel.getCbMedicion(), producto.getIdunidadmedida().getNombre());
        UTIL.setSelectedItem(panel.getCbRubro(), producto.getRubro().getNombre());
        if (producto.getSubrubro() != null) {
            UTIL.setSelectedItem(panel.getCbSubRubro(), producto.getSubrubro().getNombre());
        }
        panel.setTfCodigo(producto.getCodigo());
        panel.setTfNombre(producto.getNombre());
        panel.setTfStockMinimo(String.valueOf(producto.getStockminimo()));
        panel.setTfStockMax(String.valueOf(producto.getStockmaximo()));
        panel.setTfStockActual(String.valueOf(producto.getStockactual()));
        panel.getCheckUpdatePrecioVenta().setSelected(producto.getUpdatePrecioVenta());
        panel.getCheckBienDeCambio().setSelected(producto.isBienDeCambio());
        if (producto.getPrecioVenta() != null) {
            panel.setTfPrecio(producto.getPrecioVenta().toString());
        }
        panel.getTaDescripcion().setText(producto.getDescripcion());
        panel.setTfCostoCompra(producto.getCostoCompra().toString());
        panel.setDateUltimaCompra(producto.getUltimaCompra());
        if (producto.getFoto() != null) {
            if (producto.getFoto().length > 0) {
                fotoFile = UTIL.imageToFile(producto.getFoto(), null);
                try {
                    UTIL.setImageAsIconLabel(panel.getjLabelFoto(), fotoFile);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error recuperando imagen: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setEntity() throws MessageException, IOException, Exception {
        if (EL_OBJECT == null) {
            EL_OBJECT = new Producto();
        }
        String codigo = panel.getTfCodigo().trim();
        String nombre = panel.getTfNombre().trim();
        Rubro rubro;
        // <editor-fold defaultstate="collapsed" desc="CTRL restrictions......">
        if (codigo.length() < 1) {
            throw new MessageException("Código no válido");
        }
        if (nombre.length() < 1) {
            throw new MessageException("Ingrese un nombre");
        }
        if (nombre.length() > 250) {
            throw new MessageException("Nombre del producto ridículamente largo (máximo 250 caracteres)");
        }

        try {
            if (Integer.valueOf(panel.getTfStockMinimo()) < 0) {
                throw new MessageException("Stock mínimo no válido. Debe ser mayor o igual a 0");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Número de stock mínimo no válido");
        }
        try {
            if (Integer.valueOf(panel.getTfStockMax()) < 0) {
                throw new MessageException("Stock máximo no válido. Debe ser mayor o igual a 0");

            }
        } catch (NumberFormatException ex) {
            throw new MessageException("número de stock máximo no válido");
        }
        try {
            if (panel.getTfPrecio().length() > 0) {
                if (Double.valueOf(panel.getTfPrecio()) < 0) {
                    throw new MessageException("El precio no puede ser menor a 0");
                }
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("monto de Precio no válido");
        }
        try {
            EL_OBJECT.setMarca(((EntityWrapper<Marca>) panel.getCbMarcas().getSelectedItem()).getEntity());
        } catch (ClassCastException ex) {
            throw new MessageException("Debe especificar una Marca");
        }
        try {
            EL_OBJECT.setIva((Iva) panel.getCbIVA().getSelectedItem());
        } catch (ClassCastException ex) {
            throw new MessageException("Debe especificar un IVA");
        }
        try {
            EL_OBJECT.setIdunidadmedida((Unidadmedida) panel.getCbMedicion().getSelectedItem());
        } catch (ClassCastException ex) {
            throw new MessageException("Debe especificar una Unidad de medida");
        }
        try {
            rubro = (Rubro) panel.getCbRubro().getSelectedItem();
            if (panel.getCbSubRubro().getSelectedIndex() > 0) {
                if (rubro.equals((Rubro) panel.getCbSubRubro().getSelectedItem())) {
                    throw new MessageException("El Rubro y Subrubro no pueden ser iguales");
                }
            }
        } catch (ClassCastException ex) {
            throw new MessageException("Debe crear al menos un Rubro para poder dar de alta los Productos."
                    + "\nMenú: Productos -> Rubros");
        }
        // </editor-fold>

        // NOT NULL's
        EL_OBJECT.setCodigo(codigo);
        EL_OBJECT.setNombre(nombre);
        EL_OBJECT.setStockminimo(Integer.valueOf(panel.getTfStockMinimo()));
        EL_OBJECT.setStockmaximo(Integer.valueOf(panel.getTfStockMax()));
        EL_OBJECT.setRubro(rubro);
        if (panel.getCbSubRubro().getSelectedIndex() > 0) {
            EL_OBJECT.setSubrubro((Rubro) panel.getCbSubRubro().getSelectedItem());
        } else {
            EL_OBJECT.setSubrubro(null);
        }
        EL_OBJECT.setPrecioVenta(panel.getTfPrecio().length() > 0 ? new BigDecimal(panel.getTfPrecio()) : BigDecimal.ZERO);
        EL_OBJECT.setUpdatePrecioVenta(panel.getCheckUpdatePrecioVenta().isSelected());
        EL_OBJECT.setBienDeCambio(panel.getCheckBienDeCambio().isSelected());
        // no setteable desde la GUI
        // default's....
        EL_OBJECT.setRemunerativo(true);
        if (EL_OBJECT.getCostoCompra() == null) {
            // este se actualiza cuando se cargan FacturaCompra's
            EL_OBJECT.setCostoCompra(BigDecimal.ZERO);
        }

        // NULLABLE'sssssssss
        EL_OBJECT.setDescripcion((panel.getTaDescrip().trim().isEmpty() ? null : panel.getTaDescrip()));
        if (fotoFile != null) {
            //si se elijió algún archivo imagen ...
            EL_OBJECT.setFoto(UTIL.getBytesFromFile(fotoFile));
        } else {
            if (panel.getjLabelFoto().getIcon() == null) {
                //si se quitó la que había seleccionado o la que estaba antiguamente
                EL_OBJECT.setFoto(null);
            }
        }
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
        if (e.getComponent() instanceof JTextField) {
            JTextField tf = (JTextField) e.getComponent();
            if (tf.getName().equalsIgnoreCase("tfprecio") || tf.getName().equalsIgnoreCase("tfmargen")) {
                UTIL.solo_numeros_y_un_punto(tf.getText(), e);
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    /**
     * Arma la query, la cual va filtrar los datos en el JDContenedor
     *
     * @param filtro
     */
    private void armarQueryContenedor(String filtro) {
        String query = "SELECT p.id, p.codigo, p.nombre, p.marca.nombre, p.stockactual, p.costoCompra, p.precioVenta"
                + " FROM " + jpaController.getEntityClass().getSimpleName() + " p ";
        if (filtro != null && filtro.length() > 0) {
            query += " WHERE UPPER(p.nombre) LIKE '%" + filtro.toUpperCase() + "%' "
                    + "OR UPPER(p.codigo) LIKE '%" + filtro.toUpperCase() + "%'";
        }
        query += " ORDER BY p.nombre";
        cargarContenedorTabla(query);
    }

    private void checkConstraints(Producto object) throws MessageException, Exception {
        String idQuery = "";
        if (object.getId() != null) {
            idQuery = "o.id!=" + object.getId() + " AND ";
        }
        if (!jpaController.findByNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                + " WHERE " + idQuery + "  " + " o.codigo='" + object.getCodigo() + "'").isEmpty()) {
            throw new MessageException("Ya existe un " + CLASS_NAME + " con este codigo.");
        }
        if (!jpaController.findByNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                + " WHERE " + idQuery + "  " + " o.nombre='" + object.getNombre() + "'").isEmpty()) {
            throw new MessageException("Ya existe un " + CLASS_NAME + " con este nombre.");
        }
        if (object.getId() == null) {
            jpaController.persist(object);
        } else {
            new UsuarioAccionesController().createLog(object);
            jpaController.merge(object);
        }
    }

    private void cargarImagen() throws IOException, Exception {
        JFileChooser filec = new JFileChooser(JGestionUtils.LAST_DIRECTORY_PATH);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Imagenes", "jpg", "bmp", "jpeg", "png");
        filec.setFileFilter(filter);
        filec.addChoosableFileFilter(filter);
        int val = filec.showOpenDialog(null);
        if (val == JFileChooser.APPROVE_OPTION) {
            fotoFile = filec.getSelectedFile();
            JGestionUtils.LAST_DIRECTORY_PATH = fotoFile.getCanonicalPath();
            if (UTIL.isImagenExtension(fotoFile)) {
                UTIL.setImageAsIconLabel(panel.getjLabelFoto(), fotoFile);
            } else {
                abm.showMessage("El archivo debe ser una imagen (bmp, jpg, jpeg, png, tif)", "Extensión de archivo", 0);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="Button">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("exit")) {
                contenedor.dispose();
                if (!contenedor.isModoBuscador()) {
                    contenedor = null;
                }
            } else if (boton.getName().equalsIgnoreCase("aceptar")) {
                try {
                    setEntity();
                    String msj = EL_OBJECT.getId() == null ? "Registrado.." : "Modificado..";
                    checkConstraints(EL_OBJECT);
                    abm.showMessage(msj, CLASS_NAME, 1);
                    //si se está editando.. cierra la ventana post aceptación
                    if (EL_OBJECT.getId() != null) {
                        abm.dispose();
                    }

                    armarQueryContenedor(null);
                    EL_OBJECT = null;
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                abm.dispose();
                panel = null;
                abm = null;
                EL_OBJECT = null;
            } else if (boton.getName().equalsIgnoreCase("marcas")) {
                try {
                    new MarcaController().getABM(abm, true);
                    UTIL.loadComboBox(panel.getCbMarcas(), JGestionUtils.getWrappedMarcas(new MarcaController().findAll()), false);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                }
            }
        } // </editor-fold>
    }

    /**
     * Busca un producto por su código
     *
     * @param codigoProducto
     * @return a instance of Producto or <code>null</code> if Producto.codigo does not exist.
     */
    Producto findProductoByCodigo(String codigoProducto) {
        try {
            return jpaController.findByCodigo(codigoProducto);
        } catch (NoResultException ex) {
            return null;
        }
    }

    Producto getProductoSelected() {
        int selectedRow = contenedor.getjTable1().getSelectedRow();
        if (selectedRow > -1) {
            return DAO.getEntityManager().find(Producto.class, Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
        } else {
            return null;
        }
    }

    /**
     * Actualiza el costo de compra del producto, dependiendo de la valoricacionStock
     *
     * @param producto
     * @param newPrecioUnitario
     * @param cantidad
     * @param valoracionStock 1 = ULTIMA_COMPRA, 2 = ANTIGUO (o sea no cambia nada), 3 = PPP
     */
    void valorizarStock(Producto producto, BigDecimal newPrecioUnitario, int cantidad, int valoracionStock) {
        if (valoracionStock == ULTIMA_COMPRA) {
            producto.setCostoCompra(newPrecioUnitario);

        } else if (valoracionStock == PPP) {
            BigDecimal ppp = (producto.getCostoCompra().multiply(BigDecimal.valueOf(producto.getStockactual())))
                    .add(newPrecioUnitario.multiply(BigDecimal.valueOf(cantidad)));
            int totalStock = producto.getStockactual() + cantidad;
            ppp = ppp.divide(BigDecimal.valueOf(totalStock));
            producto.setCostoCompra(ppp);

        } else if (valoracionStock == ANTIGUO) {
            //respeta el costoCompra anterior, o sea.. no hace nada     
        } else {
            throw new IllegalArgumentException("parameter valoracionStock no válido");
        }

        producto.setUltimaCompra(new Date());
        if (producto.getUpdatePrecioVenta()) {
            producto.setPrecioVenta(producto.getCostoCompra());
        }
        LOG.debug("valorizacionStock(): costoCompra=" + producto.getCostoCompra() + ", precioVenta=" + producto.getPrecioVenta());
        jpaController.merge(producto);
    }

    /**
     * Actualiza el atributo stockActual de la entidad Producto según stock.
     *
     * @param producto al cual se le modificará el {@link Producto#stockactual}
     * @param cantidad si es una Venta, DEBE pasarse un valor NEGATIVO (para restar);
     */
    public void updateStockActual(Producto producto, int cantidad) {
        LOG.debug("updateStockActual (General): " + producto.getNombre() + " = " + producto.getStockactual() + " + " + cantidad);
        producto.setStockactual(producto.getStockactual() + cantidad);
        jpaController.merge(producto);
    }

    void initStockGral(Window owner, Producto p) throws MessageException {
        if (p == null) {
            return;
        }

        JDStockGral jdStockGral = new JDStockGral(owner, true);
        jdStockGral.setLocationRelativeTo(null);
        try {
            UTIL.getDefaultTableModel(
                    jdStockGral.getjTable1(),
                    new String[]{"Sucursal", "Stock", "Último movi.", "Usuario"},
                    new int[]{50, 20, 70, 40});

        } catch (Exception ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        List<Stock> stockList = new StockController().findStocksByProducto(p.getId());
        DefaultTableModel dtm = jdStockGral.getDtm();
        for (Stock stock : stockList) {
            dtm.addRow(new Object[]{
                stock.getSucursal().getNombre(),
                stock.getStockSucu(),
                UTIL.TIMESTAMP_FORMAT.format(stock.getFechaCarga()),
                stock.getUsuario().getNick()
            });
        }
        jdStockGral.setVisible(true);
    }

    private void eliminarProducto() throws MessageException, NonexistentEntityException, DatabaseErrorException {
        if (EL_OBJECT == null) {
            if (contenedor != null && contenedor.getjTable1().getSelectedRow() != -1) {
                EL_OBJECT = jpaController.find((Integer) contenedor.getSelectedValue(0));
            }
            if (EL_OBJECT == null) {
                throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
            }
        }
        jpaController.remove(EL_OBJECT);
        EL_OBJECT = null;
        armarQueryContenedor(null);
    }

    private Producto getSelectedFromContenedor() {
        Integer selectedRow = contenedor.getjTable1().getSelectedRow();
        if (selectedRow > -1) {
            return jpaController.find(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
        } else {
            return null;
        }
    }

    private void doDynamicReportProductos() throws Exception {
        if (jpaController.count() == 0) {
            throw new MessageException("No existen " + CLASS_NAME + "s para imprimir un listado.");
        }
        final PanelProductoReporteOptions p = new PanelProductoReporteOptions();
        UTIL.loadComboBox(p.getCbListaPrecio(), JGestionUtils.getWrappedListaPrecios(new ListaPreciosController().findAll()), false);
        final JDABM jd = new JDABM(contenedor, "Reporte de Productos", true, p);
        jd.getbAceptar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    Reportes r = new Reportes(null, true);
                    r.showWaitingDialog();
                    // QUE HIJO DE MIL PUTA!! .. list de mierda,, no se limpia
                    List<Producto> list = new ProductoJpaController().findByBienDeCambio(true);
                    DynamicReportBuilder drb = new DynamicReportBuilder();
                    drb.setRightMargin(20);
                    Style currencyStyle = new Style();
                    currencyStyle.setFont(Font.ARIAL_MEDIUM);
                    currencyStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
                    Style textStyle = new Style();
                    textStyle.setFont(Font.ARIAL_MEDIUM);
                    drb
                            .addColumn(ColumnBuilder.getNew().setColumnProperty("codigo", String.class).setTitle("Código").setWidth(60).setStyle(textStyle).setFixedWidth(true).build())
                            .addColumn(ColumnBuilder.getNew().setColumnProperty("nombre", String.class).setTitle("Producto").setWidth(200).setStyle(textStyle).build())
                            .addColumn(ColumnBuilder.getNew().setColumnProperty("marca.nombre", String.class).setTitle("Marca").setWidth(80).setStyle(textStyle).build());
                    if (p.getCheckStock().isSelected()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("stockactual", Integer.class.getName()).setTitle("Stock").setWidth(40).setStyle(currencyStyle).setFixedWidth(true).build());
                    }
                    if (p.getCheckCostoCompra().isSelected()) {
                        drb.addColumn(ColumnBuilder.getNew().setColumnProperty("costoCompra", BigDecimal.class.getName()).setTitle("Costo U.").setWidth(80)
                                .setStyle(currencyStyle)
                                .setPattern("¤ #,##0.0000")
                                .setFixedWidth(true).build());
                    }
                    ListaPrecios lp = null;
                    if (p.getCheckPrecioVenta().isSelected()) {
                        drb.addColumn(ColumnBuilder.getNew()
                                .setColumnProperty("precioVenta", BigDecimal.class.getName()).setTitle("Precio U.").setWidth(80)
                                .setStyle(currencyStyle)
                                .setPattern("¤ #,##0.0000")
                                .setFixedWidth(true).build());
                        lp = ((EntityWrapper<ListaPrecios>) p.getCbListaPrecio().getSelectedItem()).getEntity();
                        Double margen = (lp.getMargen() / 100) + 1;
                        for (Producto producto : list) {
                            BigDecimal precioVenta = producto.getPrecioVenta();
                            producto.setPrecioVenta(precioVenta.multiply(BigDecimal.valueOf(margen)));
                        }
                    }
                    drb.setTitle("Listado de Productos")
                            .setSubtitle(lp == null ? "" : "Según Lista Precios: " + lp.getNombre() + ", " + UTIL.TIMESTAMP_FORMAT.format(new Date()))
                            .setPrintBackgroundOnOddRows(true)
                            .setUseFullPageWidth(true);
                    DynamicReport dr = drb.build();
                    JRDataSource ds = new JRBeanCollectionDataSource(list);
                    JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);
                    r.setjPrint(jp);
                    r.viewReport();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "algo salió mal");
                    LOG.error("Creando reporte producto", ex);
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

    public void initMovimientoProducto(JFrame frame, boolean modal) {
        panelito = new PanelBuscadorMovimientosPro();
        UTIL.loadComboBox(panelito.getCbMarcas(), JGestionUtils.getWrappedMarcas(new MarcaController().findAll()), true);
        UTIL.loadComboBox(panelito.getCbRubros(), JGestionUtils.getWrappedRubros(new RubroController().findRubros()), true);
        UTIL.loadComboBox(panelito.getCbSubRubros(), JGestionUtils.getWrappedRubros(new RubroController().findRubros()), true);
        UTIL.loadComboBox(panelito.getCbSucursales(), new UsuarioHelper().getWrappedSucursales(), true);
        buscador = new JDBuscador(frame, "Movimientos de productos", modal, panelito);
        buscador.getPanelInferior().setVisible(true);
        buscador.addResumeItem("Total", new JTextField(8));
        buscador.agrandar(200, 0);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"RAZÓN", "CÓDIGO", "NOMBRE", "MARCA", "CANTIDAD", "PRECIO U.", "LETRA", "NÚMERO", "INTERNO", "FECHA (HORA)", "RUBRO/S.RUB", "SUCURSAL"},
                new int[]{25, 50, 150, 30, 20, 10, 40, 40, 20, 60, 30, 30},
                new Class<?>[]{null, null, null, null, null, null, null, null, null, null, null, null});
        TableColumnModel tm = buscador.getjTable1().getColumnModel();
        tm.getColumn(5).setCellRenderer(NumberRenderer.getCurrencyRenderer(4));
        buscador.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = armarQueryMovimientosProductos();
                    cargarTablaMovimientosProductos(query);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage());
                    LOG.error("Error en cargarTablaMovimientosProductos()", ex);
                }

            }
        });

        buscador.getBtnImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    buscador.getBtnImprimir().setEnabled(false);
                    String query = armarQueryMovimientosProductos();
                    cargarTablaMovimientosProductos(query);
                    Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_MovimientosProductos.jasper", "Movimientos Productos");
                    List<GenericBeanCollection> data = new ArrayList<GenericBeanCollection>(buscador.getjTable1().getRowCount());
                    DefaultTableModel dtm = buscador.getDtm();
                    for (int row = 0; row < dtm.getRowCount(); row++) {
                        data.add(new GenericBeanCollection(
                                dtm.getValueAt(row, 0), dtm.getValueAt(row, 1), dtm.getValueAt(row, 2), dtm.getValueAt(row, 3),
                                dtm.getValueAt(row, 4), dtm.getValueAt(row, 5), dtm.getValueAt(row, 6), dtm.getValueAt(row, 7),
                                dtm.getValueAt(row, 8), dtm.getValueAt(row, 10), dtm.getValueAt(row, 11), null));
                    }
//                    r.addCurrent_User();
                    r.setDataSource(data);
                    r.addMembreteParameter();
                    r.addConnection();
                    r.viewReport();
                } catch (JRException ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage());
                } catch (MissingReportException ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage());
                } finally {
                    buscador.getBtnImprimir().setEnabled(true);
                }
            }
        });
        buscador.getBtnToExcel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "No implementado aún");
            }
        });
        buscador.setVisible(true);

    }

    private void cargarTablaMovimientosProductos(String query) {
        UTIL.limpiarDtm(buscador.getjTable1());
        List<?> l = DAO.getEntityManager().createNativeQuery(query).getResultList();
        BigDecimal total = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        for (Object object : l) {
            Object[] o = (Object[]) object;
            if ((Boolean) o[10]) { // es decir.. si factura.anulada == true
                o[0] = "ANULADA " + String.valueOf(o[0]);
                o[4] = -Integer.valueOf(o[4].toString());
            }
            BigDecimal precioUnitario = (BigDecimal) o[5];
            total = total.add(precioUnitario);
            UTIL.getDtm(buscador.getjTable1()).addRow(new Object[]{
                o[0],
                o[1], //código
                o[2],
                o[3], //marca
                o[4], //cantidad
                precioUnitario,
                o[7],
                o[8].toString().replaceAll(" ", ""),
                o[9],
                UTIL.TIMESTAMP_FORMAT.format(((Date) o[6])),
                o[11],
                o[12]
            });
        }
        buscador.getResumeItems().get("Total").setText(UTIL.DECIMAL_FORMAT.format(total));
    }

    private String armarQueryMovimientosProductos() {
        String mainQuery = "SELECT * FROM ( ";
        String ingresoQuery = "SELECT 'COMPRA'::character varying(6) AS razon, p.codigo, p.nombre, marca.nombre AS marca, d.cantidad, d.precio_unitario, f.fechaalta, f.tipo, to_char(f.numero, '0000-00000000') as numero, f.movimiento_interno as movimiento, f.anulada, rubro.nombre AS rubro, sucursal.nombre as sucursal"
                + " FROM producto p"
                + " JOIN detalle_compra d ON p.id = d.producto"
                + " JOIN factura_compra f ON f.id = d.factura"
                + " JOIN marca ON p.marca = marca.id"
                + " JOIN rubro ON p.rubro = rubro.idrubro"
                + " JOIN sucursal ON f.sucursal = sucursal.id"
                + " WHERE p.codigo IS NOT NULL";
        String egresoQuery = "SELECT 'VENTA'::character varying(6) AS razon, p.codigo, p.nombre, marca.nombre AS marca, - d.cantidad, d.precio_unitario, f.fechaalta, f.tipo, to_char(sucursal.puntoventa, '0000') || '-' || to_char(f.numero, '00000000') as numero, f.movimiento_interno AS movimiento, f.anulada, rubro.nombre AS rubro, sucursal.nombre as sucursal"
                + " FROM producto p"
                + " JOIN detalle_venta d ON p.id = d.producto"
                + " JOIN factura_venta f ON f.id = d.factura"
                + " JOIN marca ON p.marca = marca.id"
                + " JOIN rubro ON p.rubro = rubro.idrubro"
                + " JOIN sucursal ON f.sucursal = sucursal.id"
                + " WHERE p.codigo IS NOT NULL ";
        int ingreso_egreso = panelito.getCbIngresoEgreso().getSelectedIndex();
        if (ingreso_egreso == 0) {
            ingresoQuery = concatQuery(ingresoQuery);
            egresoQuery = concatQuery(egresoQuery);
            mainQuery += ingresoQuery + " UNION (" + egresoQuery + ")";
        } else if (ingreso_egreso == 1) {
            ingresoQuery = concatQuery(ingresoQuery);
            mainQuery += ingresoQuery;
        } else {
            egresoQuery = concatQuery(egresoQuery);
            mainQuery += egresoQuery;
        }
        mainQuery += ") as t ";
        mainQuery += " ORDER BY fechaalta";
        Logger.getLogger(Producto.class).trace(mainQuery);

        return mainQuery;
    }

    @SuppressWarnings("unchecked")
    private String concatQuery(String query) {
        StringBuilder sb = new StringBuilder(query);
        if (panelito.getTfCodigo().getText().trim().length() > 0) {
            sb.append(" AND p.codigo = '").append(panelito.getTfCodigo().getText().trim()).append("'");
        }
        if (panelito.getTfNombre().getText().trim().length() > 0) {
            sb.append(" AND p.nombre ILIKE '").append(panelito.getTfNombre().getText().trim()).append("%'");
        }
        if (panelito.getCbMarcas().getSelectedIndex() > 0) {
            sb.append(" AND p.marca = ").append(((EntityWrapper<Marca>) panelito.getCbMarcas().getSelectedItem()).getId());
        }
        if (panelito.getCbRubros().getSelectedIndex() > 0) {
            sb.append(" AND p.rubro = ").append(((EntityWrapper<Rubro>) panelito.getCbRubros().getSelectedItem()).getId());
        }
        if (panelito.getCbSubRubros().getSelectedIndex() > 0) {
            sb.append(" AND p.subrubro = ").append(((EntityWrapper<Rubro>) panelito.getCbSubRubros().getSelectedItem()).getId());
        }
        if (panelito.getCbSucursales().getSelectedIndex() > 0) {
            sb.append(" AND sucursal.id = ").append(((EntityWrapper<Sucursal>) panelito.getCbSucursales().getSelectedItem()).getId());
        }
        if (panelito.getDcDesde().getDate() != null) {
            sb.append(" AND f.fechaalta >= '").append(panelito.getDcDesde().getDate()).append("'");
        }
        if (panelito.getDcHasta().getDate() != null) {
            sb.append(" AND f.fechaalta <= '").append(panelito.getDcHasta().getDate()).append("'");
        }
        return sb.toString();
    }

    /**
     * Retrieve a lightweigth List of Product (id, codigo, nombre, remunerativo)
     *
     * @param bienDeCambio
     * @return a List of {@link Producto} or <tt>null</tt> if something goes wrong.
     */
    public List<Producto> findProductoToCombo(Boolean bienDeCambio) {
        try {
            List<Producto> resultList = jpaController.findByNativeQuery(
                    "SELECT p.id, p.codigo, p.nombre "
                    + " FROM Producto p"
                    + (bienDeCambio != null ? " WHERE  p.bien_De_Cambio=" + bienDeCambio : "")
                    + " ORDER BY p.nombre");
            return resultList;
        } catch (Exception ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    public List<EntityWrapper<Producto>> findWrappedProductoToCombo(Boolean bienDeCambio) {
        List<Producto> ff = findProductoToCombo(bienDeCambio);
        List<EntityWrapper<Producto>> l = new ArrayList<EntityWrapper<Producto>>(ff.size());
        for (Producto producto : ff) {
            l.add(new EntityWrapper<Producto>(producto, producto.getId(), producto.getNombre()));
        }
        return l;
    }

    public void initListadoProducto(JFrame owner) {
        panelProductoListados = new PanelProductoListados();
        buscador = new JDBuscador(owner, "Productos - Listados", false, panelProductoListados);
        UTIL.loadComboBox(panelProductoListados.getCbMarcas(), JGestionUtils.getWrappedMarcas(new MarcaController().findAll()), "<Todas>");
        UTIL.loadComboBox(panelProductoListados.getCbRubros(), new RubroController().findRubros(), "<Todos>");
        UTIL.loadComboBox(panelProductoListados.getCbSubRubros(), new RubroController().findRubros(), "<Todos>");
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"NOMBRE", "CÓDIGO", "MARCA", "RUBRO", "SUB RUBRO"},
                new int[]{250, 60, 60, 50, 50});
        buscador.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQueryProductosListado(false);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage());
                }
            }
        });
        buscador.getBtnImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    armarQueryProductosListado(true);
                } catch (Exception ex) {
                    Logger.getLogger(ProductoController.class).error(ex);
                    JOptionPane.showMessageDialog(buscador, ex.getMessage());
                }
            }
        });
        buscador.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private void armarQueryProductosListado(boolean imprimirReport) throws DatabaseErrorException, Exception {
        String query = "SELECT p.*, marca.nombre as marca_nombre, r.nombre as rubro_nombre, sr.nombre as subrubro_nombre "
                + " FROM producto p"
                + " JOIN marca ON p.marca = marca.id"
                + " JOIN rubro r ON p.rubro = r.idrubro"
                + " LEFT JOIN rubro sr ON p.subrubro = sr.idrubro"
                + " WHERE p.id IS NOT NULL";

        if (panelProductoListados.getCbMarcas().getSelectedIndex() > 0) {
            query += " AND p.marca = " + ((EntityWrapper<?>) panelProductoListados.getCbMarcas().getSelectedItem()).getId();
        }

        if (panelProductoListados.getCbRubros().getSelectedIndex() > 0) {
            query += " AND p.rubro = " + ((Rubro) panelProductoListados.getCbRubros().getSelectedItem()).getId();
        }

        if (panelProductoListados.getCbSubRubros().getSelectedIndex() > 0) {
            query += " AND p.subrubro = " + ((Rubro) panelProductoListados.getCbSubRubros().getSelectedItem()).getId();
        }

        if (panelProductoListados.getCheckOrdenarPorFechaCreacion().isSelected()) {
            query += " ORDER BY p.fecha_alta ASC";
        } else {
            query += " ORDER BY p.nombre";

        }
        cargarTablaProductosListado((List<Producto>) DAO.getNativeQueryResultList(query, Producto.class));

        if (imprimirReport) {
            doReportProductosListado(query);
        }
    }

    private void cargarTablaProductosListado(List<Producto> listado) {
        UTIL.limpiarDtm(buscador.getjTable1());
        for (Producto producto : listado) {
            buscador.getDtm().addRow(new Object[]{
                producto.getNombre(),
                producto.getCodigo(),
                producto.getMarca().getNombre(),
                producto.getRubro().getNombre(),
                (producto.getSubrubro() != null ? producto.getSubrubro() : null)
            });
        }
    }

    private void doReportProductosListado(String query) throws Exception {
//      if (panelProductoListados.getCheckParaJurado().isSelected()) {
//         Reportes r = new Reportes("evento_parajurado.jasper", "Lista para jurado");
//         r.addCurrent_User();
//         r.addParameter("QUERY", query);
//         r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
//         r.viewReport();
//      } else {
        Reportes r = new Reportes("JGestion_ProductosListado.jasper", "Productos - Listado");
        r.addCurrent_User();
        r.addParameter("QUERY", query);
        r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
        r.viewReport();
//      }
    }

    @SuppressWarnings("unchecked")
    List<Producto> findProductoByIva(Iva iva) {
        return DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.iva.id=" + iva.getId()).getResultList();
    }
}

package controller;

import com.toedter.calendar.JDateChooser;
import controller.exceptions.MessageException;
import controller.exceptions.MissingReportException;
import entity.Caja;
import entity.Dominio;
import entity.FacturaCompra;
import entity.FacturaCompra_;
import entity.Sucursal;
import generics.GenericBeanCollection;
import gui.JDABM;
import gui.JDMiniABM;
import gui.generics.GroupLayoutPanelBuilder;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.RollbackException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jpa.controller.DominioJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Logger;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.postgresql.util.PSQLException;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class DominioController implements ActionListener {

    private static final Logger LOG = Logger.getLogger(DominioController.class);
    private final DominioJpaController jpaController = new DominioJpaController();
    private JDMiniABM abm;
    private Dominio entity;

    public void getABM(Window owner, boolean modal) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PRODUCTOS);
        abm = new JDMiniABM(owner, modal);
        abm.setLocationRelativeTo(owner);
        initABM();
    }

    private void initABM() {
        abm.hideBtnLock();
        abm.hideFieldExtra();
        abm.hideFieldCodigo();
        abm.setTitle("ABM - " + jpaController.getEntityClass().getSimpleName() + "s");
        abm.setVisibleTaInformacion(false);
        UTIL.getDefaultTableModel(abm.getjTable1(), new String[]{"Object", "Nombre"});
        UTIL.hideColumnTable(abm.getjTable1(), 0);
        abm.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Integer selectedRow = abm.getjTable1().getSelectedRow();
                if (selectedRow > -1) {
                    entity = (Dominio) UTIL.getSelectedValue(abm.getjTable1(), 0);
                }
                if (entity != null) {
                    abm.setTfNombre(entity.getNombre());
                    abm.tfNombreRequestFocus();
                }
            }
        });
        cargarDTM();
        abm.setListeners(this);
        abm.setVisible(true);
    }

    private void cargarDTM() {
        List<Dominio> findAll = jpaController.findAll();
        DefaultTableModel dtm = (DefaultTableModel) abm.getjTable1().getModel();
        dtm.setRowCount(0);
        for (Dominio o : findAll) {
            dtm.addRow(new Object[]{
                o,
                o.getNombre()
            });
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            //<editor-fold defaultstate="collapsed" desc="abm Actions">
            if (abm != null) {
                if (boton.equals(abm.getbNuevo())) {
                    entity = null;
                    abm.clearPanelFields();
                } else if (boton.equals(abm.getbEliminar())) {
                    try {
                        if (entity == null) {
                            throw new MessageException("No ha seleccionado ningún registro");
                        }
                        eliminar(entity);
                        entity = null;
                        abm.clearPanelFields();
                        cargarDTM();
                        JOptionPane.showMessageDialog(abm, "Eliminado");
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (boton.equals(abm.getbCancelar())) {
                    entity = null;
                    abm.clearPanelFields();
                } else if (boton.equals(abm.getbAceptar())) {
                    try {
                        setEntity();
                        save(entity);
                        entity = null;
                        abm.clearPanelFields();
                        cargarDTM();
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(abm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }//</editor-fold>
        }// </editor-fold>
    }

    private void setEntity() throws MessageException {
        if (entity == null) {
            entity = new Dominio();
        }
        String nombre = abm.getTfNombre().trim().toUpperCase();
        if (nombre.isEmpty()) {
            throw new MessageException("Nombre no válido");
        }
        if (nombre.length() > 50) {
            throw new MessageException("El nombre no puede superar los 50 caracteres");
        }
        entity.setNombre(nombre);
    }

    private void save(Dominio o) throws MessageException {
        if (!jpaController.findAll("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o WHERE "
                + " o.nombre ='" + o.getNombre() + "'"
                + (o.getId() != null ? " AND o.id <>" + o.getId() : "")).isEmpty()) {
            throw new MessageException("Ya existe un registro con este nombre");
        }
        if (o.getId() == null) {
            jpaController.create(o);
        } else {
            jpaController.merge(o);
        }
    }

    private void eliminar(Dominio o) throws MessageException {
        try {
            jpaController.remove(o);
        } catch (RollbackException ex) {
            if (ex.getCause() instanceof DatabaseException) {
                PSQLException ps = (PSQLException) ex.getCause().getCause();
                if (ps.getMessage().contains("viola la llave foránea") || ps.getMessage().contains("violates foreign key constraint")) {
                    throw new MessageException("No se puede eliminar porque existen otros registros que están relacionados a este");
                }
            }
            throw ex;
        } finally {
            jpaController.closeEntityManager();
        }
    }

    public void displayInforme(Window owner) {
        final JDateChooser dcDesde = new JDateChooser();
        final JDateChooser dcHasta = new JDateChooser();
//        dcHasta.setVisible(false);
        JLabel l = new JLabel("Fecha Hasta");
        final JComboBox cbDominios = new JComboBox();
        UTIL.loadComboBox(cbDominios, JGestionUtils.getWrappedDominios(jpaController.findAll()), true);
        final GroupLayoutPanelBuilder glpb = new GroupLayoutPanelBuilder();
//        glpb.getInfoLabel().setText("Todos los campos son necesarios");
        glpb.getInfoLabel().setForeground(Color.BLUE);
        glpb.addFormItem(new JLabel("Fecha Desde"), dcDesde);
        glpb.addFormItem(l, dcHasta);
        glpb.addFormItem(new JLabel("Dominios"), cbDominios);
        JPanel panel = glpb.build();
        final JDABM jdabm = new JDABM(owner, "Informe de Facturas Compra por Dominio", true, panel);
        jdabm.setLocationRelativeTo(owner);
        jdabm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //<editor-fold defaultstate="collapsed" desc="query">
                    StringBuilder query = new StringBuilder("SELECT o.* FROM factura_compra o"
                            + " WHERE o.anulada = FALSE");
                    if (cbDominios.getSelectedIndex() > 0) {
                        query.append(" AND o.dominio_id=" + ((ComboBoxWrapper<Dominio>) (cbDominios.getSelectedItem())).getId());
                    } else {
                        query.append(" AND o.dominio_id is not null");
                    }
                    SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy/MM/dd");
                    if (dcDesde.getDate() != null) {
                        query.append(" AND o.fecha_compra >= '").append(yyyyMMdd.format(dcDesde.getDate())).append("'");
                    } else {
                        throw new MessageException("Fecha Desde no especificada");
                    }
                    if (dcHasta.getDate() != null) {
                        query.append(" AND o.fecha_compra <= '").append(yyyyMMdd.format(dcHasta.getDate())).append("'");
                    } else {
                        throw new MessageException("Fecha Hasta no especificada");
                    }
                    UsuarioHelper usuarioHelper = new UsuarioHelper();
                    query.append(" AND (");
                    Iterator<Caja> iterator = usuarioHelper.getCajas(Boolean.TRUE).iterator();
                    while (iterator.hasNext()) {
                        Caja caja = iterator.next();
                        query.append("o.caja=").append(caja.getId());
                        if (iterator.hasNext()) {
                            query.append(" OR ");
                        }
                    }
                    query.append(")");
                    List<Sucursal> sucursales = new UsuarioHelper().getSucursales();
                    query.append(" AND (");
                    for (int i = 0; i < sucursales.size(); i++) {
                        query.append(" o.sucursal=").append(sucursales.get(i).getId());
                        if ((i + 1) < sucursales.size()) {
                            query.append(" OR ");
                        }
                    }
                    query.append(")");
                    query.append(" ORDER BY o.id");
                    @SuppressWarnings("unchecked")
                    List<FacturaCompra> l = DAO.getEntityManager().createNativeQuery(query.toString(), FacturaCompra.class).getResultList();
                    DefaultTableModel dtm = new DefaultTableModel(new String[]{"facturaID", "Nº factura", "Dominio", "Proveedor", "Importe", "Fecha", "Caja", "Sucursal", "Unid. Neg.", "Cuenta", "Sub Cuenta"}, 0);
                    for (FacturaCompra facturaCompra : l) {
                        dtm.addRow(new Object[]{
                            facturaCompra.getId(),
                            JGestionUtils.getNumeracion(facturaCompra),
                            facturaCompra.getDominio().getNombre(),
                            facturaCompra.getProveedor().getNombre(),
                            BigDecimal.valueOf(facturaCompra.getImporte()),
                            UTIL.DATE_FORMAT.format(facturaCompra.getFechaCompra()),
                            facturaCompra.getSucursal().getNombre(),
                            facturaCompra.getCaja(),
                            facturaCompra.getUsuario(),
                            UTIL.DATE_FORMAT.format(facturaCompra.getFechaalta()) + " (" + UTIL.TIME_FORMAT.format(facturaCompra.getFechaalta()) + ")"
                        });
                    }
//</editor-fold>
                    doReportFacturasPorDominio(dtm);
                } catch (JRException | MissingReportException | MessageException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                } catch (Exception ex) {
                    LOG.error(ex, ex);
                }
            }

        }
                );
        jdabm.setVisible(true);
    }

    private void doReportFacturasPorDominio(DefaultTableModel dtm) throws MissingReportException, JRException {
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
                    dtm.getValueAt(row, 9),
                    null, null, null));
        }
        Reportes r = new Reportes("JGestion_ListadoFacturasCompra.jasper", "Listado Facturas Compra");
        r.setDataSource(data);
        r.addParameter("TITLE_PAGE_HEADER", "Compras por Dominio");
        r.addParameter("IS_DOMINIO_REPORT", true);
        r.addConnection();
        r.viewReport();
    }
}

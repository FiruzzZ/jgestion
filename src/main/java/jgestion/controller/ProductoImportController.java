package jgestion.controller;

import generics.WaitingDialog;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Iva;
import jgestion.entity.Marca;
import jgestion.entity.Producto;
import jgestion.entity.Rubro;
import jgestion.entity.Unidadmedida;
import jgestion.gui.JDImportarProductos;
import jgestion.jpa.controller.IvaJpaController;
import jgestion.jpa.controller.MarcaJpaController;
import jgestion.jpa.controller.ProductoJpaController;
import jgestion.jpa.controller.RubroJpaController;
import jgestion.jpa.controller.UnidadmedidaJpaController;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class ProductoImportController {

    private File importProductXLS;
    private final ProductoJpaController jpaController = new ProductoJpaController();
    private static final Logger LOG = LogManager.getLogger();

    public ProductoImportController() {
    }

    public void displayImportacion(Window owner) {
        final int estado_columna_idx = 6;
        JDImportarProductos ui = new JDImportarProductos(owner, false);
        HighlightPredicate noImportHighlight = (Component renderer, org.jdesktop.swingx.decorator.ComponentAdapter adapter) -> {
            int rModel = adapter.convertRowIndexToModel(adapter.row);
            return !"ok".equals(adapter.getValueAt(rModel, estado_columna_idx));
        };
        ui.getjXTable1().addHighlighter(new ColorHighlighter(noImportHighlight, null, Color.RED));
        UTIL.loadComboBox(ui.getCbRubro(), JGestionUtils.getWrappedRubros(new RubroJpaController().findAll()), false);
        ui.getBtnSearchFile().addActionListener(evt -> {
            JFileChooser fileChooser = new JFileChooser(JGestionUtils.LAST_DIRECTORY_PATH);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel file", "xls", "xlsx"));
            int stateFileChoosed = fileChooser.showSaveDialog(owner);
            if (stateFileChoosed != JFileChooser.APPROVE_OPTION) {
                return;
            }
            importProductXLS = fileChooser.getSelectedFile();
            JGestionUtils.LAST_DIRECTORY_PATH = importProductXLS.getAbsolutePath();
            ui.getTfPathFile().setText(importProductXLS.getName());
        });
        ui.getBtnReadFile().addActionListener(evt -> {
            DefaultTableModel dtm = (DefaultTableModel) ui.getjXTable1().getModel();
            dtm.setRowCount(0);
            List<Object[]> data = readExcel(importProductXLS);
            data.forEach(d -> dtm.addRow(d));
            ui.getTfCantidadImportar().setText(data.stream().filter(o -> o[6].equals("ok")).count() + "");
        });
        ui.getBtnImportar().addActionListener(evt -> {
            initImport(ui, estado_columna_idx);
        });
        ui.setVisible(true);
    }

    public void initImport(JDImportarProductos ui, final int estado_columna_idx) {
        WaitingDialog wd = new WaitingDialog(ui, "Importando", true, null);
        wd.addTask(() -> {
            try {
                DefaultTableModel dtm = (DefaultTableModel) ui.getjXTable1().getModel();
                if (dtm.getRowCount() == 0 || ui.getTfCantidadImportar().getText().equals("0")) {
                    throw new MessageException("Nada que importar..");
                }
                boolean actualizaPV = ui.getCheckActualizarPrecioVenta().isSelected();
                final Rubro defaultRubro = (Rubro) UTIL.getEntityWrapped(ui.getCbRubro()).getEntity();
                RubroJpaController rubroDAO = new RubroJpaController();
                IvaJpaController ivaDAO = new IvaJpaController();
                final Iva defaultIVA = ivaDAO.findByValue(0);
                Unidadmedida unitario = new UnidadmedidaJpaController().findUnitario();
                for (int row = 0; row < dtm.getRowCount(); row++) {
                    if (!dtm.getValueAt(row, estado_columna_idx).equals("ok")) {
                        continue;
                    }
                    String codigo = (String) dtm.getValueAt(row, 0);
                    String nombre = (String) dtm.getValueAt(row, 1);
                    String marcaStr = (String) dtm.getValueAt(row, 2);
                    Marca marca = new MarcaJpaController().findByNombre(marcaStr);
                    if (marca == null) {
                        marca = new Marca(null, marcaStr);
                        new MarcaJpaController().persist(marca);
                    }
                    String rubroStr = (String) dtm.getValueAt(row, 3);
                    Rubro rubro;
                    if (rubroStr == null) {
                        rubro = defaultRubro;
                    } else {
                        rubro = rubroDAO.findByNombre(rubroStr);
                        if (rubro == null) {
                            rubro = new Rubro(null, rubroStr, (short) 1);
                            rubroDAO.persist(rubro);
                        }
                    }
                    Iva iva = defaultIVA;
                    BigDecimal ivaVal = (BigDecimal) dtm.getValueAt(row, 4);
                    if (ivaVal != null) {
                        iva = ivaDAO.findByValue(ivaVal.floatValue());
                        if (iva == null) {
                            iva = defaultIVA;
                        }
                    }
                    BigDecimal precio = (BigDecimal) dtm.getValueAt(row, 5);
                    Producto producto = new Producto();
                    producto.setCodigo(codigo);
                    producto.setNombre(nombre);
                    producto.setRubro(rubro);
                    producto.setMarca(marca);
                    producto.setIva(iva);
                    producto.setPrecioVenta(precio);
                    producto.setUpdatePrecioVenta(actualizaPV);
                    producto.setCostoCompra(BigDecimal.ZERO);
                    producto.setStockminimo(0);
                    producto.setStockactual(0);
                    producto.setStockmaximo(0);
                    producto.setBienDeCambio(true);
                    producto.setRemunerativo(true);
                    producto.setUnidadmedida(unitario);
                    jpaController.persist(producto);
                    wd.appendMessage("Importando " + (row + 1) + "/" + ui.getTfCantidadImportar().getText(), false);
                }
                dtm.setRowCount(0);
                ui.getTfCantidadImportar().setText(null);
                EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(null, wd.getMessageToKeep(), "Resumen", JOptionPane.INFORMATION_MESSAGE));
            } catch (MessageException ex) {
                ex.displayMessage(null);
            } catch (Exception ex) {
                new MessageException("Algo salió mal", ex.getMessage()).displayMessage(wd);
            } finally {
                wd.dispose();
            }
        });
        wd.setVisible(true);
    }

    private List<Object[]> readExcel(File file) {
        String az09 = "\\w+";
        String az09plus = "[\\p{IsLatin}\\d]+[\\p{IsLatin}\\d\\-\\.\\s\\/°]+[\\p{IsLatin}\\d]+";
        List<Object[]> data = null;
        try {
            Workbook workbook;
            try {
                try (FileInputStream fis = new FileInputStream(file)) {
                    workbook = new HSSFWorkbook(fis);
                }
            } catch (IOException | OfficeXmlFileException ex) {
                //Probando alternativa XSSF ( *.xlsx)
                try (FileInputStream ff = new FileInputStream(file)) {
                    workbook = new XSSFWorkbook(ff);
                }
            }
            Sheet sheet = workbook.getSheetAt(0);
            data = new ArrayList<>(sheet.getLastRowNum());
            for (Iterator<Row> iterator = sheet.iterator(); iterator.hasNext();) {
                Row row = iterator.next();
                String codigo = null;
                String nombre = null;
                String marca = null;
                String rubro = null;
                BigDecimal iva = null;
                BigDecimal precioVenta = BigDecimal.ZERO;
                Cell cellCodigo = row.getCell(0);
                String estado = null;
                if (cellCodigo == null) {
                    estado = ("columna código no válida (" + row.getRowNum() + ")");
                }
                Cell cellNombre = row.getCell(1);
                if (cellNombre == null) {
                    if (estado == null) {
                        estado = ("columna nombre no válida (" + row.getRowNum() + ")");
                    }
                }
                try {
                    if (cellCodigo.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        codigo = BigDecimal.valueOf(cellCodigo.getNumericCellValue()).longValue() + "";
                    } else {
                        codigo = StringUtils.trimToNull(cellCodigo.getStringCellValue());
                    }
                    if (codigo == null || codigo.length() > 15) {
                        if (estado == null) {
                            estado = ("columna código no válida (" + row.getRowNum() + ")");
                        }
                    } else if (!UTIL.VALIDAR_REGEX(az09, codigo)) {
                        if (estado == null) {
                            estado = ("formato de código no válidos (" + row.getRowNum() + ")");
                        }
                    } else {
                        Producto old = jpaController.findByCodigo(codigo);
                        if (old != null) {
                            //le da tiempo a cargar el nombre para mostrar este error
                            if (estado == null) {
                                estado = ("código ya registrado (" + row.getRowNum() + ")");
                            }
                        }
                        for (Object[] o : data) {
                            if (codigo.equals(o[0])) {
                                if (estado == null) {
                                    estado = "código repetido (" + row.getRowNum() + ")";
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    if (estado == null) {
                        estado = ("Formato de código no válido (" + row.getRowNum() + ")");
                    }
                }
                try {
                    nombre = StringUtils.trimToNull(cellNombre.getStringCellValue());
                } catch (Exception ex) {
                    LOG.error("importación producto, columna nombre", ex);
                    if (estado == null) {
                        estado = ("columna nombre no válida (" + row.getRowNum() + ")");
                    }
                }
                if (nombre == null) {
                    if (estado == null) {
                        estado = ("nombre no válido (" + row.getRowNum() + ")");
                    }
                } else if (nombre.length() > 150) {
                    if (estado == null) {
                        estado = ("nombre muy largo  (" + row.getRowNum() + ")");
                    }
                } else if (!UTIL.VALIDAR_REGEX(az09plus, nombre)) {
                    if (estado == null) {
                        estado = ("formato de nombre no válido (" + row.getRowNum() + ") ");
                    }
                }

                Cell cellMarca = row.getCell(2);
                if (cellMarca == null) {
                    if (estado == null) {
                        estado = "marca no válida (" + row.getRowNum() + ")";
                    }
                } else {
                    try {
                        marca = StringUtils.trimToNull(cellMarca.getStringCellValue());
                        if (marca == null || marca.length() > 40) {
                            if (estado == null) {
                                estado = "marca no válida (" + row.getRowNum() + ")";
                            }
                        }
                    } catch (Exception ex) {
                        if (estado == null) {
                            estado = "marca no válida (" + row.getRowNum() + ")";
                        }
                    }
                }
                Cell cellRubro = row.getCell(3);
                if (cellRubro != null) {
                    try {
                        rubro = StringUtils.trimToNull(cellRubro.getStringCellValue());
                        if (rubro == null || rubro.length() > 50) {
                            if (estado == null) {
                                estado = ("rubro no válida (" + row.getRowNum() + ")");
                            }
                        }
                    } catch (Exception ex) {
                        if (estado == null) {
                            estado = ("rubro no válida (" + row.getRowNum() + ")");
                        }
                    }
                }
                Cell cellIVA = row.getCell(4);
                if (cellIVA != null) {
                    if (cellIVA.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        iva = BigDecimal.valueOf(cellIVA.getNumericCellValue());
                    } else if (cellIVA.getCellType() == Cell.CELL_TYPE_STRING) {
                        if (StringUtils.trimToNull(cellIVA.getStringCellValue()) != null) {
                            try {
                                iva = new BigDecimal(cellIVA.getStringCellValue().replaceAll(",", "."));
                            } catch (Exception e) {
                                if (estado == null) {
                                    estado = ("Iva no válido (" + row.getRowNum() + ")");
                                }
                            }
                        }
                    }
                    if (iva != null) {
                        if (iva.intValue() < 0 || iva.intValue() > 100) {
                            if (estado == null) {
                                estado = ("Iva, valor no válido (" + row.getRowNum() + ")");
                            }
                        }
                    }
                }
                Cell cellPrecio = row.getCell(5);
                if (cellPrecio != null) {
                    if (cellPrecio.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        precioVenta = BigDecimal.valueOf(cellPrecio.getNumericCellValue());
                    } else if (cellPrecio.getCellType() == Cell.CELL_TYPE_STRING) {
                        if (StringUtils.trimToNull(cellPrecio.getStringCellValue()) != null) {
                            try {
                                precioVenta = new BigDecimal(cellPrecio.getStringCellValue().replaceAll(",", "."));
                            } catch (Exception ex) {
                                if (estado == null) {
                                    estado = ("Precio no válido  (" + row.getRowNum() + ")");
                                }
                            }
                        }
                    }
                    if (precioVenta.compareTo(BigDecimal.ZERO) == -1
                            || precioVenta.compareTo(BigDecimal.valueOf(99_999_999)) == 1) {
                        if (estado == null) {
                            estado = ("Precio venta demasiado grande (" + row.getRowNum() + ")");
                        }
                    }
                }
                data.add(new Object[]{codigo, nombre, marca, rubro, iva, precioVenta, estado == null ? "ok" : estado});
            }
            workbook.close();
        } catch (IOException ex) {
            LOG.error("leyendo excel importación productos: " + file, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Algo salió mal", JOptionPane.ERROR_MESSAGE);
        } finally {

        }
        return data;
    }

}

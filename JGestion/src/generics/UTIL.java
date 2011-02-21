package generics;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FiruzzZ
 */
public abstract class UTIL {

   public final static String EMAIL_REGEX = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
   /**
    * Regular expression para validar cadenas que:
    * <UL>
    * <LI>Empiece con un caracter alfabético.
    * <LI>Seguidos de 0..* caracteres alfabeticos y espacios.
    * <LI>Termine en un caracter alfabético.
    * </UL>
    * Ejemplos válidos: "a", "a b", "a  B cd".
    */
   public final static String REGEX_ALFA_TEXT_WITH_WHITE =
           "^[a-zA-Z][a-zA-Z\\s]*[a-zA-Z]$|"
           + "^[a-zA-Z]+[a-zA-Z]*$|"
           + "^[a-zA-Z]$";
   public final static Pattern email = Pattern.compile("");
   /**
    * [a-zA-Z]?
    */
   public final static Pattern azAZ = Pattern.compile("[a-zA-Z]?");
   /**
    * [0-9]?
    */
   public final static Pattern _09 = Pattern.compile("[0-9]?");
   /**
    * [a-zA-Z_0-9]?
    */
   public final static Pattern azAZ09 = Pattern.compile("[a-zA-Z_0-9]?");
   /**
    * Límite de filas que se cargan en una Tabla
    */
   public final static int ROW_LIMIT = 1000;
   public final static String TIME_ZONE = "GMT-03:00";
   /**
    * Porque a la poronga de MySql solo le gusta así yyyy/MM/dd
    */
   public final static SimpleDateFormat yyyy_MM_dd;
   /**
    * formato de salida de la fecha dd/MM/yyyy
    */
   public final static SimpleDateFormat DATE_FORMAT;
   /**
    * formato de salida del Time -> String HH:mm:ss
    */
   public final static SimpleDateFormat TIME_FORMAT;
   /**
    * formato de salida Timestamp dd/MM/yyyy HH:mm:ss
    */
   public final static SimpleDateFormat TIMESTAMP_FORMAT;
   /**
    * formato de salida del <code>double</code> -> #,###.00
    * Con separador de millares y COMA de separador decimal
    */
   public final static DecimalFormat DECIMAL_FORMAT;
   /**
    * formato de salida del <code>double</code> a un String casteable nuavamente
    * a double.
    * Es decir que usa el punto (.) como separador decimal y sin separadores
    * de millares
    * Formato "########.##"
    */
   public final static DecimalFormat PRECIO_CON_PUNTO;
   /**
    * Extensiones de imagenes permitidas: "jpeg", "jpg", "gif", "tiff", "tif", "png", "bmp"
    */
   public final static String[] IMAGEN_EXTENSION = {"jpeg", "jpg", "gif", "tiff", "tif", "png", "bmp"};
   /**
    * Tamaño máximo de las imagenes que se puede guardar: 1.048.576 bytes
    */
   public final static int MAX_IMAGEN_FILE_SIZE = 1048576; // en bytes (1Mb/1024Kb/...)

   static {
      DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
      simbolos.setDecimalSeparator('.');
      PRECIO_CON_PUNTO = new DecimalFormat("#######0.00", simbolos);
      DECIMAL_FORMAT = new DecimalFormat("#,###.00");
      DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
      yyyy_MM_dd = new SimpleDateFormat("yyyy/MM/dd");
      TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
      TIMESTAMP_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
   }

   public static SimpleDateFormat instanceOfDATE_FORMAT() {
      return new SimpleDateFormat("dd/MM/yyyy");
   }

   public static SimpleDateFormat instanceOfTIME_FORMAT() {
      return new SimpleDateFormat("HH:mm:ss");
   }

   /**
    * Returns the contents of the file in a byte array
    * @param file File this method should read
    * @return byte[] Returns a byte[] array of the contents of the file
    * @throws IOException
    * @throws Exception
    */
   public static byte[] getBytesFromFile(File file) throws IOException {
      BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
//      InputStream is = new BufferedInputStream(new FileInputStream(file));
      long length = file.length();
      controlSizeFile(file, MAX_IMAGEN_FILE_SIZE);

      // Create the byte array to hold the data
      byte[] bytes = new byte[(int) length];
      // Read in the bytes
      int offset = 0;
      int numRead = 0;
      while ((offset < bytes.length)
              && ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)) {
         //numRead == -1 cuando sea EOF..
         offset += numRead;
      }

      // Ensure all the bytes have been read in
      if (offset < bytes.length) {
         throw new IOException("Could not completely read file " + file.getName());
      }

      is.close();
      return bytes;
   }

   public static void setImageAsIconLabel(JLabel label, byte[] imageInByte) throws IOException {
      ByteArrayInputStream bais = new ByteArrayInputStream(imageInByte);
      BufferedImage bufferedImage = ImageIO.read(bais);
      int labelWidth = label.getWidth();
      int labelHeight = label.getHeight();
      // Get a transform...
      AffineTransform trans = AffineTransform.getScaleInstance(
              (double) labelWidth / bufferedImage.getWidth(), (double) labelHeight / bufferedImage.getHeight());
      Graphics2D g = (Graphics2D) label.getGraphics();
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      GraphicsConfiguration cg = gd.getDefaultConfiguration();
      int transparency = bufferedImage.getColorModel().getTransparency();
      BufferedImage dest = cg.createCompatibleImage(labelWidth, labelHeight, transparency);
      g = dest.createGraphics();
      g.drawRenderedImage(bufferedImage, trans);
      g.dispose();
      label.setIcon(new ImageIcon(dest)); // <-- si hace resize()
      label.setText(null);
   }

   /**
    * Ajusta la imagen al size de la jLabel, también deja <code>null</code> el texto de la label
    * @param label
    * @param imageFile File de una imagen, la cual se va ajustar al tamaño de la jLabel.
    * @return el jLabel con la imagen ajustada..
    * @exception  java.io.IOException si no puede leer el <code>imageFile</code>
    * @exception Exception si el tamaño del archivo supera el configurado permitido (default is Integer.MAX_VALUE).
    */
   public static JLabel setImageAsIconLabel(JLabel label, File imageFile)
           throws IOException {
      controlSizeFile(imageFile, MAX_IMAGEN_FILE_SIZE);
      BufferedImage bufferedImage = ImageIO.read(imageFile);
      int labelWidth = label.getWidth();
      int labelHeight = label.getHeight();
      // Get a transform...
      AffineTransform trans = AffineTransform.getScaleInstance(
              (double) labelWidth / bufferedImage.getWidth(), (double) labelHeight / bufferedImage.getHeight());

      Graphics2D g = (Graphics2D) label.getGraphics();
//        g.drawRenderedImage(src, trans);
//        jLabel.setIcon(new ImageIcon(src)); // <-- no resizea la img en la label
      //----------------------------
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      GraphicsConfiguration cg = gd.getDefaultConfiguration();
      int transparency = bufferedImage.getColorModel().getTransparency();
      BufferedImage dest = cg.createCompatibleImage(labelWidth, labelHeight, transparency);
      g = dest.createGraphics();
      g.drawRenderedImage(bufferedImage, trans);
      g.dispose();
      label.setIcon(new ImageIcon(dest)); // <-- si hace resize()
      label.setText(null);
      return label;
   }

   public static void controlSizeFile(File file, int size) {
      // Get the size of the file
      long length = file.length();
      if (length > size) {
         throw new IllegalArgumentException("La imagen seleccionada es demasiado grande.\n"
                 + "El tamaño no debe superar los " + size + " bytes (tamaño actual: " + length + "b)");
      }
   }

   /**
    * Transforma una IMAGEN de tipo bytea (postgre) a un java.io.File
    * @param img tipo de dato almacenado en la DB (debe ser una imagen)
    * @param extension la extensión de la imagen. if == null, se le asigna "png"
    * @return el archivo de la imagen que fue creado en el disco
    * @throws IOException
    */
   public static File imageToFile(byte[] img, String extension)
           throws IOException {
//      Toolkit.getDefaultToolkit().createImage(img);
//        java.awt.image.BufferedImage src = new java.awt.image.BufferedImage(0, 0, 0);
      if (extension == null || extension.length() < 1) {
         extension = "png";
      }

      File file = new File("./reportes/img." + extension);
      file.createNewFile();
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(img);
      OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
      int data;
      while ((data = byteArrayInputStream.read()) != -1) {
         outputStream.write(data);
      }
      outputStream.close();
      return file;
   }

   /**
    * Return true si la extesión es tiff/tif/gif/jpg/jpeg/png/bmp
    * @param imageFile
    * @return si la extesión del archivo es de algún tipo de imagen.
    */
   public static boolean isImagenExtension(File imageFile) {
      String extension = UTIL.getExtensionFile(imageFile.getName());
      if (extension != null) {
         for (String string : IMAGEN_EXTENSION) {
            if (extension.compareToIgnoreCase(string) == 0) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Validador de CUIL/CUIT
    * @param cuil .. formato del String ########### (11)
    * @throws Exception1 si la length != 11.
    * @throws Exception2 si los 2 1ros dígitos no corresponden a ningún tipo.
    * @throws Exception3 si el dígito identificador (el último) no se corresponde al cálculo.
    * @throws NumberFormatException if can not be castable to a Long type.
    */
   public static void VALIDAR_CUIL(String cuil) throws NumberFormatException {
      String c = cuil.trim();
      try {
         Long.valueOf(cuil);
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("La CUIT/CUIL no es válida (ingrese solo números)");
      }
      if (c.length() != 11) {
         throw new IllegalArgumentException("Longitud de la CUIT/CUIL no es correcta (" + c.length() + ")");
      }
      //ctrl de los 1ros 2 dígitos...//
      String x = c.substring(0, 2);
      int xx = Integer.parseInt(x);
      if ((xx != 20) && (xx != 23) && (xx != 24) && (xx != 27) && (xx != 30) && (xx != 33) && (xx != 34)) {
         throw new IllegalArgumentException("Los primeros 2 dígitos de la CUIT/CUIL no corresponden a ningún tipo."
                 + "\nHombres: 20, 23 o 24; Mujeres: 27; Empresas: 30, 33, 34");
      }
      //ctrl del verificador...//
      int digito, suma = 0;
      int[] codigo = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
      for (int index = 0; index < 10; index++) {
         digito = Integer.parseInt(cuil.substring(index, index + 1));
         suma += digito * codigo[index];
      }
      if (Integer.parseInt(cuil.substring(10, 11)) != (11 - (suma % 11))) {
         throw new IllegalArgumentException("El dígito verificador de la CUIT/CUIL no es correcto");
      }
   }

   public static DefaultTableModel getDtm(JTable jtable) {
      return (DefaultTableModel) jtable.getModel();
   }

   public static JTable getDefaultTableModel(JTable tabla, String[] columnsName) {
      DefaultTableModel dtm = new DefaultTableModelImpl();
      if (tabla.getModel() != null) {
         dtm = (DefaultTableModel) tabla.getModel();
      }
      for (String string : columnsName) {
         dtm.addColumn(string);
      }
      tabla.getTableHeader().setReorderingAllowed(false);
      return tabla;
   }

   /**
    * Personaliza una DefaulTableModel
    * @param tabla en la cual se insertará el modelo, if this is null, a new one will be initialized
    * @param columnNames Nombre de las HeaderColumns
    * @param columnWidths Ancho de columnas
    * @param columnClassType Tipo de datos que va contener cada columna
    * @param editableColumns Index de las columnas las cuales podrán ser
    * editables ( > -1 && cantidadColumnas >= columnsCount)
    * @return una JTable con el modelo
    */
   public static JTable getDefaultTableModel(JTable tabla,
           String[] columnNames, int[] columnWidths, Class[] columnClassType,
           int[] editableColumns) {

      if ((columnNames.length != columnWidths.length)) {
         throw new IllegalArgumentException("los Array no tiene la misma cantidad de elementos"
                 + " (Names length = " + columnNames.length + ""
                 + " y Widthslength = " + columnWidths.length + ")");
      } else {
         if (columnClassType != null && (columnNames.length != columnClassType.length)) {
            throw new IllegalArgumentException("los Array no tiene la misma cantidad de "
                    + "elementos (column Names = " + columnNames.length
                    + " y ClassType = " + columnClassType.length + ")");
         }
      }
      if (editableColumns != null) {
         for (int i : editableColumns) {
            if (i < 0 || i > (columnNames.length - 1)) {
               throw new IndexOutOfBoundsException("El array editableColumns[]"
                       + " contiene un indice número de columna no válido: index = " + i);
            }
         }
      }

      DefaultTableModel dtm;
      if (columnClassType != null && editableColumns != null) {
         dtm = new DefaultTableModelImpl(columnClassType, editableColumns);
      } else if (columnClassType != null) {
         dtm = new DefaultTableModelImpl(columnClassType);
      } else if (editableColumns != null) {
         dtm = new DefaultTableModelImpl(editableColumns);
      } else {
         dtm = new DefaultTableModelImpl();
      }
      for (String string : columnNames) {
         dtm.addColumn(string);
      }
      if (tabla == null) {
         tabla = new JTable(dtm);
      } else {
         tabla.setModel(dtm);
      }

      for (int i = 0; i < dtm.getColumnCount(); i++) {
         tabla.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
      }
      tabla.getTableHeader().setReorderingAllowed(false);
      return tabla;
   }

   public static JTable getDefaultTableModel(
           JTable tabla, String[] columnNames, int[] columnWidths, Class[] columnClassType) {

      if ((columnNames.length != columnWidths.length)) {
         throw new IllegalArgumentException("los Array no tiene la misma cantidad de elementos"
                 + " (Names length = " + columnNames.length + ""
                 + " y Widthslength = " + columnWidths.length + ")");
      } else {
         if (columnClassType != null && (columnNames.length != columnClassType.length)) {
            throw new IllegalArgumentException("los Array no tiene la misma cantidad de "
                    + "elementos (column Names = " + columnNames.length
                    + " y ClassType = " + columnClassType.length + ")");
         }
      }

      DefaultTableModel dtm;
      if (columnClassType != null) {
         dtm = new DefaultTableModelImpl(columnClassType);
      } else {
         dtm = new DefaultTableModelImpl();
      }
      for (String string : columnNames) {
         dtm.addColumn(string);
      }
      if (tabla == null) {
         tabla = new JTable(dtm);
      } else {
         tabla.setModel(dtm);
      }

      for (int i = 0; i < dtm.getColumnCount(); i++) {
         tabla.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
      }
      tabla.getTableHeader().setReorderingAllowed(false);
      return tabla;
   }

   public static JTable getDefaultTableModel(JTable tabla, String[] columnNames, int[] columnWidths) {

      if (columnNames.length != columnWidths.length) {
         throw new IllegalArgumentException("los columns Names y Widths no tiene la misma cantidad de elementos (length !=)");
      }
      DefaultTableModel dtm = new DefaultTableModelImpl();
      for (String string : columnNames) {
         dtm.addColumn(string);
      }
      if (tabla == null) {
         tabla = new JTable(dtm);
      } else {
         tabla.setModel(dtm);
      }

      for (int i = 0; i < dtm.getColumnCount(); i++) {
         tabla.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
      }
      tabla.getTableHeader().setReorderingAllowed(false);
      return tabla;
   }

   /**
    * Devuelve un dtm con los nombres de las columnas
    * @param dtm if == null a new instance of a PRIVATE implementation of will be created.
    * @param columnNames los nombres de las respectivas columns que va tener la tabla.
    * @return a DefaultTableModel with column names setted.
    * @exception NullPointerException if columnNames is null.
    */
   public static DefaultTableModel setColumnNames(DefaultTableModel dtm, String[] columnNames) {
      if (dtm == null) {
         dtm = new DefaultTableModelImpl();
      }
      if (columnNames != null && columnNames.length > 0) {
         for (String string : columnNames) {
            dtm.addColumn(string);
         }
      }
      return dtm;
   }

   public static boolean hasta2Decimales(String monto) {
      if (monto != null && monto.length() > 0) {
         monto = monto.trim();
         char[] ja = monto.toCharArray();
         for (int i = 0; i < monto.length(); i++) {
            if (ja[i] == '.') {
               if (i + 3 >= monto.length()) {
                  return true;
               } else {
                  return false;
               }
            }
         }
      } else {
         return false;
      }
      return true; // si no encontró ningún '.'
   }

   /**
    * Crea un java.util.Date con los parámetros de fecha mandados y
    * TimeZone a GTM-3 (no toma del SO ni nada).
    * @param year el año del Date
    * @param month 0=enero and so on...
    * @param day if == NULL.. will be setted to 1, si == -1 al último día de <b>month</b>
    * @return java.util.Date inicializado con la fecha.
    * @throws Exception si month < 0 || month > 11
    */
   public static Date customDate(int year, int month, Integer day)
           throws Exception {
      if (month < 0 || month > 11) {
         throw new Exception("Mes (month) no válido, must be >= 0 AND <= 11");
      }
      Calendar c = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
      if (day == null) {
         c.set(year, month, 1);
      } else if (day == -1) {
         c.set(year, month + 1, -1);
      }
      return c.getTime();
   }

   /**
    * Personaliza una fecha según los parámetros. <code>fecha</code> no debe ser
    * <code>null</code>.
    * @param fecha un java.util.Date como punto de referencia inicial
    * @param year años por adicionar o restar
    * @param month meses por adicionar o restar
    * @param days días por adicionar o restar
    * @return a customized Date
    */
   public static Date customDate(Date fecha, int year, int month, int days) {
      Calendar c = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
      c.setTime(fecha);
      c.add(Calendar.YEAR, year);
      c.add(Calendar.MONTH, month);
      c.add(Calendar.DAY_OF_MONTH, days);
      return c.getTime();
   }

   /**
    * Devuelte un Date modificada según los <code>dias</code>
    * @param fecha Date base sobre el cual se va trabajar. If <code>fecha</code> is <code>null</code> will return null Date
    * @param dias cantidad de días por adicionar o restar a <code>fecha</code>
    * @return customized Date!!!...
    */
   public static Date customDateByDays(java.util.Date fecha, int dias) {
      Calendar c = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
      c.setTime(fecha);
      c.add(Calendar.DAY_OF_MONTH, dias);
      return c.getTime();
   }

   public static String getExtensionFile(String fileName) {
      return (fileName.lastIndexOf(".") == -1) ? ""
              : fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
   }

   /**
    * Borra las imagenes temporales creadas por reportes e informes
    * @param pathName si es == a "777".. va buscar todos los archivos img.IMG_EXTENSION
    * en /reportes/
    */
   public static void borrarFile(String pathName) {
      File f = null;
      if (pathName != null && pathName.length() > 0) {
         if (pathName.equals("777")) {
            for (String string : IMAGEN_EXTENSION) {
               try {
                  f = new File("./reportes/img." + string);
                  if (f.delete()) {
                     System.out.println("borrado..." + string);
                  } else {
                     System.out.println("no existía " + string);
                  }
               } catch (NullPointerException e) {
                  System.out.println("Exception.. no existía ->" + string);
               } catch (SecurityException e) {
                  System.out.println("SECURITY EXCEPTION!!!");
               }
            }
         } else {
            f = new File(pathName);
            if (f.delete()) {
               System.out.println("borrado...pathName=" + pathName);
            } else {
               System.out.println("no Existía");
            }
         }
         //pathName==null
      } else {
         f = new File("./reportes/img.png");
      }

   }

   /**
    * Controla que no se pueda tipear mas de un "."
    * @param cadena
    * @return <code>true</code> if 0 '.' has been found.
    * If <code>cadena</code> is null or 1 > length return <code>false</code>
    */
   public static boolean SOLO_UN_PUNTO(String cadena) {

      if (cadena == null || cadena.length() < 1) {
         return false;
      }
      System.out.println("cadena: " + cadena);
      int cantDePuntos = 0;
      for (int i = 0; i < cadena.length(); i++) {
         if (cadena.charAt(i) == '.') {
            cantDePuntos++;
         }
         if (cantDePuntos > 0) {
            return false;
         }
      }
      return true;
   }

   /**
    * Ctrla que sea un caracter numérico el apretado
    * @param KeyEvent evt!!
    * @return true si es un caracter numérico [1234567890], otherwise will return false.
    */
   public static void soloNumeros(KeyEvent evt) {
      int k = evt.getKeyChar();
      if (k < 48 || k > 57) {
         evt.setKeyChar((char) KeyEvent.VK_CLEAR);
      }
   }

   public static void solo_numeros_y_un_punto(String cadena, KeyEvent e) {
      // si es != de '.'
      if ((int) e.getKeyChar() != 46) {
         soloNumeros(e);

      } else // ctrl si ya se ha tipeado un '.'
      if (SOLO_UN_PUNTO(cadena)) {
         int k = e.getKeyChar();
         if ((k < 48 || k > 57) && (k != 46)) {
            e.setKeyChar((char) KeyEvent.VK_CLEAR);
         }
      } else {
         e.setKeyChar((char) KeyEvent.VK_CLEAR);
      }
   }

   /**
    * Devuelve un String del estado (int o short)
    * @param estado 1 = Activo, 2= Baja, 3= Suspendido
    * @return
    */
   public static String ESTADO_TO_STRING(int estado) {
      if (estado == 1) {
         return "Activo";
      } else if (estado == 2) {
         return "Baja";
      } else if (estado == 3) {
         return "Suspendido";
      }
      return null;
   }

//    /**
//     * Ctrla que sea un caracter numérico el apretado
//     * @param KeyEvent evt!!
//     * @return true si es un caracter numérico [1234567890], otherwise will return false.
//     */
//    public static boolean soloNumeros(java.awt.event.KeyEvent evt) {
//        int k = evt.getKeyChar();
//        if(k<48 || k>57) return false;
//        return true;
//    }
   public static String TO_UPPER_CASE(String unString) {
      if (unString != null) {
         return unString.toUpperCase();
      }
      throw new NullPointerException("El String para mayusculizar es NULL!");
   }

   /**
    * Pasa el caracter a mayuscula y retorna.
    * @param letra a mayusculizar.
    * @return el character mayusculizado.
    */
   public static char TO_UPPER_CASE(char letra) {
      return (String.valueOf(letra).toUpperCase()).charAt(0);
   }

   /**
    * Remueve de la ColumnModel las columnas que se le indique,
    * deja de ser visible para el usuario pero sigue siendo accesible desde el
    * TableModel
    * @param jTable tabla de la cual se desea sacar las columnas
    * @param columnsIndex columnas a quitar de la vista del usuario
    */
   public static void hideColumnsTable(JTable jTable, int[] columnsIndex) {
      for (int i = 0; i < columnsIndex.length; i++) {
         // se le va restando i al index real indicado, porque estos se van desplazando
         // a medida que se van eliminando las columnas
         jTable.getColumnModel().removeColumn(jTable.getColumnModel().getColumn(columnsIndex[i] - i));
      }
   }

   /**
    * Remueve de la ColumnModel la columna que se le indique,
    * deja de ser visible para el usuario pero sigue siendo accesible desde el
    * TableModel (DefaultTableModel)
    * @param jTable tabla de la cual se desea sacar la columna
    * @param columnIndex número de la columna a quitar de la vista del usuario
    */
   public static void hideColumnTable(JTable jTable, int columnIndex) {
      hideColumnsTable(jTable, new int[]{columnIndex});
//      jTable.getColumnModel().removeColumn(jTable.getColumnModel()
//                                                      .getColumn(columnIndex));
   }

   /**
    * setea como selected al item del comboBox que coincida con el <code>candidato</code>
    * @param combo if is null or combo<code>.getItemCount() </code> is less than 1, no selectedItem
    * @param candidato if this is <code>null</code>, no habrá selectedItem
    */
   public static void setSelectedItem(JComboBox combo, String candidato) {
      if (candidato == null || candidato.length() < 1 || combo == null) {
         return;
      }

      boolean encontrado = false;
      int index = 0;
      while (index < combo.getItemCount() && !encontrado) {
         if ((combo.getItemAt(index)).toString().equals(candidato)) {
            combo.setSelectedIndex(index);
            encontrado = true;
         }
         index++;
      }
   }

   /**
    * Setea como selected al item del comboBox que coincida con el <code>candidato</code>
    * Este método utiliza <code>equals</code> para la comparación
    * @param combo El cual podría contener el item <code>candidato</code>
    * @param candidato
    * @return a index of the selectedItem, or <code>-1</code> if
    * 1 > combo.getItemCount() || if <code>candidato</code> does not match any item
    */
   public static int setSelectedItem(javax.swing.JComboBox combo, Object candidato) {
      if (candidato == null) {
         throw new IllegalArgumentException("El Objeto candidato == null");
      }
      if (combo == null) {
         throw new IllegalArgumentException("El JCombo combo == null");
      }
      if (combo.getItemCount() < 1) {
         return -1;
      }

      boolean encontrado = false;
      int index = 0;
      while (index < combo.getItemCount() && !encontrado) {
         if ((combo.getItemAt(index)).equals(candidato)) {
            combo.setSelectedIndex(index);
            encontrado = true;
         } else {
            index++;
         }
      }
      return index;
   }

   /**
    * Carga la List de objetos en el comboBox
    * @param comboBox JComboBox donde se van a cargar los Objectos
    * @param objectList Si la List está vacía o es null se carga un String Item "Vacio"
    * @param elegible Si es true, se agrega un Item "Elegir" en el index 0; sino
    * solo se cargar los objetos
    */
   public static void loadComboBox(JComboBox comboBox, List objectList, boolean elegible) {
      if (comboBox == null) {
         throw new IllegalArgumentException("El Objecto comboBox que intentas cargar es NULL!!! (NullPointerException) guampa!!");
      }

      comboBox.removeAllItems();
      if (objectList != null && objectList.size() > 0) {
         //si se permite que NO se elija ningún elemento del combobox
         if (elegible) {
            comboBox.addItem("<Elegir>");
         }

         for (Object object : objectList) {
            comboBox.addItem(object);
         }
      } else {
         //si la lista a cargar está vacía o es NULL
         comboBox.addItem("<Vacio>");
      }
   }

   /**
    * Personaliza la carga de datos en un JComboBox, según una List y bla bla...
    * @param comboBox
    * @param objectList collection la cual se va cargar
    * @param message1erItem mensaje del 1er item del combo, dejar <code>null</code> si no hay preferencia
    * @param itemWhenIsEmpy item que se va cargar cuando el combo esté vacio
    */
   public static void loadComboBox(JComboBox comboBox, List objectList, String message1erItem, String itemWhenIsEmpy) {
      if (itemWhenIsEmpy == null) {
         loadComboBox(comboBox, objectList, message1erItem);
      } else {
         comboBox.removeAllItems();
         if (objectList != null && objectList.size() > 0) {

            if (message1erItem != null && message1erItem.length() > 0) {
               comboBox.addItem(message1erItem);
            }

            for (Object object : objectList) {
               comboBox.addItem(object);
            }
         } else {
            //si la lista a cargar está vacía o es NULL
            comboBox.addItem(itemWhenIsEmpy);
         }
      }
   }

   /**
    * Personaliza la carga de datos en un JComboBox, según una List y bla bla...
    * Si no hay elementos para cargar al comboBox, retor
    * @param comboBox
    * @param objectList collection la cual se va cargar
    * @param message1erItem mensaje del 1er item del combo, dejar <code>null</code> si no hay preferencia
    */
   public static void loadComboBox(JComboBox comboBox, List objectList, String message1erItem) {
      comboBox.removeAllItems();
      if (objectList != null && objectList.size() > 0) {
         if (message1erItem != null && message1erItem.length() > 0) {
            comboBox.addItem(message1erItem);
         }

         for (Object object : objectList) {
            comboBox.addItem(object);
         }
      } else {
         //si la lista a cargar está vacía o es NULL
         comboBox.addItem("<Vacio>");
      }
   }

   /**
    * Agrega "0" a la <code>cadena</code> hasta que esta tenga la longitudMaxima
    * @param cadena If == <code>null</code> will do nothing!
    * @param longitudMaxima agrega "0" hasta que <code>cadena</code> tenga la longitud deseada
    * @return <code>cadena<code> overclocking..
    */
   public static String AGREGAR_CEROS(String cadena, int longitudMaxima) {
      if (cadena == null) {
         throw new NullPointerException("El parámetro cadena es NULL!!!");
      }
      StringBuilder newCadena = new StringBuilder(cadena);
      for (int i = newCadena.toString().length(); i < longitudMaxima; i++) {
         newCadena.insert(0, "0");
      }
      return newCadena.toString();
   }

   public static String AGREGAR_CEROS(long numero, int longitudMaxima) {
      return AGREGAR_CEROS(String.valueOf(numero), longitudMaxima);
   }

   /**
    * Devuelte el % del monto
    * @param monto sobre el cual se calcula el %
    * @param porcentaje
    * @return El porcentaje (%) del monto, being <code>0 >= monto</conde> or  0 >= <code>porcentaje</code>, otherwise will return 0.0!
    */
   public static Double getPorcentaje(double monto, double porcentaje) {
      if (monto <= 0 || porcentaje <= 0) {
         return 0.0;
      }
      return (porcentaje * (monto / 100));
   }

   /**
    * Obtiene la TableModel (DefaultTableModel) y elimina todas las filas
    * @param table
    * @see #limpiarDtm(javax.swing.table.DefaultTableModel)
    */
   public static void limpiarDtm(JTable table) {
      limpiarDtm((DefaultTableModel) table.getModel());
   }

   public static void limpiarDtm(DefaultTableModel dtm) {
      for (int i = dtm.getRowCount() - 1; i > -1; i--) {
         dtm.removeRow(i);
      }
   }

   /**
    * Devuelve el Objeto en la celda de la fila selecciada y la columna indicada
    * @param jTable De la cual se va obtener el DefaultTableModel y la selectedRow.
    * @param indexColumn La columna de la que se solicita el Object
    * @return the value Object at the specified cell, or <code>null</code> if no row is selected
    */
   public static Object getSelectedValue(JTable jTable, int indexColumn) {
      if (jTable.getSelectedRow() != -1) {
         return ((DefaultTableModel) jTable.getModel()).getValueAt(jTable.getSelectedRow(), indexColumn);
      } else {
         return null;
      }
   }

   /**
    * Implementación de DefaultTableModel
    * By default editableColumns = false
    */
   private static class DefaultTableModelImpl extends DefaultTableModel {

      private Class[] columnTypes = null;
      private int[] editableColumns = null;

      /**
       * Constructor por defecto igual al javax.swing.table.DefaultTableModel
       * Me parece que está alpedo..
       */
      public DefaultTableModelImpl() {
      }

      /**
       * Este constructor permite especificar a que class pertenecen los datos
       * que se van a insertar en cada columna
       * @param columnTypes tipo de objeto que contendrá cada columna
       */
      public DefaultTableModelImpl(Class[] columnTypes) {
         this.columnTypes = columnTypes;
      }

      /**
       * Este constructor permite especificar a que class pertenecen los datos
       * que se van a insertar en cada columna y cuales será editables
       * @param columnTypes tipo de objecto que contendrá cada columna
       * @param editableColumns debe contenedor los números (index de las columnas) que se
       * desea que puedan ser editables.
       */
      public DefaultTableModelImpl(Class[] columnTypes, int[] editableColumns) {
         this.columnTypes = columnTypes;
         this.editableColumns = editableColumns;
      }

      private DefaultTableModelImpl(int[] editableColumns) {
         this.editableColumns = editableColumns;
      }

      @Override
      public boolean isCellEditable(int row, int column) {
         if (editableColumns != null) {
            boolean columnEditable = false;
            for (int i = (editableColumns.length - 1); i > -1; i--) {
               columnEditable = (column == editableColumns[i]);
               if (columnEditable) {
                  break;
               }
            }
            return columnEditable;
         } else {
            return false;
         }
      }

      @Override
      public Class<?> getColumnClass(int columnIndex) {
         if (columnTypes != null) {
            return columnTypes[columnIndex];
         } else {
            return super.getColumnClass(columnIndex);
         }
      }
   }

   /**
    * Valida si es un email
    * @param email String to validate
    * @return return <code>true</code> si es una dirección de email
    */
   public static boolean VALIDAR_EMAIL(String email) {
      return UTIL.email.matcher(email).find();
   }

   public static boolean VALIDAR_REGEX(String regex, String stringToEvaluate) {
      return Pattern.compile(regex).matcher(stringToEvaluate).find();
   }
}

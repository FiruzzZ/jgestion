package jgestion;

import generics.WaitingDialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public abstract class ShutDownListener {

    private boolean closeFuerzaBrutaThread = false;
    private boolean shutDownSystem = false;
    private boolean activeConnection = false;
    private Connection connection;
    private static final Logger LOG = LogManager.getLogger();
    private String message;
    private WaitingDialog lostConnectionDialog;
    private final Thread fuerzaBrutaShutDown = new Thread(new Runnable() {
        @Override
        public void run() {
            LOG.trace("Iniciando bucle fuerzaBruta!!!");
            while (!closeFuerzaBrutaThread && !shutDownSystem) {
                try {
                    shutDownSystem = hasToShutdown();
                    if (shutDownSystem) {
                        shutDownAction();
                    }
                    activeConnection = true;
                    if (lostConnectionDialog != null && lostConnectionDialog.isVisible()) {
                        lostConnectionDialog.dispose();
                    }
                } catch (InterruptedException ex) {
                    LOG.trace("shutDownThread sleeping INTERRUPTED!!");
                    break;
                } catch (Exception ex) {
                    LOG.warn("shutDownThread Exception!!", ex);
                    activeConnection = false;
                    displayLostConnectionUI(ex);
                }
            }
            LOG.trace("finishing Thread > fuerzaBrutaShutDown");
        }
    });

    public abstract Connection getConnection();

    public abstract void shutDownAction();

    public void close() throws SQLException {
        closeFuerzaBrutaThread = true;
        if (!connection.isClosed()) {
            connection.close();
        }
    }

    public boolean isShutDownSystem() {
        return shutDownSystem;
    }

    public final Thread getFuerzaBrutaShutDown() {
        return fuerzaBrutaShutDown;
    }

    public boolean hasToShutdown() throws SQLException, InterruptedException {
//        try {
        ResultSet rs = getActiveConnection().createStatement().executeQuery("SELECT shutdown, shutdown_message from sistema");
        rs.next();
        boolean cerrar = rs.getBoolean(1);
        if (cerrar) {
            message = rs.getString(2).replaceAll(" ", "_");
        } else {
            Thread.sleep(5000);
        }
//            if (lostConnectionDialog != null && lostConnectionDialog.isVisible()) {
//                lostConnectionDialog.dispose();
//            }
        return cerrar;
//        } catch (SQLException sQLException) {
//            displayLostConnectionUI();
//            return hasToShutdown();
//        }
    }

    private Connection getActiveConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = getConnection();
        }
        return connection;
    }

    public final String getMessage() {
        return message;
    }

    private void displayLostConnectionUI(Exception exception) {
        System.out.println("display lost connection");
        if (lostConnectionDialog == null) {
            lostConnectionDialog = new WaitingDialog(null, "Error de conexión con la base de datos.", true,
                    "<html>"
                    + "<br>Esta ventana desaparecerá cuando la conexión sea re-establecida o"
                    + "<br>puede cerrar la ventana para finalizar el programa."
                    + "<br>" + exception.getLocalizedMessage()
                    + "</html>");
            lostConnectionDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("closing");
                    if (!activeConnection) {
                        System.exit(1);
                    }
                }
            });
            lostConnectionDialog.getLabelMessage().setIcon(new ImageIcon(getClass().getResource("/iconos/gnome-panel-force-quit.png")));
            lostConnectionDialog.setLocationRelativeTo(null);
        }
        if (!lostConnectionDialog.isVisible()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    lostConnectionDialog.setVisible(true);
                }
            }).start();
        } else {
            if (lostConnectionDialog.isVisible()) {
                try {
                    System.out.print(".");
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}

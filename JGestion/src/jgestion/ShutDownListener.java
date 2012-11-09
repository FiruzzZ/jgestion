package jgestion;

import generics.WaitingDialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import org.apache.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public abstract class ShutDownListener {

    private boolean closeFuerzaBrutaThread = false;
    private boolean shutDownSystem = false;
    private Connection connection;
    private static final Logger LOG = Logger.getLogger(ShutDownListener.class.getName());
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
                } catch (InterruptedException ex) {
                    LOG.trace("shutDownThread sleeping INTERRUPTED!!");
                    break;
                } catch (Exception ex) {
                    LOG.warn("shutDownThread Exception!!", ex);
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
        ResultSet rs = getActiveConnection().createStatement().executeQuery("SELECT shutdown from sistema");
        rs.next();
        boolean cerrar = rs.getBoolean(1);
        if (!cerrar) {
            Thread.sleep(5000);
        } else {
            message = getShutDownMessage();
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

    public String getShutDownMessage() throws SQLException {
        ResultSet rs = getActiveConnection().createStatement().executeQuery("SELECT shutdown_message from sistema");
        if (rs.next()) {
            return rs.getString(1).replaceAll(" ", "_");
        } else {
            return "No_message_from_server";
        }
    }

    private void displayLostConnectionUI() {
        System.out.println("display lost connection");
        if (lostConnectionDialog == null) {
            lostConnectionDialog = new WaitingDialog((JDialog) null, "Error de conexión", true, "<html>Error de conexión con la base de datos."
                    + "\nEsta ventana desaparecerá cuando la conexión sea re-establecida</html>");
            lostConnectionDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    System.exit(1);
                }
            });
            lostConnectionDialog.getLabelMessage().setIcon(new ImageIcon(getClass().getResource("/img/db_connection_error.png")));
        }
        if (!lostConnectionDialog.isVisible()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    lostConnectionDialog.setVisible(true);
                }
            }).start();
        } else {
            while (lostConnectionDialog.isVisible()) {
                try {
                    System.out.print(".");
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}

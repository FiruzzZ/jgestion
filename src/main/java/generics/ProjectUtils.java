package generics;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import org.apache.logging.log4j.LogManager;
import utilities.security.PasswordHash;

/**
 *
 * @author FiruzzZ
 */
public final class ProjectUtils {

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources");
    public static final String ICONS_FOLDER = "iconsfolder";
    public static final String REPORTS_FOLDER = "reportsfolder";
    public static final String DATE_FORMAT = getProperty("dateFormat");
    /**
     * Hora sin segundos
     */
    public static final String TIME_FORMAT = getProperty("timeFormat");
    /**
     * Hora con segundos
     */
    public static final String TIMEF_FORMAT = getProperty("timefFormat");
    /**
     * formato para Mes y año (sin días)
     */
    public static final String PERIODO_FORMAT = getProperty("periodoFormat");
    public static final String TIMESTAMP_FORMAT = getProperty("timestampFormat");

    public static String getProperty(String key) {
        return resourceBundle.getString(key);
    }

    public static ImageIcon getIcon(String iconName) {
        try {
            return new ImageIcon(ProjectUtils.class.getResource(getProperty(ICONS_FOLDER) + iconName));
        } catch (Exception e) {
            LogManager.getLogger().warn("Icon not found: " + iconName);
            return null;
        }
    }

    public static String encrypt(String pwd) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int it = 100000;
        byte[] salt;
        salt = PasswordHash.getSalt(24);
        byte[] hash = PasswordHash.getPBKDF2WithHmacSHA512(pwd, it, salt, 64 * 8);
        return PasswordHash.toHex(salt) + ":" + it + ":" + PasswordHash.toHex(hash);
    }

    public static String getBuildNumber() {
        return ResourceBundle.getBundle("build").getString("buildnumber");
    }

    private ProjectUtils() {
    }
}

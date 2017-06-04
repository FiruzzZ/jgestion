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

package generics;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author FiruzzZ
 */
public class JPAUtils {

    private Map<String, String> properties = new HashMap<String, String>(5);

    /**
     * Example: "jdbc:postgresql://jgestion.no-ip.org:5433/jgestionahinco"
     * @param value
     * @return 
     */
    public Map setJDBC_URL(String value) {
        if (value == null) {
            throw new IllegalArgumentException("parameter value can not be null");
        }
        properties.put("javax.persistence.jdbc.url", value);
        return properties;
    }

    /**
     * Example: "org.postgresql.Driver"
     * @param value
     * @return 
     */
    public Map setJDBCDriver(String value) {
        if (value == null) {
            throw new IllegalArgumentException("parameter value can not be null");
        }
        properties.put("javax.persistence.jdbc.driver", value);
        return properties;
    }

    public Map setJDBCUser(String value) {
        if (value == null) {
            throw new IllegalArgumentException("parameter value can not be null");
        }
        properties.put("javax.persistence.jdbc.user", value);
        return properties;
    }

    public Map setJDBCPassword(String value) {
        if (value == null) {
            throw new IllegalArgumentException("parameter value can not be null");
        }
        properties.put("javax.persistence.jdbc.password", value);
        return properties;
    }

    public Map setAnotherProperty(String key, String value) {
        if (value == null) {
            throw new IllegalArgumentException("parameter value can not be null");
        }
        properties.put(key, value);
        return properties;
    }
}

package entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "msj_informativos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"code"})
})
@NamedQueries({
    @NamedQuery(name = "MsjInformativos.findAll", query = "SELECT m FROM MsjInformativos m"),
    @NamedQuery(name = "MsjInformativos.findById", query = "SELECT m FROM MsjInformativos m WHERE m.id = :id"),
    @NamedQuery(name = "MsjInformativos.findByCode", query = "SELECT m FROM MsjInformativos m WHERE m.code = :code")
})
public class MsjInformativos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "message", nullable = false, length = 3000)
    private String message;
    @Basic(optional = false)
    @Column(name = "code", nullable = false)
    private String code;

    public MsjInformativos() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MsjInformativos)) {
            return false;
        }
        MsjInformativos other = (MsjInformativos) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.MsjInformativos[id=" + id + "]";
    }
}

package jgestion.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "operaciones_bancarias", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OperacionesBancarias.findAll", query = "SELECT o FROM OperacionesBancarias o"),
    @NamedQuery(name = "OperacionesBancarias.findById", query = "SELECT o FROM OperacionesBancarias o WHERE o.id = :id")
})
public class OperacionesBancarias implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 60)
    @OrderBy(value = "nombre")
    private String nombre;

    public OperacionesBancarias() {
    }

    public OperacionesBancarias(Integer id) {
        this.id = id;
    }

    public OperacionesBancarias(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
        if (!(object instanceof OperacionesBancarias)) {
            return false;
        }
        OperacionesBancarias other = (OperacionesBancarias) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.OperacionesBancarias[ id=" + id + " ]";
    }
}

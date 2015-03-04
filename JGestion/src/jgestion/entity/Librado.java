package jgestion.entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author FiruzzZ
 * @deprecated 
 */
@Entity
@Table(name = "librado", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre"})})
@NamedQueries({
    @NamedQuery(name = "Librado.findAll", query = "SELECT l FROM Librado l"),
    @NamedQuery(name = "Librado.findById", query = "SELECT l FROM Librado l WHERE l.id = :id"),
    @NamedQuery(name = "Librado.findByNombre", query = "SELECT l FROM Librado l WHERE l.nombre = :nombre")})
@Deprecated
public class Librado implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
//    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 30)
    private String nombre;

    public Librado() {
    }

    public Librado(Integer id, String nombre) {
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
        if (!(object instanceof Librado)) {
            return false;
        }
        Librado other = (Librado) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.nombre;
    }
    
}

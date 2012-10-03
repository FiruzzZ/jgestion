
package entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "rubro", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre", "tipo"})
})
@NamedQueries({
    @NamedQuery(name = "Rubro.findAll", query = "SELECT r FROM Rubro r"),
    @NamedQuery(name = "Rubro.findByIdrubro", query = "SELECT r FROM Rubro r WHERE r.idrubro = :idrubro"),
    @NamedQuery(name = "Rubro.findByCodigo", query = "SELECT r FROM Rubro r WHERE r.codigo = :codigo"),
    @NamedQuery(name = "Rubro.findByNombre", query = "SELECT r FROM Rubro r WHERE r.nombre = :nombre"),
    @NamedQuery(name = "Rubro.findByTipo", query = "SELECT r FROM Rubro r WHERE r.tipo = :tipo ORDER BY r.nombre")
})
public class Rubro implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "idrubro", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer idrubro;
    @Column(name = "codigo", length = 50)
    private String codigo;
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;
    @Basic(optional = false)
    @Column(name = "tipo", nullable = false)
    private short tipo;
//    @OneToMany(mappedBy = "rubro")
//    private List<Cliente> clienteList;
//    @OneToMany(mappedBy = "rubro")
//    private List<Proveedor> proveedorList;

    public Rubro() {
    }

    public Rubro(Integer idrubro) {
        this.idrubro = idrubro;
    }

    public Rubro(Integer idrubro, String nombre, short tipo) {
        this.idrubro = idrubro;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Integer getIdrubro() {
        return idrubro;
    }

    public void setIdrubro(Integer idrubro) {
        this.idrubro = idrubro;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public short getTipo() {
        return tipo;
    }

    public void setTipo(short tipo) {
        this.tipo = tipo;
    }

//    public List<Cliente> getClienteList() {
//        return clienteList;
//    }

//    public void setClienteList(List<Cliente> clienteList) {
//        this.clienteList = clienteList;
//    }

//    public List<Proveedor> getProveedorList() {
//        return proveedorList;
//    }

//    public void setProveedorList(List<Proveedor> proveedorList) {
//        this.proveedorList = proveedorList;
//    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idrubro != null ? idrubro.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Rubro)) {
            return false;
        }
        Rubro other = (Rubro) object;
        if ((this.idrubro == null && other.idrubro != null) || (this.idrubro != null && !this.idrubro.equals(other.idrubro))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getNombre();
    }

}

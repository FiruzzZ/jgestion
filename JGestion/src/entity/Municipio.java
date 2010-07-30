package entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "municipio", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"iddepto", "nombre"})
})
@NamedQueries({
    @NamedQuery(name = "Municipio.findAll", query = "SELECT m FROM Municipio m ORDER BY m.iddepto.nombre, m.nombre"),
    @NamedQuery(name = "Municipio.findById", query = "SELECT m FROM Municipio m WHERE m.id = :id"),
    @NamedQuery(name = "Municipio.findByNombre", query = "SELECT m FROM Municipio m WHERE m.nombre = :nombre"),
    @NamedQuery(name = "Municipio.findByCodigo", query = "SELECT m FROM Municipio m WHERE m.codigo = :codigo"),
    @NamedQuery(name = "Municipio.findByDepto", query = "SELECT m FROM Municipio m  WHERE m.iddepto.iddepto = :iddepto ORDER BY m.nombre")
})

public class Municipio implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 2147483647)
    private String nombre;
    @Column(name = "codigo", length = 2147483647)
    private String codigo;
    @OneToMany(mappedBy = "municipio")
    private List<Sucursal> sucursalList;
    @JoinColumn(name = "iddepto", referencedColumnName = "iddepto", nullable = false)
    @ManyToOne(optional = false)
    private Depto iddepto;
    @OneToMany(mappedBy = "municipio")
    private List<Proveedor> proveedorList;

    public Municipio() {   }

    public Municipio(Integer id) {
        this.id = id;
    }

    public Municipio(Integer id, String nombre) {
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public List<Sucursal> getSucursalList() {
        return sucursalList;
    }

    public void setSucursalList(List<Sucursal> sucursalList) {
        this.sucursalList = sucursalList;
    }

    public Depto getIddepto() {
        return iddepto;
    }

    public void setDepto(Depto iddepto) {
        this.iddepto = iddepto;
    }

    public List<Proveedor> getProveedorList() {
        return proveedorList;
    }

    public void setProveedorList(List<Proveedor> proveedorList) {
        this.proveedorList = proveedorList;
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
        if (!(object instanceof Municipio)) {
            return false;
        }
        Municipio other = (Municipio) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getNombre();
    }

}

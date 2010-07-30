package entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "contribuyente", uniqueConstraints = {@UniqueConstraint(columnNames = {"nombre"})})
@NamedQueries({
    @NamedQuery(name = "Contribuyente.findAll", query = "SELECT c FROM Contribuyente c"),
    @NamedQuery(name = "Contribuyente.findById", query = "SELECT c FROM Contribuyente c WHERE c.id = :id"),
    @NamedQuery(name = "Contribuyente.findByNombre", query = "SELECT c FROM Contribuyente c WHERE c.nombre = :nombre")
})

public class Contribuyente implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
//    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 2147483647)
    private String nombre;
    @Basic(optional = false)
    @Column(name = "factu_a", nullable = false)
    private boolean factuA;
    @Basic(optional = false)
    @Column(name = "factu_b", nullable = false)
    private boolean factuB;
    @Basic(optional = false)
    @Column(name = "factu_c", nullable = false)
    private boolean factuC;
    @Basic(optional = false)
    @Column(name = "factu_m", nullable = false)
    private boolean factuM;
    @Basic(optional = false)
    @Column(name = "factu_x", nullable = false)
    private boolean factuX;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contribuyente", fetch=FetchType.LAZY)
    private List<Cliente> clienteList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contribuyente", fetch=FetchType.LAZY)
    private List<Proveedor> proveedorList;

    public Contribuyente() {
    }

    public Contribuyente(Integer id) {
        this.id = id;
    }

    public Contribuyente(Integer id, String nombre, boolean factuA, boolean factuB, boolean factuC, boolean factuM, boolean factuX) {
        this.id = id;
        this.nombre = nombre;
        this.factuA = factuA;
        this.factuB = factuB;
        this.factuC = factuC;
        this.factuM = factuM;
        this.factuX = factuX;
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

    public boolean getFactuA() {
        return factuA;
    }

    public void setFactuA(boolean factuA) {
        this.factuA = factuA;
    }

    public boolean getFactuB() {
        return factuB;
    }

    public void setFactuB(boolean factuB) {
        this.factuB = factuB;
    }

    public boolean getFactuC() {
        return factuC;
    }

    public void setFactuC(boolean factuC) {
        this.factuC = factuC;
    }

    public boolean getFactuM() {
        return factuM;
    }

    public void setFactuM(boolean factuM) {
        this.factuM = factuM;
    }

    public boolean getFactuX() {
        return factuX;
    }

    public void setFactuX(boolean factuX) {
        this.factuX = factuX;
    }

    public List<Cliente> getClienteList() {
        return clienteList;
    }

    public void setClienteList(List<Cliente> clienteList) {
        this.clienteList = clienteList;
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
        if (!(object instanceof Contribuyente)) {
            return false;
        }
        Contribuyente other = (Contribuyente) object;
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

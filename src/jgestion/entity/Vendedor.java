package jgestion.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 *
 * @author FiruzzZ
 */
@Entity
public class Vendedor implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(length = 30, nullable = false)
    private String apellido;
    @Basic(optional = false)
    @Column(length = 50, nullable = false)
    private String nombre;
    @Basic(optional = false)
    @Column(length = 100)
    private String direccion;
    @Column(name = "tele1")
    private Long tele1;
    @Column(name = "tele2")
    private Long tele2;
    @Column(name = "email", length = 50)
    private String email;
    @Column(length = 200)
    private String observacion;
    @OneToMany(mappedBy = "vendedor")
    private List<FacturaVenta> facturaVentas;
    @OneToMany(mappedBy = "vendedor")
    private List<Remito> remitos;
    private boolean activo;

    public Vendedor() {
    }

    public Vendedor(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Long getTele1() {
        return tele1;
    }

    public void setTele1(Long tele1) {
        this.tele1 = tele1;
    }

    public Long getTele2() {
        return tele2;
    }

    public void setTele2(Long tele2) {
        this.tele2 = tele2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<FacturaVenta> getFacturaVentas() {
        return facturaVentas;
    }

    public void setFacturaVentas(List<FacturaVenta> facturaVentas) {
        this.facturaVentas = facturaVentas;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public List<Remito> getRemitos() {
        return remitos;
    }

    public void setRemitos(List<Remito> remitos) {
        this.remitos = remitos;
    }

    public boolean getActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
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
        if (!(object instanceof Vendedor)) {
            return false;
        }
        Vendedor other = (Vendedor) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Vendedor{" + "id=" + id + ", apellido=" + apellido + ", nombre=" + nombre + ", direccion=" + direccion + ", tele1=" + tele1 + ", tele2=" + tele2 + ", email=" + email + ", observacion=" + observacion + '}';
    }
}

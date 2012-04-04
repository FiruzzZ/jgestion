package entity;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "Sucursal.findAll", query = "SELECT s FROM Sucursal s ORDER BY s.nombre"),
    @NamedQuery(name = "Sucursal.findById", query = "SELECT s FROM Sucursal s WHERE s.id = :id")
})
public class Sucursal implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, unique=true, length = 200)
    private String nombre;
    @Basic(optional = false)
    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;
    @Column(name = "tele1")
    private BigInteger tele1;
    @Column(name = "tele2")
    private BigInteger tele2;
    @Column(name = "estado")
    private Integer estado;
    @Column(name = "interno1")
    private Integer interno1;
    @Column(name = "interno2")
    private Integer interno2;
    @Column(name = "encargado", length = 200)
    private String encargado;
    @Column(name = "email", length = 60)
    private String email;
    @JoinColumn(name = "departamento", referencedColumnName = "iddepto", nullable = false)
    @ManyToOne(optional = false)
    private Departamento departamento;
    @JoinColumn(name = "municipio", referencedColumnName = "id")
    @ManyToOne
    private Municipio municipio;
    @JoinColumn(name = "provincia", referencedColumnName = "idprovincia", nullable = false)
    @ManyToOne(optional = false)
    private Provincia provincia;
    @Basic(optional = false)
    @Column(nullable = false, unique = true)
    private Long puntoVenta;

    public Sucursal() {
    }

    public Sucursal(Integer id) {
        this.id = id;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public BigInteger getTele1() {
        return tele1;
    }

    public void setTele1(BigInteger tele1) {
        this.tele1 = tele1;
    }

    public BigInteger getTele2() {
        return tele2;
    }

    public void setTele2(BigInteger tele2) {
        this.tele2 = tele2;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public Integer getInterno1() {
        return interno1;
    }

    public void setInterno1(Integer interno1) {
        this.interno1 = interno1;
    }

    public Integer getInterno2() {
        return interno2;
    }

    public void setInterno2(Integer interno2) {
        this.interno2 = interno2;
    }

    public String getEncargado() {
        return encargado;
    }

    public void setEncargado(String encargado) {
        this.encargado = encargado;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }

    public Long getPuntoVenta() {
        return puntoVenta;
    }

    public void setPuntoVenta(Long puntoVenta) {
        this.puntoVenta = puntoVenta;
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
        if (!(object instanceof Sucursal)) {
            return false;
        }
        Sucursal other = (Sucursal) object;
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

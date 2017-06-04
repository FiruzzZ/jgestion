package jgestion.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "banco_sucursal", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre", "banco"})})
@NamedQueries({
    @NamedQuery(name = "BancoSucursal.findAll", query = "SELECT b FROM BancoSucursal b"),
    @NamedQuery(name = "BancoSucursal.findById", query = "SELECT b FROM BancoSucursal b WHERE b.id = :id"),
    @NamedQuery(name = "BancoSucursal.findByNombre", query = "SELECT b FROM BancoSucursal b WHERE b.nombre = :nombre")})
public class BancoSucursal implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;
    @Column(name = "codigo", length = 20)
    private String codigo;
    @Basic(optional = false)
    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;
    @Column(name = "telefono")
    private Long telefono;
    @JoinColumn(name = "banco", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Banco banco;

    public BancoSucursal() {
    }

    public BancoSucursal(Integer id) {
        this.id = id;
    }

    public BancoSucursal(Integer id, String nombre, String codigo, String direccion, Long telefono, Banco banco) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.direccion = direccion;
        this.telefono = telefono;
        this.banco = banco;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Long getTelefono() {
        return telefono;
    }

    public void setTelefono(Long telefono) {
        this.telefono = telefono;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
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
        if (!(object instanceof BancoSucursal)) {
            return false;
        }
        BancoSucursal other = (BancoSucursal) object;
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

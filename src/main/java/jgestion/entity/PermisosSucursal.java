package jgestion.entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = {"usuario_id", "sucursal_id"}))
public class PermisosSucursal implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Usuario usuario;
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Sucursal sucursal;

    public PermisosSucursal() {
        //orm
    }

    public PermisosSucursal(Usuario usuario, Sucursal sucursal) {
        this.usuario = usuario;
        this.sucursal = sucursal;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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
        if (!(object instanceof PermisosSucursal)) {
            return false;
        }
        PermisosSucursal other = (PermisosSucursal) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PermisosSucursal{" + "id=" + id + ", usuario=" + usuario.getId() + ", sucursal=" + sucursal.getId() + '}';
    }
}

package entity;

import controller.CuentaBancaria;
import entity.enums.ChequeEstado;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "cheque_propio")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ChequePropio.findAll", query = "SELECT c FROM ChequePropio c"),
    @NamedQuery(name = "ChequePropio.findById", query = "SELECT c FROM ChequePropio c WHERE c.id = :id")
})
public class ChequePropio extends Cheque implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @JoinColumn(name = "proveedor", nullable = false)
    @ManyToOne(optional = false)
    private Proveedor proveedor;
    @JoinColumn(name = "cuentabancaria_id", nullable = false)
    @ManyToOne(optional = false)
    private CuentaBancaria cuentabancaria;

    public ChequePropio() {
    }

    public ChequePropio(Proveedor proveedor, Long numero, Banco banco, BancoSucursal bancoSucursal, BigDecimal importe, Date fechaCheque, Date fechaCobro, boolean cruzado, String observacion, ChequeEstado chequeEstado, String endosatario, Date fechaEndoso, Usuario usuario, Librado librado, CuentaBancaria cuentabancaria) {
        super(numero, banco, bancoSucursal, importe, fechaCheque, fechaCobro, cruzado, observacion, chequeEstado, endosatario, fechaEndoso, usuario, librado);
        this.proveedor = proveedor;
        this.cuentabancaria = cuentabancaria;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean getCruzado() {
        return cruzado;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public CuentaBancaria getCuentabancaria() {
        return cuentabancaria;
    }

    public void setCuentabancaria(CuentaBancaria cuentabancaria) {
        this.cuentabancaria = cuentabancaria;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ChequePropio)) {
            return false;
        }
        ChequePropio other = (ChequePropio) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return super.equals(object);
    }
}

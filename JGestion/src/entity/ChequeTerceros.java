package entity;

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
@Table(name = "cheque_terceros", uniqueConstraints = {})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ChequeTerceros.findAll", query = "SELECT c FROM ChequeTerceros c"),
    @NamedQuery(name = "ChequeTerceros.findById", query = "SELECT c FROM ChequeTerceros c WHERE c.id = :id")
})
public class ChequeTerceros extends Cheque implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @JoinColumn(name = "cliente", nullable = false)
    @ManyToOne(optional = false)
    private Cliente cliente;

    public ChequeTerceros() {
    }

    public ChequeTerceros(Integer id) {
        this.id = id;
    }

    public ChequeTerceros(Cliente cliente, Long numero, Banco banco, BancoSucursal bancoSucursal, BigDecimal importe, Date fechaCheque, Date fechaCobro, boolean cruzado, String observacion, ChequeEstado chequeEstado, String endosatario, Date fechaEndoso, Usuario usuario, Librado librado) {
        super(numero, banco, bancoSucursal, importe, fechaCheque, fechaCobro, cruzado, observacion, chequeEstado, endosatario, fechaEndoso, usuario, librado);
        this.cliente = cliente;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        super.equals(object);
        System.out.println(getClass() + ".equals()");
        if (!(object instanceof ChequeTerceros)) {
            return false;
        }
        ChequeTerceros other = (ChequeTerceros) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        } else if ((!this.getBanco().equals(other.getBanco())) || (!this.getNumero().equals(other.getNumero()))) {
            return false;
        }
        return true;
    }
}

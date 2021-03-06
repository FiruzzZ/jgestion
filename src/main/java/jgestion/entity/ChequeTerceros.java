package jgestion.entity;

import jgestion.entity.enums.ChequeEstado;
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
@Table(name = "cheque_terceros")
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
    @JoinColumn(name = "cliente")//, nullable = false)
    @ManyToOne//(optional = false)
    private Cliente cliente;

    public ChequeTerceros() {
    }

    public ChequeTerceros(Integer id) {
        this.id = id;
    }

    public ChequeTerceros(Integer id, Cliente cliente, Long numero, Banco banco, BancoSucursal bancoSucursal, BigDecimal importe, Date fechaCheque, Date fechaCobro, boolean cruzado, String observacion, ChequeEstado chequeEstado, String endosatario, Date fechaEndoso, Usuario usuario) {
        super(numero, banco, bancoSucursal, importe, fechaCheque, fechaCobro, cruzado, observacion, chequeEstado, endosatario, fechaEndoso, usuario);
        this.id = id;
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
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChequeTerceros other = (ChequeTerceros) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
}

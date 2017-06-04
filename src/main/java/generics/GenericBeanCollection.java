package generics;

/**
 * Clase usada para enviar como dataSource a los reportes, cada atributo representa un dato de una
 * columna y desde los jasperreport se los llama por reflexión
 * @author FiruzzZ
 */
public class GenericBeanCollection {

    private Object o1;
    private Object o2;
    private Object o3;
    private Object o4;
    private Object o5;
    private Object o6;
    private Object o7;
    private Object o8;
    private Object o9;
    private Object o10;
    private Object o11;
    private Object o12;
    private Object o13;
    private Object o14;
    private Object o15;
    private Object o16;
    private Object o17;

    public GenericBeanCollection(Object... o) {
        if (o.length >0) o1 = o[0];
        if (o.length >1) o2 = o[1];
        if (o.length >2) o3 = o[2];
        if (o.length >3) o4 = o[3];
        if (o.length >4) o5 = o[4];
        if (o.length >5) o6 = o[5];
        if (o.length >6) o7 = o[6];
        if (o.length >7) o8 = o[7];
        if (o.length >8) o9 = o[8];
        if (o.length >9) o10 = o[9];
        if (o.length >10) o11 = o[10];
        if (o.length >11) o12 = o[11];
        if (o.length >12) o13 = o[12];
        if (o.length >13) o14 = o[13];
        if (o.length >14) o15 = o[14];
        if (o.length >15) o16 = o[15];
        if (o.length >16) o17 = o[16];
    }

    public Object getO1() {
        return o1;
    }

    public void setO1(Object o1) {
        this.o1 = o1;
    }

    public Object getO2() {
        return o2;
    }

    public void setO2(Object o2) {
        this.o2 = o2;
    }

    public Object getO3() {
        return o3;
    }

    public void setO3(Object o3) {
        this.o3 = o3;
    }

    public Object getO4() {
        return o4;
    }

    public void setO4(Object o4) {
        this.o4 = o4;
    }

    public Object getO5() {
        return o5;
    }

    public void setO5(Object o5) {
        this.o5 = o5;
    }

    public Object getO6() {
        return o6;
    }

    public void setO6(Object o6) {
        this.o6 = o6;
    }

    public Object getO7() {
        return o7;
    }

    public void setO7(Object o7) {
        this.o7 = o7;
    }

    public Object getO8() {
        return o8;
    }

    public void setO8(Object o8) {
        this.o8 = o8;
    }

    public Object getO9() {
        return o9;
    }

    public void setO9(Object o9) {
        this.o9 = o9;
    }

    public Object getO10() {
        return o10;
    }

    public void setO10(Object o10) {
        this.o10 = o10;
    }

    public Object getO11() {
        return o11;
    }

    public void setO11(Object o11) {
        this.o11 = o11;
    }

    public Object getO12() {
        return o12;
    }

    public void setO12(Object o12) {
        this.o12 = o12;
    }

    public Object getO13() {
        return o13;
    }

    public void setO13(Object o13) {
        this.o13 = o13;
    }

    public Object getO14() {
        return o14;
    }

    public void setO14(Object o14) {
        this.o14 = o14;
    }

    public Object getO15() {
        return o15;
    }

    public Object getO16() {
        return o16;
    }

    public Object getO17() {
        return o17;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.o1 != null ? this.o1.hashCode() : 0);
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
        final GenericBeanCollection other = (GenericBeanCollection) obj;
        if (this.o1 != other.o1 && (this.o1 == null || !this.o1.equals(other.o1))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GenericBeanCollection{" + "o1=" + o1 + ", o2=" + o2 + ", o3=" + o3 + ", o4=" + o4 + ", o5=" + o5 + ", o6=" + o6 + ", o7=" + o7 + ", o8=" + o8 + ", o9=" + o9 + ", o10=" + o10 + ", o11=" + o11 + ", o12=" + o12 + '}';
    }
}

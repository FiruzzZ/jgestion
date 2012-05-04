package generics;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import utilities.gui.SwingUtil;

/**
 *
 * @author Administrador
 */
public class CurrencyFormatterTextFieldListener extends NumericFormatterTextFieldListener {

    private final DecimalFormat PRECIO_CON_PUNTO;

    public CurrencyFormatterTextFieldListener() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        PRECIO_CON_PUNTO = new DecimalFormat("#######0.00", simbolos);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        SwingUtil.checkInputDigit(e, true);
    }

    @Override
    protected String format(String text) {
        BigDecimal a = new BigDecimal(text.isEmpty() ? "0" : text);
        return PRECIO_CON_PUNTO.format(a);
    }
}

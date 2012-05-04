package generics;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import javax.swing.text.JTextComponent;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;

/**
 *
 * @author Administrador
 */
public class NumericFormatterTextFieldListener implements KeyListener, FocusListener {

    @Override
    public void keyTyped(KeyEvent e) {
        SwingUtil.checkInputDigit(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void focusGained(FocusEvent e) {
        JTextComponent jtc = (JTextComponent) e.getSource();
        jtc.setSelectionStart(0);
    }

    @Override
    public void focusLost(FocusEvent e) {
        JTextComponent jtc = (JTextComponent) e.getSource();
        String text = jtc.getText().trim();
        try {
            String formatted = format(text);
            jtc.setText(formatted);
            jtc.setForeground(null);
        } catch (Exception ex) {
            jtc.setForeground(Color.RED);
        }
    }

    protected String format(String text) {
        Integer a = new Integer(text.isEmpty() ? "0" : text);
        return a.toString();
    }
}

package generics.gui;

import com.toedter.calendar.JDateChooser;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

/**
 *
 * @author FiruzzZ
 */
public class GroupLayoutPanelBuilder {

    private static final long serialVersionUID = 1L;
    private final JPanel panel = new JPanel();
    private JLabel infoLabel = new JLabel();
    private List<JLabel> labelList;
    private List<Component> componentList;
    private int maxSize;

    public GroupLayoutPanelBuilder() {
        this(new ArrayList<JLabel>(), new ArrayList<Component>());
    }

    public GroupLayoutPanelBuilder(List<JLabel> labelList, List<Component> componentList) {
        //        infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rsc/cancel.png")));
        this.labelList = labelList;
        this.componentList = componentList;
    }

    public int addFormItem(String labelText, Component c) {
        return addFormItem(new JLabel(labelText), c);
    }

    public int addFormItem(JLabel l, Component c) {
        labelList.add(l);
        componentList.add(c);
        return labelList.size();
    }

    public JPanel getPanel() {
        return panel;
    }

    public JLabel getInfoLabel() {
        return infoLabel;
    }

    public JPanel build() {
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        //agregar las labels acá
        GroupLayout.ParallelGroup labelsParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        for (JLabel l : labelList) {
            labelsParallelGroup.addComponent(l);
        }
        //agregar los components acá
        GroupLayout.ParallelGroup horizontalComponentsParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        for (Component c : componentList) {
            if (maxSize > 0) {
                horizontalComponentsParallelGroup.addComponent(c, GroupLayout.PREFERRED_SIZE, maxSize, GroupLayout.PREFERRED_SIZE);
//                } else {
//                    horizontalComponentsParallelGroup.addComponent(c, GroupLayout.PREFERRED_SIZE, maxSize, GroupLayout.PREFERRED_SIZE);
            } else {
                if (c instanceof JComboBox) {
                    horizontalComponentsParallelGroup.addComponent(c, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
                } else if (c instanceof JDateChooser) {
                    horizontalComponentsParallelGroup.addComponent(c, 120, 120, GroupLayout.PREFERRED_SIZE);
                } else {
                    horizontalComponentsParallelGroup.addComponent(c);
                }
            }
        }
        GroupLayout.ParallelGroup horizontalGroup =
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(infoLabel, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup().addGap(20, 20, 20).addGroup(labelsParallelGroup).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(horizontalComponentsParallelGroup)))
                .addContainerGap());
        layout.setHorizontalGroup(horizontalGroup);

        //VERTICAL SHIT.............
        GroupLayout.SequentialGroup verticalSubGroup = layout.createSequentialGroup().addContainerGap(); // gap al inicio
        for (int i = 0; i < labelList.size(); i++) {
            JLabel l = labelList.get(i);
            Component c = componentList.get(i);
            //addGroup por cada (label + component)
            verticalSubGroup.addGroup(layout.createParallelGroup(c instanceof JDateChooser ? GroupLayout.Alignment.CENTER : GroupLayout.Alignment.BASELINE)
                    //cada label 
                    .addComponent(l)
                    //con su componenete
                    .addComponent(c, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED); //entre cada (label+component) va uno de estos para el espacio!!

        }
        verticalSubGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(infoLabel);
        // gap al final
        verticalSubGroup.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

        GroupLayout.ParallelGroup verticalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(verticalSubGroup);
        layout.setVerticalGroup(verticalGroup);
        return panel;
    }

    public void setComponentsMaximumSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public static void main(String[] args) {
        List<JLabel> labelList = new ArrayList<>();
        labelList.add(new JLabel("label n° 1"));
        labelList.add(new JLabel("label n°nnnnnn 2"));
        labelList.add(new JLabel("label n°nnnnnn 3333333"));
        labelList.add(new JLabel("label n°nnnnnn "));
        List<Component> componentList = new ArrayList<>();
        componentList.add(new JTextField("compinche de label 1"));
        componentList.add(new JComboBox());
        componentList.add(new JDateChooser());
        componentList.add(new JComboBox(new Object[]{"adfsawsfawefaadsfafdsdafsafaf", "Posadas misiones de la república arg", "marilin mia!"}));
        GroupLayoutPanelBuilder glp = new GroupLayoutPanelBuilder(labelList, componentList);
//        glp.setComponentsMaximumSize(100);
        glp.build();
        JPanel panel = glp.getPanel();
        JFrame frame = new JFrame("Group !!!");
        frame.getContentPane().add(panel);
//        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

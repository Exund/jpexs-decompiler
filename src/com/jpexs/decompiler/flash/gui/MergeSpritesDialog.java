/*
 *  Copyright (C) 2010-2023 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

/**
 *
 * @author JPEXS
 */
public class MergeSpritesDialog extends AppDialog {

    private final JButton okButton = new JButton(translate("button.ok"));

    private final JButton cancelButton = new JButton(translate("button.cancel"));
    
    private final JList<DefineSpriteTag> priorityList;
    
    private final DefaultListModel<DefineSpriteTag> sprites;

    private final JComboBox<ComboBoxItem<ResolutionMethod>> resolutionMethodComboBox;
    
    private final JCheckBox compactCheckBox;

    private final JCheckBox replaceCheckBox;

    private final JCheckBox deleteCheckBox;

    private int result = ERROR_OPTION;

    public MergeSpritesDialog(Window owner) {
        super(owner);
        setSize(400, 150);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        Container cnt = getContentPane();
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.Y_AXIS));
        
        cnt.add(new JLabel(translate("merge.resolutionMethod")));
        resolutionMethodComboBox = new JComboBox<>();
        for(ResolutionMethod rm : ResolutionMethod.values()) {
            resolutionMethodComboBox.addItem(new ComboBoxItem<>(translate("resolution." +  rm.toString()), rm));
        }
        cnt.add(resolutionMethodComboBox);
        
        compactCheckBox = new JCheckBox(translate("merge.compact"));
        cnt.add(compactCheckBox);
        
        replaceCheckBox = new JCheckBox(translate("merge.replace"));
        replaceCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCheckBox.setEnabled(replaceCheckBox.isSelected());
                if(!deleteCheckBox.isEnabled()) {
                    deleteCheckBox.setSelected(false);
                }
            }
            
        });
        cnt.add(replaceCheckBox);
        
        deleteCheckBox = new JCheckBox(translate("merge.delete"));
        deleteCheckBox.setEnabled(false);
        cnt.add(deleteCheckBox);
        
        cnt.add(new JLabel(translate("merge.priority")));      
        sprites = new DefaultListModel<>();
        
        priorityList = new JList<>(sprites);
        priorityList.setDragEnabled(true);
        priorityList.setDropMode(DropMode.INSERT);
        priorityList.setVisibleRowCount(7);
        priorityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        priorityList.setTransferHandler(new TransferHandler() {
            private int index;
            private boolean beforeIndex = false; //Start with `false` therefore if it is removed from or added to the list it still works
            private final DataFlavor localFlavor = new DataFlavor(DefineSpriteTag.class, "DefineSpiteTag");
            
            
            @Override
            public int getSourceActions(JComponent comp) {
                return MOVE;
            }

            @Override
            public Transferable createTransferable(JComponent comp) {
                index = priorityList.getSelectedIndex();
                Object data = priorityList.getSelectedValue();
                return new Transferable() {
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[] { localFlavor };
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return Objects.equals(flavor, localFlavor);
                    }

                    @Override
                    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
                        return null;
                    }
                };
            }

            @Override
            public void exportDone(JComponent comp, Transferable trans, int action) {
                if (action == MOVE) {
                    if(beforeIndex) {
                        sprites.remove(index + 1);
                    } else {
                        sprites.remove(index);
                    }
                }
            }

            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(localFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                int newIndex = dl.getIndex();
                sprites.add(newIndex, sprites.get(index));
                beforeIndex = newIndex < index;
                return true;
            }
        });
          
        JScrollPane listScroller = new FasterScrollPane(priorityList);
        listScroller.setPreferredSize(new Dimension(400, 200));
        cnt.add(listScroller);
        
        JPanel panButtons = new JPanel(new FlowLayout());
        okButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        panButtons.add(okButton);
        panButtons.add(cancelButton);

        add(panButtons, BorderLayout.SOUTH);

        setModalityType(ModalityType.APPLICATION_MODAL);
        View.setWindowIcon(this);
        setTitle(translate("dialog.title"));
        getRootPane().setDefaultButton(okButton);
        pack();
        View.centerScreen(this);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        result = OK_OPTION;
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public List<DefineSpriteTag> getOrder() {
        if (result == ERROR_OPTION) {
            return null;
        }
        
        List<DefineSpriteTag> result = new ArrayList<>();
        
        for(int i = 0; i < sprites.size(); i++) {
            result.add(sprites.elementAt(i));
        }

        return result;
    }
    
    public ResolutionMethod getResolutionMethod() {
        @SuppressWarnings("unchecked")
        ComboBoxItem<ResolutionMethod> item = (ComboBoxItem<ResolutionMethod>)resolutionMethodComboBox.getSelectedItem();
        return item.getValue();
    }
    
    public boolean getCompactDepths() {
        return compactCheckBox.isSelected();
    } 
    
    public boolean getReplace() {
        return replaceCheckBox.isSelected();
    }
    
    public boolean getDelete() {
        return deleteCheckBox.isSelected();
    }

    public int showDialog(Collection<DefineSpriteTag> sprites) {
        this.sprites.clear();
        this.sprites.addAll(sprites);
        priorityList.setVisibleRowCount(7);
        setVisible(true);
        return result;
    }
    
    public enum ResolutionMethod {
        Override,
        //InterleaveDepths,
        Stack,
        None
    }
}

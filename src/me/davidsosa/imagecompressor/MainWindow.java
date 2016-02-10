/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 David Sosa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.davidsosa.imagecompressor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Main window for the Image compressor software.
 *
 * @author David Sosa
 */
public class MainWindow extends JFrame {

    private final JFileChooser fileChooser;
    private final JLabel pathLabel;
    private final JLabel errorLabel;
    private final JSpinner qualitySpinner;
    private final JCheckBox replaceOriginalsCheckBox;
    private HelpWindow helpWindow;
    private final ImageCompressor imageCompressor;
    private final JButton compressButton;

    public MainWindow() throws HeadlessException {
        super("Image Compressor");
        assert false;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(400, 200);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openMenuItem = new JMenuItem("Open...");
        JMenuItem helpItem = new JMenuItem("Help");

        helpItem.addActionListener(new HelpListener());
        openMenuItem.addActionListener(new OpenListener(this));

        fileMenu.add(openMenuItem);
        menuBar.add(fileMenu);
        menuBar.add(helpItem);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.05);
        qualitySpinner = new JSpinner(spinnerModel);
        menuBar.add(qualitySpinner);

        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a folder");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        pathLabel = new JLabel("Selected folder: " + fileChooser.getCurrentDirectory());
        errorLabel = new JLabel();

        replaceOriginalsCheckBox = new JCheckBox("Replace original images");

        compressButton = new JButton("Compress");
        compressButton.addActionListener(new CompressButtonListener());

        JPanel centerPanel = new JPanel();
        ((FlowLayout) centerPanel.getLayout()).setAlignment(FlowLayout.CENTER);
        centerPanel.add(pathLabel);
        centerPanel.add(replaceOriginalsCheckBox);
        centerPanel.add(errorLabel);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(menuBar, BorderLayout.NORTH);
        getContentPane().add(compressButton, BorderLayout.SOUTH);

        imageCompressor = new ImageCompressor();
    }

    public static void main(String[] args) {
        new MainWindow().setVisible(true);
    }

    /**
     * Listener for the button that compresses the images. The compression of
     * the images takes place in a second thread.
     *
     * @author David Sosa
     */
    private class CompressButtonListener implements ActionListener, Runnable {

        @Override
        public void actionPerformed(ActionEvent e) {
            Thread compressThread = new Thread(this, "ImageCompressingThread");
            compressThread.start();
        }

        @Override
        public void run() {
            try {
                compressButton.setEnabled(false);
                errorLabel.setText("Compressing images...");
                String directoryPath;
                if (fileChooser.getSelectedFile() != null) {
                    directoryPath = fileChooser.getSelectedFile().getAbsolutePath();
                } else {
                    directoryPath = fileChooser.getCurrentDirectory().getAbsolutePath();
                }
                imageCompressor.compressImagesInDirectory(directoryPath, ((Double) qualitySpinner.getValue()).floatValue(), replaceOriginalsCheckBox.isSelected());
                errorLabel.setText("Image compression complete.");
            } catch (IOException | UnsupportedOperationException ex) {
                errorLabel.setText("Error: " + ex.getMessage());
            } finally {
                compressButton.setEnabled(true);
            }
        }
    }

    /**
     * Listener for a button that opens the help window.
     *
     * @author David Sosa
     */
    private class HelpListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (helpWindow == null) {
                helpWindow = new HelpWindow();
            }
            helpWindow.setVisible(true);
        }
    }

    /**
     * Listener for a button that opens a JDialog to choose a directory where
     * the images to be compressed are stored.
     *
     * @author David Sosa
     */
    private class OpenListener implements ActionListener {

        private final Component parent;

        private OpenListener(Component parent) {
            this.parent = parent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = fileChooser.showOpenDialog(parent);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File directory = fileChooser.getSelectedFile();
                pathLabel.setText("Selected folder: " + directory.getAbsolutePath());
                System.out.println("Selected folder: " + directory.getAbsolutePath());
            }
        }
    }
}

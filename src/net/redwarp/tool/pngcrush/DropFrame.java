/*
 * Copryright (C) 2012 Redwarp
 * 
 * This file is part of PNGCrush Wrapper.
 * PNGCrush Wrapper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * PNGCrush Wrapper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with PNGCrush Wrapper.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.redwarp.tool.pngcrush;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import net.iharder.dnd.FileDrop;

public class DropFrame extends JFrame {
	JTextArea console;
	JButton arrow;
	ExecutorService service = Executors.newSingleThreadExecutor();
	JFileChooser fileChooser;
	ImageIcon blueArrow;
	ImageIcon redArrow;
	
	public DropFrame() {
		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "Folders and PNG files";
			}
			
			@Override
			public boolean accept(File arg0) {
				return (arg0.isDirectory() || arg0.getName().endsWith(".png"));
			}
		});
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		blueArrow = new ImageIcon(DropFrame.class.getResource("/img/blue-go-down-th.png"));
		redArrow = new ImageIcon(DropFrame.class.getResource("/img/red-go-down-th.png"));
		
		setResizable(false);
		setSize(new Dimension(600, 200));
		setPreferredSize(new Dimension(100, 100));
		setIconImage(Toolkit.getDefaultToolkit().getImage(DropFrame.class.getResource("/img/blue-go-down-th.png")));

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel dropZone = new JPanel();
		dropZone.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		dropZone.setPreferredSize(new Dimension(200, 200));
		getContentPane().add(dropZone, BorderLayout.WEST);
		dropZone.setLayout(new GridLayout(0, 1, 0, 0));

		arrow = new JButton("");
		arrow.setPressedIcon(redArrow);
		arrow.setFocusPainted(false);
		arrow.setBorderPainted(false);
		arrow.setBorder(null);
		arrow.setIcon(blueArrow);
		arrow.setSelectedIcon(redArrow);
		arrow.setContentAreaFilled(false);
		arrow.setHorizontalAlignment(SwingConstants.CENTER);
		arrow.setHorizontalTextPosition(SwingConstants.CENTER);
		arrow.setAlignmentX(Component.CENTER_ALIGNMENT);
		arrow.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		
		arrow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				chooseFile();
			}
		});

		dropZone.add(arrow);

		JPanel rightPanel = new JPanel();
		getContentPane().add(rightPanel, BorderLayout.CENTER);
		rightPanel.setLayout(new BorderLayout(0, 0));

		console = new JTextArea(0, 0);
		console.setLineWrap(true);
		console.setEditable(false);
		
		JScrollPane scrollPane = new JScrollPane(console);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		rightPanel.add(scrollPane);
		

		new FileDrop<JPanel>(dropZone, null, new FileDrop.Listener<JPanel>() {

			@Override
			public void filesDropped(JPanel source, File[] files) {
				handleFileList(files);
			}

			@Override
			public void dragEnter(JPanel source) {
				arrow.setSelected(true);
			}

			@Override
			public void dragExit(JPanel source) {
				arrow.setSelected(false);
			}
		});

		bruteForce = new JCheckBox("Brute force");
		bruteForce.setSelected(true);
		getContentPane().add(bruteForce, BorderLayout.SOUTH);
		
		menuBar = new JMenuBar();
		getContentPane().add(menuBar, BorderLayout.NORTH);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		menuOpen = new JMenuItem("Open...");
		menuOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooseFile();
			}
		});
		mnFile.add(menuOpen);
	}

	private static final long serialVersionUID = 2909819674605164461L;
	private JCheckBox bruteForce;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem menuOpen;
	
	private void crushPNGFile(File file){
		PNGCrusher crusher = new PNGCrusher(file,
				bruteForce.isSelected()) {
			protected void process(java.util.List<String> chunks) {
				for (String string : chunks) {
					console.append(string);
				}
			};
		};
		service.submit(crusher);
	}
	
	private void handleFileList(File[] fileList){
		for (File file : fileList) {
			if (file.getName().endsWith(".png")) {
				crushPNGFile(file);						
			} else if(file.isDirectory()){
				File[] list = file.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File file, String name) {
						return name.endsWith(".png");
					}
				});
				for(File subFile : list){
					crushPNGFile(subFile);
				}
			}
		}
	}
	
	private void chooseFile(){
		int returnCode = fileChooser.showOpenDialog(DropFrame.this);
		
		if(returnCode == JFileChooser.APPROVE_OPTION){
			File[] fileList = fileChooser.getSelectedFiles();
			handleFileList(fileList);
		}
	}
}

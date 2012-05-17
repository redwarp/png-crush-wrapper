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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import net.iharder.dnd.FileDrop;

public class DropFrame extends JFrame {
	JButton arrow;
	// ExecutorService service =
	// Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	ControlledExecutorService service = new ControlledExecutorService();
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

		blueArrow = new ImageIcon(
				DropFrame.class.getResource("/img/blue-go-down-th.png"));
		redArrow = new ImageIcon(
				DropFrame.class.getResource("/img/red-go-down-th.png"));

		setResizable(false);
		setSize(new Dimension(600, 300));
		setPreferredSize(new Dimension(100, 100));
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				DropFrame.class.getResource("/img/blue-go-down-th.png")));
		setLocation(50, 50);

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

		table = new JTable();
		table.setRowSelectionAllowed(false);
		model = new ResultTableModel();
		table.setModel(model);

		table.getColumnModel().getColumn(0).setPreferredWidth(200);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
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

		panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		bruteForce = new JCheckBox("Brute force");
		panel.add(bruteForce);
		bruteForce.setEnabled(false);
		bruteForce.setSelected(true);

		horizontalGlue = Box.createHorizontalGlue();
		panel.add(horizontalGlue);

		appStatus = new JLabel("Waiting for files...");
		panel.add(appStatus);

		horizontalStrut = Box.createHorizontalStrut(20);
		panel.add(horizontalStrut);

		service.setTasksListener(new ControlledExecutorService.Listener() {

			@Override
			public void onTasksStart() {
				System.out.println("Started");
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						appStatus.setText("Crunching...");
					}
				});
			}

			@Override
			public void onTasksFinish() {
				System.out.println("Stopped");
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						appStatus.setText("Waiting for files...");
					}
				});
			}
		});
	}

	private static final long serialVersionUID = 2909819674605164461L;
	private JCheckBox bruteForce;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem menuOpen;
	private JTable table;
	private ResultTableModel model;
	private JPanel panel;
	private Component horizontalGlue;
	private JLabel appStatus;
	private Component horizontalStrut;

	private void crushPNGFile(OperationStatus status) {
		PNGCrusher crusher = new PNGCrusher(status, bruteForce.isSelected()) {
			protected void process(java.util.List<OperationStatus> chunks) {
				for (OperationStatus operation : chunks) {
					// console.append(string);
					model.notifyChange(operation);
				}
			};
		};
		service.submit(crusher);
	}

	private void handleFileList(File[] fileList) {
		for (File file : fileList) {
			if (file.getName().endsWith(".png")) {
				OperationStatus status = model.addFile(file);
				crushPNGFile(status);
			} else if (file.isDirectory()) {
				File[] list = file.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File file, String name) {
						return name.endsWith(".png");
					}
				});
				for (File subFile : list) {
					OperationStatus status = model.addFile(subFile);
					crushPNGFile(status);
				}
			}
		}
	}

	private void chooseFile() {
		int returnCode = fileChooser.showOpenDialog(DropFrame.this);

		if (returnCode == JFileChooser.APPROVE_OPTION) {
			File[] fileList = fileChooser.getSelectedFiles();
			handleFileList(fileList);
		}
	}
}

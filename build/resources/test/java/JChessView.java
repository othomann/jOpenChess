/*
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package main.java;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;

import main.java.game.Game;
import main.java.game.Settings;
import main.java.gui.ChooseThemeWindow;
import main.java.gui.GUI;
import main.java.gui.JChessAboutBox;
import main.java.gui.NewGameWindow;
import main.java.gui.PawnPromotionWindow;

/**
 * The application's main frame.
 */

public class JChessView extends FrameView implements ActionListener, ComponentListener {
	static GUI gui = null;

	public Game addNewTab(String title) {
		Game newGUI = new Game();
		this.gamesPane.addTab(title, newGUI);
		return newGUI;
	}

	public void actionPerformed(ActionEvent event) {
		Object target = event.getSource();
		if (target == newGameItem) {
			this.newGameFrame = new NewGameWindow();
			JChessApp.getApplication().show(this.newGameFrame);
		} else if (target == saveGameItem) { // saveGame
			if (this.gamesPane.getTabCount() == 0) {
				JOptionPane.showMessageDialog(null, Settings.lang("save_not_called_for_tab"));
				return;
			}
			while (true) {// until
				JFileChooser fc = new JFileChooser();
				int retVal = fc.showSaveDialog(this.gamesPane);
				if (retVal == JFileChooser.APPROVE_OPTION) {
					File selFile = fc.getSelectedFile();
					Game tempGUI = (Game) this.gamesPane.getComponentAt(this.gamesPane.getSelectedIndex());
					if (!selFile.exists()) {
						try {
							selFile.createNewFile();
						} catch (java.io.IOException exc) {
							
							LogToFile.log(exc, "ERROR", "Error creating file: ");
						}
					} else if (selFile.exists()) {
						int opt = JOptionPane.showConfirmDialog(tempGUI, Settings.lang("file_exists"),
								Settings.lang("file_exists"), JOptionPane.YES_NO_OPTION);
						if (opt == JOptionPane.NO_OPTION)// if user choose to
															// now overwrite
						{
							continue; // go back to file choose
						}
					}
					if (selFile.canWrite()) {
						tempGUI.saveGame(selFile);
					}
					// System.out.println(fc.getSelectedFile().isFile());
					LogToFile.log(null, "INFO", "fc.getSelectedFile().isFile() = " + fc.getSelectedFile().isFile());
					break;
				} else if (retVal == JFileChooser.CANCEL_OPTION) {
					break;
				}
				/// JChessView.gui.game.saveGame(fc.);
			}
		} else if (target == loadGameItem) { // loadGame
			JFileChooser fc = new JFileChooser();
			int retVal = fc.showOpenDialog(this.gamesPane);
			if (retVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists() && file.canRead()) {
					Game.loadGame(file);
				}
			}
		} else if (target == this.themeSettingsMenu) {
			try {
				ChooseThemeWindow choose = new ChooseThemeWindow(this.getFrame());
				JChessApp.getApplication().show(choose);
			} catch (Exception exc) {
				JOptionPane.showMessageDialog(JChessApp.getApplication().getMainFrame(), exc.getMessage());
				 System.out.println("Something wrong creating window - perhaps themeList is null");

				LogToFile.log(exc, "Error", "Something wrong creating window - perhaps themeList is null");
				exc.printStackTrace();
			}
		}
	}

	/// --endOf- don't delete, because they're interfaces for MouseEvent

	public JChessView(SingleFrameApplication app) {
		super(app);

		initComponents();
		// status bar initialization - message timeout, idle icon and busy
		// animation, etc
		ResourceMap resourceMap = getResourceMap();
		int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
		messageTimer = new Timer(messageTimeout, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusMessageLabel.setText("");
			}
		});
		messageTimer.setRepeats(false);
		int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
		for (int i = 0; i < busyIcons.length; i++) {
			busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
		}
		busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
				statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
			}
		});
		idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
		statusAnimationLabel.setIcon(idleIcon);
		progressBar.setVisible(false);

		// connecting action tasks to status bar via TaskMonitor
		TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
		taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent evt) {
				String propertyName = evt.getPropertyName();
				if ("started".equals(propertyName)) {
					if (!busyIconTimer.isRunning()) {
						statusAnimationLabel.setIcon(busyIcons[0]);
						busyIconIndex = 0;
						busyIconTimer.start();
					}
					progressBar.setVisible(true);
					progressBar.setIndeterminate(true);
				} else if ("done".equals(propertyName)) {
					busyIconTimer.stop();
					statusAnimationLabel.setIcon(idleIcon);
					progressBar.setVisible(false);
					progressBar.setValue(0);
				} else if ("message".equals(propertyName)) {
					String text = (String) (evt.getNewValue());
					statusMessageLabel.setText((text == null) ? "" : text);
					messageTimer.restart();
				} else if ("progress".equals(propertyName)) {
					int value = (Integer) (evt.getNewValue());
					progressBar.setVisible(true);
					progressBar.setIndeterminate(false);
					progressBar.setValue(value);
				}
			}
		});

	}

	@Action
	public void showAboutBox() {
		if (aboutBox == null) {
			JFrame mainFrame = JChessApp.getApplication().getMainFrame();
			aboutBox = new JChessAboutBox(mainFrame);
			aboutBox.setLocationRelativeTo(mainFrame);
		}
		JChessApp.getApplication().show(aboutBox);
	}

	public String showPawnPromotionBox(String color) {
		if (promotionBox == null) {
			JFrame mainFrame = JChessApp.getApplication().getMainFrame();
			promotionBox = new PawnPromotionWindow(mainFrame, color);
			promotionBox.setLocationRelativeTo(mainFrame);
			promotionBox.setModal(true);

		}
		promotionBox.setColor(color);
		JChessApp.getApplication().show(promotionBox);

		return promotionBox.result;
	}

	public String showSaveWindow() {

		return "";
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		mainPanel = new javax.swing.JPanel();
		gamesPane = new main.java.gui.JChessTabbedPane();
		menuBar = new javax.swing.JMenuBar();
		javax.swing.JMenu fileMenu = new javax.swing.JMenu();
		newGameItem = new javax.swing.JMenuItem();
		loadGameItem = new javax.swing.JMenuItem();
		saveGameItem = new javax.swing.JMenuItem();
		javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
		optionsMenu = new javax.swing.JMenu();
		themeSettingsMenu = new javax.swing.JMenuItem();
		javax.swing.JMenu helpMenu = new javax.swing.JMenu();
		javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
		statusPanel = new javax.swing.JPanel();
		javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
		statusMessageLabel = new javax.swing.JLabel();
		statusAnimationLabel = new javax.swing.JLabel();
		progressBar = new javax.swing.JProgressBar();

		mainPanel.setMaximumSize(new java.awt.Dimension(800, 600));
		mainPanel.setMinimumSize(new java.awt.Dimension(800, 600));
		mainPanel.setName("mainPanel"); // NOI18N
		mainPanel.setPreferredSize(new java.awt.Dimension(800, 600));

		gamesPane.setName("gamesPane"); // NOI18N

		javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		mainPanelLayout
				.setHorizontalGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(mainPanelLayout.createSequentialGroup().addContainerGap()
								.addComponent(gamesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 776, Short.MAX_VALUE)
								.addContainerGap()));
		mainPanelLayout.setVerticalGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(mainPanelLayout.createSequentialGroup().addContainerGap().addComponent(gamesPane,
						javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)));

		menuBar.setName("menuBar"); // NOI18N

		org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application
				.getInstance(main.java.JChessApp.class).getContext().getResourceMap(JChessView.class);
		fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
		fileMenu.setName("fileMenu"); // NOI18N

		newGameItem.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
		newGameItem.setText(resourceMap.getString("newGameItem.text")); // NOI18N
		newGameItem.setName("newGameItem"); // NOI18N
		fileMenu.add(newGameItem);
		newGameItem.addActionListener(this);

		loadGameItem.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
		loadGameItem.setText(resourceMap.getString("loadGameItem.text")); // NOI18N
		loadGameItem.setName("loadGameItem"); // NOI18N
		fileMenu.add(loadGameItem);
		loadGameItem.addActionListener(this);

		saveGameItem.setAccelerator(
				javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
		saveGameItem.setText(resourceMap.getString("saveGameItem.text")); // NOI18N
		saveGameItem.setName("saveGameItem"); // NOI18N
		fileMenu.add(saveGameItem);
		saveGameItem.addActionListener(this);

		javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(main.java.JChessApp.class)
				.getContext().getActionMap(JChessView.class, this);
		exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
		exitMenuItem.setName("exitMenuItem"); // NOI18N
		fileMenu.add(exitMenuItem);

		menuBar.add(fileMenu);

		optionsMenu.setText(resourceMap.getString("optionsMenu.text")); // NOI18N
		optionsMenu.setName("optionsMenu"); // NOI18N

		themeSettingsMenu.setText(resourceMap.getString("themeSettingsMenu.text")); // NOI18N
		themeSettingsMenu.setName("themeSettingsMenu"); // NOI18N
		optionsMenu.add(themeSettingsMenu);
		themeSettingsMenu.addActionListener(this);

		menuBar.add(optionsMenu);

		helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
		helpMenu.setName("helpMenu"); // NOI18N

		aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
		aboutMenuItem.setName("aboutMenuItem"); // NOI18N
		helpMenu.add(aboutMenuItem);

		menuBar.add(helpMenu);

		statusPanel.setName("statusPanel"); // NOI18N

		statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

		statusMessageLabel.setName("statusMessageLabel"); // NOI18N

		statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

		progressBar.setName("progressBar"); // NOI18N

		javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
		statusPanel.setLayout(statusPanelLayout);
		statusPanelLayout.setHorizontalGroup(statusPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
				.addGroup(statusPanelLayout.createSequentialGroup().addContainerGap().addComponent(statusMessageLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 616, Short.MAX_VALUE)
						.addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(statusAnimationLabel).addContainerGap()));
		statusPanelLayout.setVerticalGroup(statusPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(statusPanelLayout.createSequentialGroup()
						.addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
								javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(statusMessageLabel).addComponent(statusAnimationLabel).addComponent(
										progressBar, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(3, 3, 3)));

		setComponent(mainPanel);
		setMenuBar(menuBar);
		setStatusBar(statusPanel);
	}// </editor-fold>//GEN-END:initComponents
	private javax.swing.JTabbedPane gamesPane;
	private javax.swing.JMenuItem loadGameItem;
	public javax.swing.JPanel mainPanel;
	private javax.swing.JMenuBar menuBar;
	private javax.swing.JMenuItem newGameItem;
	private javax.swing.JMenu optionsMenu;
	private javax.swing.JProgressBar progressBar;
	private javax.swing.JMenuItem saveGameItem;
	private javax.swing.JLabel statusAnimationLabel;
	private javax.swing.JLabel statusMessageLabel;
	private javax.swing.JPanel statusPanel;
	private javax.swing.JMenuItem themeSettingsMenu;
	// End of variables declaration//GEN-END:variables
	// private JTabbedPaneWithIcon gamesPane;
	private final Timer messageTimer;
	private final Timer busyIconTimer;
	private final Icon idleIcon;
	private final Icon[] busyIcons = new Icon[15];
	private int busyIconIndex = 0;

	private JDialog aboutBox;
	private PawnPromotionWindow promotionBox;
	public NewGameWindow newGameFrame;

	public void componentResized(ComponentEvent e) {
		// System.out.println("jchessView resized!!;");
		LogToFile.log(new UnsupportedOperationException("Not supported yet."), "ERROR", "Nor supported operation");
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Game getActiveTabGame() throws ArrayIndexOutOfBoundsException {
		Game activeGame = (Game) this.gamesPane.getComponentAt(this.gamesPane.getSelectedIndex());
		return activeGame;
	}

	public int getNumberOfOpenedTabs() {
		return this.gamesPane.getTabCount();
	}

	public void componentMoved(ComponentEvent e) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void componentShown(ComponentEvent e) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void componentHidden(ComponentEvent e) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}

/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class EvolutionQuiz {

	public static void main(String[] args) {
		KieServices ks = KieServices.Factory.get();
		KieContainer kc = ks.getKieClasspathContainer();

		new EvolutionQuiz().init(kc, true);
	}

	public EvolutionQuiz() {
	}

	public void init(KieContainer kc, boolean exitOnClose) {
		Question question = new Question("Test1?", new String[] { "Yes", "No" });

		// The callback is responsible for populating working memory and
		// fireing all rules

		EvolutionQuizUI ui = new EvolutionQuizUI(question, new CheckoutCallback(kc));
		ui.createAndShowGUI(exitOnClose);
	}

	public static class EvolutionQuizUI extends JPanel {

		private static final long serialVersionUID = 510l;
		private JLabel questionTitle;
		private JTextArea output;
		private TableModel tableModel;
		private CheckoutCallback callback;
		private JList<String> jlist;
		private Vector<String> items;

		/**
		 * Build UI using specified items and using the given callback to pass the items
		 * and jframe reference to the drools application
		 * 
		 * @param listData
		 * @param callback
		 */
		public EvolutionQuizUI(Question question, CheckoutCallback callback) {
			super(new BorderLayout());
			this.callback = callback;
			this.items = question.getOptions();

			// Create main vertical split panel
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			add(splitPane, BorderLayout.CENTER);

			// create top half of split panel and add to parent
			JPanel topHalf = new JPanel();
			topHalf.setLayout(new BoxLayout(topHalf, BoxLayout.Y_AXIS));
			topHalf.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
			topHalf.setMinimumSize(new Dimension(400, 50));
			topHalf.setPreferredSize(new Dimension(450, 250));
			splitPane.add(topHalf);

			// create bottom top half of split panel and add to parent
			JPanel bottomHalf = new JPanel(new BorderLayout());
			bottomHalf.setMinimumSize(new Dimension(400, 50));
			bottomHalf.setPreferredSize(new Dimension(450, 300));
			splitPane.add(bottomHalf);

			// Add the question text
			System.out.println(111 + question.getQuestion());
			this.questionTitle = new JLabel(question.getQuestion());
			this.questionTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
			topHalf.add(this.questionTitle);

			// List of available choices
			JPanel listContainer = new JPanel(new GridLayout(1, 1));
			listContainer.setBorder(BorderFactory.createTitledBorder("Options"));
			topHalf.add(listContainer);

			// Create JList for items, add to scroll pane and then add to parent
			// container
			this.jlist = new JList<String>(this.items);
			ListSelectionModel listSelectionModel = jlist.getSelectionModel();
			listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			// handler adds item to shopping cart
			this.jlist.addMouseListener(new ListSelectionHandler());
			JScrollPane listPane = new JScrollPane(jlist);
			listContainer.add(listPane);

//            JPanel tableContainer = new JPanel( new GridLayout( 1, 1 ) );
//            tableContainer.setBorder( BorderFactory.createTitledBorder( "Selected answer" ) );
//            topHalf.add( tableContainer );

			// Container that displays table showing items in cart
			this.tableModel = new TableModel();
			JTable table = new JTable(tableModel);
			// handler removes item to shopping cart
			table.addMouseListener(new TableSelectionHandler());
			ListSelectionModel tableSelectionModel = table.getSelectionModel();
			tableSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			TableColumnModel tableColumnModel = table.getColumnModel();
			// notice we have a custom renderer for each column as both columns
			// point to the same underlying object
			tableColumnModel.getColumn(0).setCellRenderer(new NameRenderer());

//            JScrollPane tablePane = new JScrollPane( table );
//            tablePane.setPreferredSize( new Dimension( 150, 100 ) );
//            tableContainer.add( tablePane );

			// Create panel for checkout button and add to bottomHalf parent
			JPanel checkoutPane = new JPanel();
			JButton button = new JButton("Confirm");
			button.setVerticalTextPosition(AbstractButton.CENTER);
			button.setHorizontalTextPosition(AbstractButton.LEADING);
			// attach handler to assert items into working memory
			button.addMouseListener(new ConfirmButtonHandler());
			button.setActionCommand("confirm");
			checkoutPane.add(button);
			bottomHalf.add(checkoutPane, BorderLayout.NORTH);

			// Create output area, imbed in scroll area an add to bottomHalf parent
			// Scope is at instance level so it can be easily referenced from other
			// methods
			output = new JTextArea(4, 100);
			output.setLineWrap(true);
			output.setText(question.getQuestion());
			output.setEditable(false);
			JScrollPane outputPane = new JScrollPane(output, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			bottomHalf.add(outputPane, BorderLayout.CENTER);

			this.callback.setOutput(this.output);
		}

		/**
		 * Create and show the GUI
		 */
		public void createAndShowGUI(boolean exitOnClose) {
			// Create and set up the window.
			JFrame frame = new JFrame("EvolutionQuiz");
			frame.setDefaultCloseOperation(exitOnClose ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);

			setOpaque(true);
			frame.setContentPane(this);

			// Display the window.
			frame.pack();
			frame.setLocationRelativeTo(null); // Center in screen
			frame.setVisible(true);
		}

		/**
		 * Adds the selected item to the table
		 */
		private class ListSelectionHandler extends MouseAdapter {
			public void mouseReleased(MouseEvent e) {
				tableModel.addItem((String) jlist.getSelectedValue());
			}
		}

		/**
		 * Removes the selected item from the table
		 */
		private class TableSelectionHandler extends MouseAdapter {
			public void mouseReleased(MouseEvent e) {
				JTable jtable = (JTable) e.getSource();
				TableModel tableModel = (TableModel) jtable.getModel();
				tableModel.removeItem(jtable.getSelectedRow());
			}
		}

		/**
		 * Calls the referenced callback, passing a the jrame and selected items.
		 */
		private class ConfirmButtonHandler extends MouseAdapter {
			public void mouseReleased(MouseEvent e) {
				JButton button = (JButton) e.getComponent();
				if (tableModel.getItems().size() == 0)
					return;
				Question newQuestion = callback.checkout((JFrame) button.getTopLevelAncestor(), tableModel.getItems(),
						output.getText());
				tableModel.clear();
				output.setText("");
				if (e.getSource() == "confirm") {
					DefaultListModel<String> listModel = (DefaultListModel<String>) jlist.getModel();
					listModel.removeAllElements();
				}
				this.update(newQuestion);
			}

			public void update(Question newQuestion) {
				jlist.setListData(newQuestion.getOptions());
				output.setText(newQuestion.getQuestion());
				questionTitle.setText(newQuestion.getQuestion());
			}
		}

		/**
		 * Used to render the name column in the table
		 */
		private class NameRenderer extends DefaultTableCellRenderer {

			private static final long serialVersionUID = 510l;

			public NameRenderer() {
				super();
			}

			public void setValue(Object object) {
				String item = (String) object;
				setText(item);
			}
		}
	}

	/**
	 * This is the table model used to represent the users shopping cart While we
	 * have two colums, both columns point to the same object. We user a different
	 * renderer to display the different information abou the object - name and
	 * price.
	 */
	private static class TableModel extends AbstractTableModel {

		private static final long serialVersionUID = 510l;

		private String[] columnNames = { "Answer" };

		private ArrayList<String> items;

		public TableModel() {
			super();
			items = new ArrayList<String>();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return items.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return items.get(row);
		}

		public Class<?> getColumnClass(int c) {
			return String.class;
		}

		public void addItem(String item) {
			clear();
			items.add(item);
			fireTableRowsInserted(items.size(), items.size());
		}

		public void removeItem(int row) {
			items.remove(row);
			fireTableRowsDeleted(row, row);
		}

		public List<String> getItems() {
			return items;
		}

		public void clear() {
			int lastRow = items.size();
			items.clear();
			fireTableRowsDeleted(0, lastRow);
		}
	}

	/**
	 * 
	 * This callback is called when the user pressed the checkout button. It is
	 * responsible for adding the items to the shopping cart, asserting the shopping
	 * cart and then firing all rules.
	 * 
	 * A reference to the JFrame is also passed so the rules can launch dialog boxes
	 * for user interaction. It uses the ApplicationData feature for this.
	 */
	public static class CheckoutCallback {
		KieContainer kcontainer;
		JTextArea output;
		KieSession ksession;

		public CheckoutCallback(KieContainer kcontainer) {
			this.kcontainer = kcontainer;
		}

		public void setOutput(JTextArea output) {
			this.output = output;
		}

		/**
		 * Populate the cart and assert into working memory Pass Jframe reference for
		 * user interaction
		 * 
		 * @param frame
		 * @param items
		 * @return cart.toString();
		 */
		public Question checkout(JFrame frame, List<String> items, String question) {
			if (items.size() == 0)
				return new Question();
			else {
				System.out.println(question);
				String answer = new String();
				for (String item : items) {
					answer = item;
					System.out.println(answer);
				}
				this.ksession = this.kcontainer.newKieSession("ksession-rules");
				Fact newFact = new Fact(question, answer);

				Question nextQuestion = new Question();

				this.ksession.setGlobal("nextQuestion", nextQuestion);

				this.ksession.insert(newFact);

				this.ksession.fireAllRules();

				this.ksession.dispose();

				return nextQuestion;
			}
		}
	}

	public static class Question {
		private String question;
		private Vector<String> options;
		private boolean endNode;

		public Question() {
			this.question = new String();
			this.options = new Vector<String>();
		}

		public Question(String question, String[] options) {
			this.question = question;
			this.options = new Vector<String>(Arrays.asList(options));
		}

		public String getQuestion() {
			return this.question;
		}

		public Vector<String> getOptions() {
			return this.options;
		}

		public void setQuestion(String question) {
			this.question = question;
		}

		public void setOptions(Vector<String> options) {
			this.options = options;
		}

		public void addOption(String option) {
			this.getOptions().add(option);
		}
	}

	public static class Fact {
		public String question;
		public String answer;

		Fact() {

		}

		Fact(String question, String answer) {
			this.question = question;
			this.answer = answer;
		}

		public void setQuestion(String question) {
			this.question = question;
		}

		public void setAnswer(String answer) {
			this.answer = answer;
		}

		public String getQuestion() {
			return question;
		}

		public String getAnswer() {
			return answer;
		}
	}

}
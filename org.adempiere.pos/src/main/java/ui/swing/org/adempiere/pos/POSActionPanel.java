/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 Adempiere, Inc. All Rights Reserved.               *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/

package org.adempiere.pos;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;

import org.adempiere.pos.service.I_POSPanel;
import org.adempiere.pos.service.I_POSQuery;
import org.compiere.apps.ADialog;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerInfo;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MSequence;
import org.adempiere.pos.search.QueryBPartner;
import org.adempiere.pos.search.QueryTicket;
import org.compiere.print.ReportCtl;
import org.compiere.swing.CButton;
import org.compiere.swing.CTextField;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;


/**
 *	Customer Sub Panel
 *	
 *  @author Comunidad de Desarrollo OpenXpertya 
 *         *Basado en Codigo Original Modificado, Revisado y Optimizado de:
 *         *Copyright � Jorg Janke
 *         *Copyright � ConSerTi
 *  @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 *  @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 *  <li> Implement best practices
 *  @version $Id: SubOrder.java,v 1.1 2004/07/12 04:10:04 jjanke Exp $
 *  @version $Id: SubOrder.java,v 2.0 2015/09/01 00:00:00 mar_cal_westf
 */
public class POSActionPanel extends POSSubPanel 
	implements ActionListener, FocusListener, I_POSPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5895558315889871887L;

	/**
	 * 	Constructor
	 *	@param posPanel POS Panel
	 */
	public POSActionPanel (VPOS posPanel) {
		super (posPanel);
	}	//	PosSubCustomer
	
	/**	Buttons Command		*/
	private CButton 			f_bNew;
	private CButton 			f_bBPartner;
	private CButton 			f_bHistory;
	private CButton 			f_bBack;
	private CButton 			f_bNext;
	private CButton 			f_bCollect;
	private CButton 			f_bCancel;
	private CButton 			f_bLogout;
	/**	For Show BPartner	*/
	private	CTextField			f_NameBPartner;
	/**	For Orde List		*/
	private int 				m_RecordPosition;
	private ArrayList<Integer>	m_OrderList;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(POSActionPanel.class);	

	private final String ACTION_NEW         = "New";
	private final String ACTION_BPARTNER    = "BPartner";
	private final String ACTION_HISTORY     = "History";
	private final String ACTION_BACK       	= "Previous";
	private final String ACTION_NEXT  		= "Next";
	private final String ACTION_PAYMENT     = "Payment";
	private final String ACTION_CANCEL      = "Cancel";
	private final String ACTION_LOGOUT      = "End";
	
	/**
	 * 	Initialize
	 */
	public void init()
	{
		//	Content
		MigLayout layout = new MigLayout("ins 20 20","[fill|fill|fill|fill]","[nogrid]unrel[||]");
		setLayout(layout);
		listOrder();
		m_RecordPosition = m_OrderList.size()-1;

		String buttonSize = "w 50!, h 50!,";
		// NEW
		f_bNew = createButtonAction(ACTION_NEW, KeyStroke.getKeyStroke(KeyEvent.VK_F2, Event.F2));
		add (f_bNew, buttonSize+",gapx 35");

		// BPARTNER
		f_bBPartner = createButtonAction (ACTION_BPARTNER, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.SHIFT_MASK+Event.CTRL_MASK));
		add (f_bBPartner,buttonSize+",gapx 35" );
		
		// HISTORY
		f_bHistory = createButtonAction(ACTION_HISTORY, null);
 		add (f_bHistory, buttonSize+",gapx 35"); 
		
 		// 	BACK
 		f_bBack = createButtonAction(ACTION_BACK, null);
 		add (f_bBack, buttonSize+",gapx 35");
 		f_bBack.setEnabled(true);
		
 		//	NEXT
 		f_bNext = createButtonAction(ACTION_NEXT, null);
 		f_bNext.setActionCommand(ACTION_NEXT);
		add (f_bNext, buttonSize+",gapx 35"); 
		f_bNext.setEnabled(true);
 		
 		// PAYMENT
 		f_bCollect = createButtonAction(ACTION_PAYMENT, null);
		f_bCollect.setActionCommand(ACTION_PAYMENT);
		add (f_bCollect, buttonSize+",gapx 35"); 
		f_bCollect.setEnabled(false);
 		
 		// CANCEL
		f_bCancel = createButtonAction(ACTION_CANCEL, null);
 		add (f_bCancel, buttonSize +",gapx 35");
 		
		// Logout
		f_bLogout = createButtonAction (ACTION_LOGOUT, null);
		add (f_bLogout, buttonSize + ", gapx 35, wrap");

		// BP
//		CLabel BPLabelLabel = new CLabel(Msg.translate(Env.getCtx(), MBPartner.COLUMNNAME_C_BPartner_ID)); 
//		add(BPLabelLabel, "");
		f_NameBPartner = new CTextField();
		f_NameBPartner.setEditable(false);
		f_NameBPartner.setName(MBPartner.COLUMNNAME_Name);
//		add (f_name, "wrap,spanx 3, growx");

	}	//	init

	/**
	 * 	Dispose - Free Resources
	 */
	public void dispose()
	{
		if (f_NameBPartner != null)
			f_NameBPartner.removeFocusListener(this);
		f_NameBPartner = null;
		removeAll();
		super.dispose();
	}	//	dispose

	
	/**
	 * 	Distribute actions
	 *	@param e event
	 */
	public void actionPerformed (ActionEvent e) {
		String action = e.getActionCommand();
		if (action == null || action.length() == 0)
			return;
		log.info( "PosSubCustomer - actionPerformed: " + action);
		//	New
		if (e.getSource().equals(f_bNew)) {
			v_POSPanel.newOrder();
			v_POSPanel.refreshPanel();
			return;
		} else if (e.getSource().equals(f_bBPartner)) {
			changeBusinessPartner(); 
		} else if (e.getSource().equals(f_bHistory)) {
			// For already created, but either not completed or not yet paid POS Orders
			I_POSQuery qt = new QueryTicket(v_POSPanel);
			qt.setVisible(true);
			if (qt.getRecord_ID() > 0) {
				v_POSPanel.setOrder(qt.getRecord_ID());
			}
		} else if (e.getSource().equals(f_bBack)){
			previousRecord();
			v_POSPanel.refreshPanel();
		} else if (e.getSource().equals(f_bNext)){
			nextRecord();
		} else if (e.getSource().equals(f_bCollect)) {
			payOrder();
		} else if (e.getSource().equals(f_bCancel)) {
			deleteOrder();
		} else if (e.getSource().equals(f_bLogout)) {	//	Logout
			v_POSPanel.dispose();
			return;
		} else if (e.getSource() == f_NameBPartner) {
			findBPartner();
		}
		//	Refresh
		v_POSPanel.refreshPanel();
	}	//	actionPerformed

	/**
	 * 	Execute printing an order
	 */
	private void printOrder() {
		{
			if (isOrderFullyPaid())
			{
				changeViewPanel();
				printTicket();
				openCashDrawer();
			}
		}
	}
	
	/**
	 * Previous Record Order
	 */
	public void previousRecord() {
		if(m_RecordPosition > 0) {
			v_POSPanel.setOrder(m_OrderList.get(m_RecordPosition--));
		}
		//	Refresh
		v_POSPanel.refreshPanel();
	}

	/**
	 * Next Record Order
	 */
	public void nextRecord() {
		if(m_RecordPosition < m_OrderList.size() - 1) {
			v_POSPanel.setOrder(m_OrderList.get(m_RecordPosition++));
		}
		//	Refresh
		v_POSPanel.refreshPanel();
	}
	
	/**
	 * Execute order payment
	 * If order is not processed, process it first.
	 * If it is successful, proceed to pay and print ticket
	 */
	private void payOrder() {
		//Check if order is completed, if so, print and open drawer, create an empty order and set cashGiven to zero
		if(!v_POSPanel.hasOrder()) {		
			ADialog.warn(v_POSPanel.getWindowNo(), this,  Msg.getMsg(m_ctx, "POS.MustCreateOrder"));
		} else {
			VCollect collect = new VCollect(v_POSPanel);
			if (collect.showCollect()) {
				printTicket();
				v_POSPanel.setOrder(0);
			}
		}	
	}  // payOrder

	/**
	 * Execute deleting an order
	 * If the order is in drafted status -> ask to delete it
	 * If the order is in completed status -> ask to void it it
	 * Otherwise, it must be done outside this class.
	 */
	private void deleteOrder() {
		if (!v_POSPanel.hasOrder()) {
			ADialog.warn(v_POSPanel.getWindowNo(), this,  Msg.getMsg(m_ctx, "POS.MustCreateOrder"));
			return;			
		} else if (!v_POSPanel.isCompleted()) {
			if (ADialog.ask(v_POSPanel.getWindowNo(), this, Msg.getMsg(m_ctx, "POS.DeleteOrder"))) {	//	TODO translate it: Do you want to delete the Order? 
				if (!v_POSPanel.deleteOrder()) {
					ADialog.warn(v_POSPanel.getWindowNo(), this, Msg.getMsg(m_ctx, "POS.OrderCouldNotDeleted"));	//	TODO translate it: Order could not be deleted
				}
			}
		} else if (v_POSPanel.isCompleted()) {	
			if (ADialog.ask(0, this, Msg.getMsg(m_ctx, Msg.getMsg(m_ctx, "POS.OrderIsAlreadyCompleted")))) {	//	TODO Translate it: The order is already completed. Do you want to void it?
				if (!v_POSPanel.cancelOrder())
					ADialog.warn(v_POSPanel.getWindowNo(), this, Msg.getMsg(m_ctx, "POS.OrderCouldNotVoided"));	//	TODO Translate it: Order could not be voided
			}
		} else {
			ADialog.warn(v_POSPanel.getWindowNo(), this,  Msg.getMsg(m_ctx, "POS.OrderIsNotProcessed"));	//	TODO Translate it: Order is not Drafted nor Completed. Try to delete it other way
			return;
		}
		//	Update
		changeViewPanel();

	} // deleteOrder

	/**
	 * 	Focus Gained
	 *	@param e
	 */
	public void focusGained (FocusEvent e)
	{
	}	//	focusGained

	/**
	 * 	Focus Lost
	 *	@param e
	 */
	public void focusLost (FocusEvent e)
	{
		if (e.isTemporary())
			return;
		log.info(e.toString());
		findBPartner();
	}	//	focusLost

	
	/**
	 * 	Find/Set BPartner
	 */
	private void findBPartner() {
		String query = f_NameBPartner.getText();
		//	
		if (query == null || query.length() == 0)
			return;
		
		// unchanged
		if (!v_POSPanel.hasBPartner() 
				&& v_POSPanel.compareBPName(query))
			return;
		
		query = query.toUpperCase();
		//	Test Number
		boolean allNumber = true;
		boolean noNumber = true;
		char[] qq = query.toCharArray();
		for (int i = 0; i < qq.length; i++) {
			if (Character.isDigit(qq[i])) {
				noNumber = false;
				break;
			}
		} try {
			Integer.parseInt(query);
		} catch (Exception e) {
			allNumber = false;
		}
		String Value = query;
		String Name = (allNumber ? null : query);
		String EMail = (query.indexOf('@') != -1 ? query : null); 
		String Phone = (noNumber ? null : query);
		String City = null;
		//
		MBPartnerInfo[] results = MBPartnerInfo.find(m_ctx, Value, Name, 
			/*Contact, */null, EMail, Phone, City);
		
		//	Set Result
		if (results.length == 0) {
			v_POSPanel.setC_BPartner_ID(0);
		} else if (results.length == 1) {
			MBPartner bp = MBPartner.get(m_ctx, results[0].getC_BPartner_ID());
			v_POSPanel.setC_BPartner_ID(bp.getC_BPartner_ID());
			f_NameBPartner.setText(bp.getName());
		} else {	//	more than one
			QueryBPartner qt = new QueryBPartner(v_POSPanel);
			qt.setResults (results);
			qt.setVisible(true);
			if (qt.getRecord_ID() > 0) {
				v_POSPanel.setC_BPartner_ID(qt.getRecord_ID());
				log.fine("C_BPartner_ID=" + qt.getRecord_ID());
			} else {
				v_POSPanel.setC_BPartner_ID(0);
			}
		}
	}	//	findBPartner
	
	/**
	 * 	Print Ticket
	 * 
	 */
	public void printTicket() {
		if (!v_POSPanel.hasOrder())
			return;
		//	
		MOrder order = v_POSPanel.getM_Order();
		//int windowNo = p_posPanel.getWindowNo();
		//Properties m_ctx = p_posPanel.getPropiedades();
		
		if (order != null)
		{
			try 
			{
				//TODO: to incorporate work from Posterita
				/*
				if (p_pos.getAD_PrintLabel_ID() != 0)
					PrintLabel.printLabelTicket(order.getC_Order_ID(), p_pos.getAD_PrintLabel_ID());
				*/ 
				//print standard document
				Boolean print = true;
				if (m_pos.getAD_Sequence_ID()!= 0) {
					MSequence seq = new MSequence(Env.getCtx(), m_pos.getAD_Sequence_ID(), order.get_TrxName());
					String docno = seq.getPrefix() + seq.getCurrentNext();
					String q = "Confirmar el número consecutivo "  + docno;
					if (org.compiere.apps.ADialog.ask(v_POSPanel.getWindowNo(), this, q)) {
						order.setPOReference(docno);
						order.saveEx();
						ReportCtl.startDocumentPrint(0, order.getC_Order_ID(), false);
						int next = seq.getCurrentNext() + seq.getIncrementNo();
						seq.setCurrentNext(next);
						seq.saveEx();
					}
				}
				else
					ReportCtl.startDocumentPrint(0, order.getC_Order_ID(), false);				
			}
			catch (Exception e) 
			{
				log.severe("PrintTicket - Error Printing Ticket");
			}
		}	  
	}	
	
	/**
	 * Is order fully pay ?
	 * Calculates if the given money is sufficient to pay the order
	 * 
	 */
	public boolean isOrderFullyPaid()
	{
		/*TODO
		BigDecimal given = new BigDecimal(f_cashGiven.getValue().toString());
		boolean paid = false;
		if (p_posPanel != null && p_posPanel.f_curLine != null)
		{
			MOrder order = p_posPanel.f_curLine.getOrder();
			BigDecimal total = new BigDecimal(0);
			if (order != null)
				total = order.getGrandTotal();
			paid = given.doubleValue() >= total.doubleValue();
		}
		return paid;
		*/
		return true;
	}	

	/**
	 * 	Open cash drawer
	 * 
	 */
	public void openCashDrawer()
	{
		String port = "/dev/lp";
		
		byte data[] = new byte[] {0x1B, 0x40, 0x1C};
		try {  
            FileOutputStream m_out = null;
			if (m_out == null) {
                m_out = new FileOutputStream(port);  // No poner append = true.
            }
            m_out.write(data);
        } catch (IOException e) {
        }  
	}
	
	/**
	 * Get Data List Order
	 */
	public void listOrder() {
		String sql = "";
		PreparedStatement pstm;
		ResultSet rs;
		m_OrderList = new ArrayList<Integer>();
		try {
			sql=" SELECT o.C_Order_ID"
					+ " FROM C_Order o"
					+ " LEFT JOIN c_invoice i ON i.c_order_ID = o.c_order_ID"
					+ " WHERE"
					+ " (coalesce(invoiceopen(i.c_invoice_ID, 0), 0) > 0 OR o.docstatus IN ('DR', 'IP') ) AND "
					+ " o.issotrx='Y' AND "
					+ " o.ad_client_id=? "
					+ " ORDER BY o.dateordered ASC, o.datepromised ASC";
			
			pstm= DB.prepareStatement(sql, null);
			pstm.setInt (1, Env.getAD_Client_ID(Env.getCtx()));
			rs = pstm.executeQuery();
			//	Add to List
			while(rs.next()){
				m_OrderList.add(rs.getInt(1));
			}
		} catch(Exception e) {
			log.severe("SubOrder.listOrder: " + e + " -> " + sql);
		}
	}

	@Override
	public void refreshPanel() {
		
	}

	@Override
	public String validatePanel() {
		return null;
	}

	@Override
	public void changeViewPanel() {
		MOrder order = v_POSPanel.getM_Order();
		if (order != null) {  				
				// Button BPartner: enable when order drafted, and order has no lines
//				v_POSPanel.setC_BPartner_ID(order.getC_BPartner_ID());  				
				if(order.getDocStatus().equals(MOrder.DOCSTATUS_Drafted) && 
						order.getLines().length == 0 )
					f_bBPartner.setEnabled(true);
				else
					f_bBPartner.setEnabled(false);

				// Button New: enabled when lines existing or order is voided
				f_bNew.setEnabled(order.getLines().length != 0 || order.getDocStatus().equals(MOrder.DOCSTATUS_Voided));
				
				// Button Credit Sale: enabled when drafted, with lines and not invoiced
				if(order.getDocStatus().equals(MOrder.DOCSTATUS_Drafted) && 
						order.getLines().length != 0 && 
						order.getC_Invoice_ID()<=0)

			    // History Button: enabled when lines existing or order is voided
				if(order.getLines().length != 0 || order.getDocStatus().equals(MOrder.DOCSTATUS_Voided))
	  				f_bHistory.setEnabled(true);  	
				else
					f_bHistory.setEnabled(false);

				if(!order.getDocStatus().equals(MOrder.DOCSTATUS_Voided))			
	  				f_bCancel.setEnabled(true);
				else
					f_bCancel.setEnabled(false);
				
				// Button Payment: enable when (drafted, with lines) or (completed, on credit, (not invoiced or not paid) ) 
				// or (is completed, standard and not fully paid)
				if((order.getDocStatus().equals(MOrder.DOCSTATUS_Drafted) && order.getLines().length != 0) ||
				   (order.getDocStatus().equals(MOrder.DOCSTATUS_Completed) && 
				    order.getC_DocType().getDocSubTypeSO().equalsIgnoreCase(MOrder.DocSubTypeSO_OnCredit) &&
				    	(order.getC_Invoice_ID()<=0  ||
				    	 !MInvoice.get(m_ctx, order.getC_Invoice_ID()).isPaid()
				    	 )
				   ) ||
				   (order.getDocStatus().equals(MOrder.DOCSTATUS_Completed) && 
				    order.getC_DocType().getDocSubTypeSO().equalsIgnoreCase(MOrder.DocSubTypeSO_Standard) &&
				    order.getGrandTotal().subtract(v_POSPanel.getPaidAmt()).compareTo(Env.ZERO)==1
				   )
				  )
					f_bCollect.setEnabled(true);
				else 
				f_bCollect.setEnabled(false);	
				
			    // Next and Back Buttons:  enabled when lines existing or order is voided
				if(order.getLines().length != 0 || order.getDocStatus().equals(MOrder.DOCSTATUS_Voided)) {

					if(m_RecordPosition==m_OrderList.size()-1)
					    f_bNext.setEnabled(false); // End of order list
					else
	  					f_bNext.setEnabled(true);

					if(m_RecordPosition==0)
						f_bBack.setEnabled(false); // Begin of order list
					else
						f_bBack.setEnabled(true);
				} else {
					f_bNext.setEnabled(false);
	  				f_bBack.setEnabled(false);
				}

			    // Logout Button: enabled when lines existing or order is voided
				if(order.getLines().length != 0 || order.getDocStatus().equals(MOrder.DOCSTATUS_Voided))
	  				f_bLogout.setEnabled(true);  	
				else
					f_bLogout.setEnabled(false);
				
		} else {
			v_POSPanel.setC_BPartner_ID(0);
			f_bBPartner.setEnabled(false);
			f_bNew.setEnabled(true);
			f_bHistory.setEnabled(true);
			f_bCancel.setEnabled(false);
			f_bCollect.setEnabled(false);
		}
	}
	

	/**
	 * 	Change in Order the Business Partner, including Price list and location
	 *  In Order and POS
	 * 
	 */
	public void changeBusinessPartner() {
		// Change to another BPartner
		QueryBPartner qt = new QueryBPartner(v_POSPanel);
		qt.setVisible(true);
		if (qt.getRecord_ID() > 0) {
			MBPartner bp = MBPartner.get(m_ctx, qt.getRecord_ID());
			f_NameBPartner.setText(bp.getName()); 
			v_POSPanel.setC_BPartner_ID(bp.getC_BPartner_ID());
			log.fine("C_BPartner_ID=" + bp.getC_BPartner_ID());
		}
//		else {
//			v_POSPanel.setC_BPartner_ID(0);
//		}
//		findBPartner();
//		if (v_POSPanel.getC_BPartner_ID() > 0) {				
//			v_POSPanel.getM_Order().saveEx();  // TODO: how to avoid save here? Otherwise, neither BP nor PriceList are saved.
//		}	
	}	

}//	POSActionPanel
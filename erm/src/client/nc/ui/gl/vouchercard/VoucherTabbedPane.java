package nc.ui.gl.vouchercard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import nc.ui.gl.pubvoucher.VoucherToftPanel;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.ToftPanel;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UITabbedPane;
import nc.vo.gateway60.accountbook.AccountBookUtil;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.gl.uicfg.ButtonRegistVO;
import nc.vo.gl.uicfg.UIConfigVO;

public class VoucherTabbedPane extends UIPanel {
	private ToftPanel toftPanel = null;

	private Component currentVoucherPanel = null;

	private Component firstVoucherPanel = null;

	private UITabbedPane tabbedPane = null;

	private UILabel iconLabel = null;

	private HashMap vouchermap = new HashMap();
	private VoucherCheckPanel voucherCheckPanel = null;

	int componentIndex = 0;

	String componentTitle = null;

	public VoucherTabbedPane() {
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setSize(522, 316);
	}

	private UITabbedPane getTabbedPane() {
		if (this.tabbedPane == null) {
			this.tabbedPane = new UITabbedPane();

			this.tabbedPane.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						VoucherTabbedPane.this.componentIndex = VoucherTabbedPane.this.getTabbedPane().getSelectedIndex();
						VoucherTabbedPane.this.componentTitle = VoucherTabbedPane.this.getTabbedPane().getTitleAt(VoucherTabbedPane.this.componentIndex);
						VoucherTabbedPane.this.removeAll();
						VoucherTabbedPane.this.getIconLabel().setToolTipText(
								VoucherTabbedPane.this.getTabbedPane().getToolTipTextAt(VoucherTabbedPane.this.getTabbedPane().getSelectedIndex()));
						VoucherTabbedPane.this.add(VoucherTabbedPane.this.getIconLabel(), "West");
						VoucherTabbedPane.this.add(VoucherTabbedPane.this.getTabbedPane().getSelectedComponent(), "Center");
						VoucherTabbedPane.this.updateUI();
					}
				}
			});
			this.tabbedPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if ((VoucherTabbedPane.this.getTabbedPane().isVisible()) && (VoucherTabbedPane.this.getTabbedPane().isShowing())) {
						VoucherTabbedPane.this.currentVoucherPanel = VoucherTabbedPane.this.getTabbedPane().getSelectedComponent();
						ButtonRegistVO[] bns = VoucherTabbedPane.this.getCurrentVoucherPanel().getVoucherUI().getUIConfigVO().getButtons();

						if (bns != null) {
							((VoucherToftPanel) VoucherTabbedPane.this.getToftPanel()).installButtons(null, bns);
						}
					}
				}
			});
		}
		return this.tabbedPane;
	}

	public VoucherPanel getVoucherPanel() {
		return getCurrentVoucherPanel();
	}

	public VoucherPanel getCurrentVoucherPanel() {
		if ((this.currentVoucherPanel instanceof VoucherPanel))
			return (VoucherPanel) this.currentVoucherPanel;
		if ((this.currentVoucherPanel instanceof VoucherTabbedPane))
			return ((VoucherTabbedPane) this.currentVoucherPanel).getCurrentVoucherPanel();
		return null;
	}

	public void setVoucherPanel(VoucherPanel voucherPanel) {
		removeAll();
		add(voucherPanel, "Center");
		VoucherCheckPanel checkPanel = getVoucherCheckPanel();
		voucherPanel.getVoucherModel().addValueChangeListener(checkPanel);
		add(checkPanel, "South");
		updateUI();
		this.currentVoucherPanel = voucherPanel;
		this.firstVoucherPanel = voucherPanel;
		getTabbedPane().removeAll();
		this.vouchermap.clear();
		VoucherVO vo = (VoucherVO) voucherPanel.getVoucherModel().getValue(0, new Integer(0));
		if ((vo != null) && (vo.getPk_accountingbook() != null)) {
			this.componentTitle = new AccountBookUtil().getAccountingBookNameByPk(vo.getPk_accountingbook());
		}
	}

	public void addVoucherPanel(String title, VoucherTabbedPane voucherPanel) {
		if (getTabbedPane().getTabCount() == 0) {
			removeAll();
			add(getTabbedPane(), "Center");
			this.vouchermap.clear();
			getTabbedPane().addTab(formatToHtml(NCLangRes.getInstance().getStrByID("20021005", "UPP20021005-000737")), null, getCurrentVoucherPanel(),
					formatToHtml(this.componentTitle));
		}

		String tip = null;
		VoucherVO vo = (VoucherVO) voucherPanel.getVoucherPanel().getVoucherModel().getValue(0, new Integer(0));
		if ((vo != null) && (vo.getPk_accountingbook() != null))
			tip = new AccountBookUtil().getAccountingBookNameByPk(vo.getPk_accountingbook());
		if (vo.getPk_voucher() != null) {
			if (this.vouchermap.get(vo.getPk_voucher()) != null) {
				((VoucherTabbedPane) this.vouchermap.get(vo.getPk_voucher())).getVoucherPanel().setVO(vo);
				return;
			}

			this.vouchermap.put(vo.getPk_voucher(), voucherPanel);
		}
		voucherPanel.setToftPanel(getToftPanel());
		voucherPanel.getVoucherPanel().addListener("ToftPanel", getToftPanel());
		getTabbedPane().addTab(formatToHtml(title), null, voucherPanel, formatToHtml(tip));
	}

	public static String formatToHtml(String str) {
		return str;
	}

	private UILabel getIconLabel() {
		if (this.iconLabel == null) {
			this.iconLabel = new UILabel();
			this.iconLabel.setText("");
			this.iconLabel.setPreferredSize(new Dimension(10, 10));
			this.iconLabel.setILabelType(0);

			this.iconLabel.setOpaque(true);
			this.iconLabel.setBackground(new Color(153, 153, 255));
			this.iconLabel.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						VoucherTabbedPane.this.removeAll();
						if (VoucherTabbedPane.this.componentIndex < VoucherTabbedPane.this.getTabbedPane().getTabCount()) {
							VoucherTabbedPane.this.getTabbedPane().insertTab(VoucherTabbedPane.this.componentTitle, null,
									VoucherTabbedPane.this.getCurrentVoucherPanel(), VoucherTabbedPane.this.getIconLabel().getToolTipText(),
									VoucherTabbedPane.this.componentIndex);
						} else
							VoucherTabbedPane.this.getTabbedPane().addTab(VoucherTabbedPane.this.componentTitle, null,
									VoucherTabbedPane.this.getCurrentVoucherPanel(), VoucherTabbedPane.this.getIconLabel().getToolTipText());
						VoucherTabbedPane.this.add(VoucherTabbedPane.this.getTabbedPane(), "Center");
						VoucherTabbedPane.this.getTabbedPane().setSelectedIndex(VoucherTabbedPane.this.componentIndex);
						VoucherTabbedPane.this.updateUI();
					}
				}
			});
		}
		return this.iconLabel;
	}

	public ToftPanel getToftPanel() {
		return this.toftPanel;
	}

	public void setToftPanel(ToftPanel toftPanel) {
		this.toftPanel = toftPanel;
	}

	public VoucherPanel getFirstVoucherPanel() {
		if ((this.firstVoucherPanel instanceof VoucherPanel))
			return (VoucherPanel) this.firstVoucherPanel;
		if ((this.firstVoucherPanel instanceof VoucherTabbedPane))
			return ((VoucherTabbedPane) this.firstVoucherPanel).getFirstVoucherPanel();
		return null;
	}

	public void showOnlyFirstVoucherPanel() {
		setVoucherPanel(getFirstVoucherPanel());
	}

	public void removeVoucherPanelByPK(String pk_voucher) {
		if (pk_voucher == null)
			return;
		if (getTabbedPane().getTabCount() == 0)
			return;
		getTabbedPane().remove((Component) this.vouchermap.get(pk_voucher));
	}

	public VoucherCheckPanel getVoucherCheckPanel() {
		if (this.voucherCheckPanel == null)
			this.voucherCheckPanel = new VoucherCheckPanel(this);
		return this.voucherCheckPanel;
	}

	public int getTabCount() {
		return getTabbedPane().getTabCount();
	}
}

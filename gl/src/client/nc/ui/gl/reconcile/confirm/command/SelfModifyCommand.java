package nc.ui.gl.reconcile.confirm.command;

import nc.bs.framework.exception.ComponentException;
import nc.ui.gl.pubreconcile.OtherModifyDlg;
import nc.ui.gl.reconcile.center.tab.ReconcileCenterTabModel;
import nc.ui.gl.reconcile.confirm.RecVoucherUtil;
import nc.ui.ml.NCLangRes;
import nc.vo.fipub.utils.uif2.FiUif2MsgUtil;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.glcom.exception.GLBusinessException;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.ValidationException;

public class SelfModifyCommand extends ReconcileConfirmCommand {
	public SelfModifyCommand() {
	}

	public void execute() {
		ReconcileCenterTabModel model = (ReconcileCenterTabModel) getClientUI().getTab().getModel();
		int rows = model.getSelectedRowCount();
		if (rows <= 0) {
			FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0001"), NCLangRes4VoTransl
					.getNCLangRes().getStrByID("contrast_0", "02002002-0394"));

			return;
		}
		VoucherVO combinedvoucher = null;
		try {
			String[] recOtherTmps = ((ReconcileCenterTabModel) getClientUI().getTab().getModel()).getOtherTmps();

			combinedvoucher = ((nc.itf.gl.reconcile.center.IReconcileCenter) nc.bs.framework.common.NCLocator.getInstance().lookup(
					nc.itf.gl.reconcile.center.IReconcileCenter.class.getName())).getVouchVOByPkOtherTmps(recOtherTmps);

		} catch (ValidationException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes.getInstance().getStrByID("200213", "UPP200213-000319"), NCLangRes4VoTransl
					.getNCLangRes().getStrByID("contrast_0", "02002002-0441") + e.toString());

		} catch (ComponentException e) {

			nc.bs.logging.Logger.error(e.getMessage(), e);
			FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes.getInstance().getStrByID("200213", "UPP200213-000319"), NCLangRes4VoTransl
					.getNCLangRes().getStrByID("contrast_0", "02002002-0441") + e.toString());

		} catch (GLBusinessException e) {

			nc.bs.logging.Logger.error(e.getMessage(), e);
			FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes.getInstance().getStrByID("200213", "UPP200213-000319"), NCLangRes4VoTransl
					.getNCLangRes().getStrByID("contrast_0", "02002002-0441") + e.toString());
		}

		VoucherVO tempvo = combinedvoucher;
		if (combinedvoucher != null) {
			tempvo.setModifyflag("NNN");
			tempvo.setDetailmodflag(nc.vo.pub.lang.UFBoolean.FALSE);
			tempvo = RecVoucherUtil.catVoucherData(tempvo);
			try {
				// 20161230 tsy 不进行本币重新计算
				// RecVoucherUtil.convertDetailAmount(tempvo);
				// 20161230 end
				RecVoucherUtil.setGroupSelfValue(tempvo);
				RecVoucherUtil.setGloalSelfValue(tempvo);
			} catch (BusinessException e) {
				nc.bs.logging.Logger.error(e.getMessage(), e);
				FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("200220120409_0", "0200220120409-0005"),
						e.getMessage());
				return;
			}
			OtherModifyDlg m_modifydlg = new OtherModifyDlg(getClientUI(), tempvo);
			m_modifydlg.setFilterAccsubjPks(null, tempvo.getPk_accountingbook());
			int result = m_modifydlg.showModal();
			if ((result == 1) && (m_modifydlg.getSelectResult() > 0)) {
				doRefresh();
				getClientUI().showHintMessage(NCLangRes.getInstance().getStrByID("2002130058", "UPP2002130058-000112"));
			}
		}
	}

	public String getCommandName() {
		return NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0385");
	}

	public int getCommandcode() {
		return ReconcileConfirmBtn.SELFMODIFY.ordinal();
	}
}

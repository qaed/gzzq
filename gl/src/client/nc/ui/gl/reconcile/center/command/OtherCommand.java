package nc.ui.gl.reconcile.center.command;

import java.util.List;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.gl.reconcile.center.IReconcileCenter;
import nc.ui.gl.cachefeed.GlBizObjectAccessor;
import nc.ui.gl.contrast.rule.util.UITable;
import nc.ui.gl.pubreconcile.ReconcileCenterUI;
import nc.ui.gl.reconcile.center.tab.ReconcileCenterTabColumn;
import nc.ui.gl.reconcile.center.tab.ReconcileCenterTabModel;
import nc.ui.gl.reconcile.confirm.RecVoucherUtil;
import nc.ui.gl.reconcilepub.DataDispose;
import nc.vo.fipub.utils.uif2.FiUif2MsgUtil;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;

public class OtherCommand extends ReconcileCenterCommand {
	public OtherCommand() {
	}

	public void execute() {
		ReconcileCenterTabModel model = (ReconcileCenterTabModel) getClientUI().getTab().getModel();
		int selectedIndex = getClientUI().getTab().getModelSelectedRow();
		if (selectedIndex < 0) {
			FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0001"), NCLangRes4VoTransl
					.getNCLangRes().getStrByID("contrast_0", "02002002-0394"));
			return;
		}
		ReconcileCenterUI clientui = getClientUI();

		Object state = model.getValueAt(selectedIndex, ReconcileCenterTabColumn.RECONSTAT.ordinal());
		if (state == null) {
			return;
		}
		VoucherVO voucherVO = null;

		if (2 == Integer.parseInt(state.toString())) {
			Object obj = model.getValueAt(selectedIndex, ReconcileCenterTabColumn.OPP_VOUCHER.ordinal());
			if (obj == null) {
				return;
			}
			String pk_voucherother = obj.toString();
			try {
				voucherVO = nc.vo.glcom.tools.GLPubProxy.getRemoteVoucher().queryByPk(pk_voucherother);
			} catch (BusinessException e) {
				Logger.error(e.getMessage(), e);
				FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0151"),
						NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0395"));
			}
			try {
				List<String> list = ((IReconcileCenter) NCLocator.getInstance().lookup(IReconcileCenter.class)).queryOppDetailByPk_voucher(pk_voucherother);

				List<DetailVO> detailList = new java.util.ArrayList();
				for (int i = 0; i < voucherVO.getDetails().length; i++) {
					if (list.contains(voucherVO.getDetails()[i].getPk_detail())) {
						detailList.add(voucherVO.getDetails()[i]);
					}
				}
				voucherVO.setDetails((DetailVO[]) detailList.toArray(new DetailVO[0]));
			} catch (BusinessException e) {
				FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0151"),
						NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0395"));
				return;
			}
		} else {
			Object obj = model.getValueAt(selectedIndex, ReconcileCenterTabColumn.PK_RECOTHERTMP.ordinal());
			if (obj == null) {
				return;
			}
			String pk_reconthertmp = obj.toString();
			try {
				voucherVO = ((IReconcileCenter) NCLocator.getInstance().lookup(IReconcileCenter.class.getName()))
						.getVouchVOByPkOtherTmps(new String[] { pk_reconthertmp });
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
				FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0151"),
						NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0395"));
			}

			voucherVO.setPk_voucher("11111");
			voucherVO.setModifyflag("NNN");
			voucherVO.setDetailmodflag(UFBoolean.FALSE);
			voucherVO.setDiscardflag(UFBoolean.FALSE);

			DataDispose datadispose = new DataDispose();
			datadispose.catVoucherData(voucherVO, false);

			GlBizObjectAccessor.setRuntimeEnvObject("1002", null);
		}
		// 20161229 tsy 不进行本币的重新计算
		// try {
		// RecVoucherUtil.convertDetailAmount(voucherVO);
		// } catch (BusinessException e) {
		// Logger.error(e.getMessage(), e);
		// }
		// 20161229 end
		clientui.showVoucher(voucherVO);
		getClientUI().showHintMessage(NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0396"));
	}

	public String getCommandName() {
		return NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0374");
	}

	public int getCommandcode() {
		return ReconcileCenterBtn.BTN_OTHER.ordinal();
	}
}
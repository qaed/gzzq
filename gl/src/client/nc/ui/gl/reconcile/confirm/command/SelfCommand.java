package nc.ui.gl.reconcile.confirm.command;

import nc.bs.logging.Logger;
import nc.ui.gl.cachefeed.CacheRequestFactory;
import nc.ui.gl.contrast.rule.util.UITable;
import nc.ui.gl.pubreconcile.ReconcileConfirmUI;
import nc.ui.gl.reconcile.center.tab.ReconcileCenterTabColumn;
import nc.ui.gl.reconcile.center.tab.ReconcileCenterTabModel;
import nc.ui.gl.reconcilepub.DataDispose;
import nc.ui.gl.remotecall.GlRemoteCallProxy;
import nc.vo.fipub.utils.uif2.FiUif2MsgUtil;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;

public class SelfCommand extends ReconcileConfirmCommand {
	public SelfCommand() {
	}

	public void execute() {
		ReconcileCenterTabModel model = (ReconcileCenterTabModel) getClientUI().getTab().getModel();
		int selectedIndex = getClientUI().getTab().getModelSelectedRow();
		String pk_accountingbook = null;
		if (selectedIndex < 0) {
			FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0001"), NCLangRes4VoTransl
					.getNCLangRes().getStrByID("contrast_0", "02002002-0394"));

			return;
		}
		ReconcileConfirmUI clientui = getClientUI();

		Object state = model.getValueAt(selectedIndex, ReconcileCenterTabColumn.RECONSTAT.ordinal());
		if (state == null) {
			return;
		}
		VoucherVO selfvo = null;

		if (2 == Integer.parseInt(state.toString())) {
			Object obj = model.getValueAt(selectedIndex, ReconcileCenterTabColumn.SELF_VOUCHER.ordinal());
			if (obj == null) {
				return;
			}
			String pk_voucherself = obj.toString();
			obj = model.getValueAt(selectedIndex, ReconcileCenterTabColumn.SELF_PKACCOUNTINGBOOK.ordinal());
			if (obj == null) {
				return;
			}
			pk_accountingbook = obj.toString();

			try {
				selfvo = (VoucherVO) GlRemoteCallProxy.callProxy(CacheRequestFactory.queryVoucherByPk(pk_accountingbook, pk_voucherself)).getBizzData();
			} catch (BusinessException e) {
				Logger.error(e.getMessage(), e);
				FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0151"),
						NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0440"));
			}
		} else {
			Object pk_reconthertmp = model.getValueAt(selectedIndex, ReconcileCenterTabColumn.PK_RECOTHERTMP.ordinal());
			if (pk_reconthertmp == null) {
				return;
			}
			Object obj = model.getValueAt(selectedIndex, ReconcileCenterTabColumn.SELF_PKACCOUNTINGBOOK.ordinal());
			if (obj == null) {
				return;
			}
			pk_accountingbook = obj.toString();

			try {
				selfvo = (VoucherVO) GlRemoteCallProxy.callProxy(
						CacheRequestFactory.queryReconVoucher(pk_accountingbook, new String[] { (String) pk_reconthertmp })).getBizzData();
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
				FiUif2MsgUtil.showUif2DetailMessage(getClientUI(), NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0151"), e.getMessage());
			}

			selfvo.setPk_voucher("11111");

			selfvo.setModifyflag("NNN");
			selfvo.setDetailmodflag(nc.vo.pub.lang.UFBoolean.FALSE);
			selfvo.setDiscardflag(nc.vo.pub.lang.UFBoolean.FALSE);

			DataDispose datadispose = new DataDispose();
			datadispose.catVoucherData(selfvo, false);

			nc.ui.gl.cachefeed.GlBizObjectAccessor.setRuntimeEnvObject("1002", null);
		}
		// 20161230 tsy 不进行本币重新计算
		// try
		// {
		// nc.ui.gl.reconcile.confirm.RecVoucherUtil.convertDetailAmount(selfvo);
		// } catch (BusinessException e) {
		// Logger.error(e.getMessage(), e);
		// }
		// 20161230 end

		clientui.showVoucher(selfvo);
		getClientUI().showHintMessage(NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0402"));
	}

	public String getCommandName() {
		return NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0", "02002002-0373");
	}

	public int getCommandcode() {
		return ReconcileConfirmBtn.SELF.ordinal();
	}
}
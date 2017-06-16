package nc.impl.obm.confirmpay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nc.bs.framework.common.NCLocator;
import nc.bs.obm.ebankconfirmpay.ace.bp.AceEbankconfirmpayCreateBP;
import nc.impl.obm.ebanklog.LogYurrefRelationRegisterImpl;
import nc.itf.obm.confirmpay.IEbankconfirmpayMaintain;
import nc.itf.obm.ebanklog.IELogLock;
import nc.itf.obm.ebanklog.IEbankLogManageService;
import nc.itf.obm.ebanklog.IYurrefRelationRegister;
import nc.md.data.access.NCObject;
import nc.md.persist.framework.IMDPersistenceQueryService;
import nc.pubitf.tmpub.dblock.ITMDataBaseLock;
import nc.ui.querytemplate.querytree.IQueryScheme;
import nc.vo.obm.ebankconfirmpay.AggConfirmPayHVO;
import nc.vo.obm.ebankconfirmpay.ConfirmPayBVO;
import nc.vo.obm.ebankconfirmpay.ConfirmPayHVO;
import nc.vo.obm.ml.MLObm;
import nc.vo.obm.obmvo.PmtconfirmAddVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.ISuperVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.pubapp.pattern.exception.ExceptionUtils;
import nc.vo.tmpub.util.ArrayUtil;
import nc.vo.tmpub.util.SqlUtil;
import nc.vo.tmpub.util.StringUtil;

public class EbankconfirmpayMaintainImpl extends AceEbankconfirmpayPubServiceImpl implements IEbankconfirmpayMaintain {
	public EbankconfirmpayMaintainImpl() {
	}

	public void delete(AggConfirmPayHVO[] clientFullVOs, AggConfirmPayHVO[] originBills) throws BusinessException {
		super.pubdeleteBills(clientFullVOs, originBills);
	}

	public AggConfirmPayHVO[] insert(AggConfirmPayHVO[] clientFullVOs, AggConfirmPayHVO[] originBills) throws BusinessException {
		return super.pubinsertBills(clientFullVOs, originBills);
	}

	public AggConfirmPayHVO[] createConfirmpayBill(PmtconfirmAddVO addVOs) throws BusinessException {
		IELogLock<String> elogBizLock = null;
		AggConfirmPayHVO[] result = null;
		try {
			String yurref = addVOs.getYurref();
			IEbankLogManageService logMgrService = (IEbankLogManageService) NCLocator.getInstance().lookup(IEbankLogManageService.class.getName());

			Set<String> yurrefLocks = new HashSet();
			yurrefLocks.add(yurref);

			IYurrefRelationRegister relsev = new LogYurrefRelationRegisterImpl();
			String[] realyurref = relsev.findAllRelVOByDestYurrefs((String[]) yurrefLocks.toArray(new String[0]));
			Set<String> realyurrefLocks = new HashSet();
			for (int i = 0; i < realyurref.length; i++) {
				realyurrefLocks.add(realyurref[i]);
			}

			elogBizLock = logMgrService.lockedEBankLog(realyurrefLocks, "insertConfirmpayBill");

			ITMDataBaseLock dblock = (ITMDataBaseLock) NCLocator.getInstance().lookup(ITMDataBaseLock.class.getName());
			dblock.dBLocks("ebank_dblock", (String[]) realyurrefLocks.toArray(new String[0]));

			AceEbankconfirmpayCreateBP createBP = new AceEbankconfirmpayCreateBP();
			result = createBP.create(new PmtconfirmAddVO[] { addVOs });
			// tsy
			if (result != null && result.length != 0) {
				((ConfirmPayHVO) result[0].getParentVO()).setConfirmtime(new UFDate());
				ConfirmPayBVO[] childrenVO = (ConfirmPayBVO[]) result[0].getChildren(ConfirmPayBVO.class);
				if (childrenVO != null && childrenVO.length != 0) {
					for (ConfirmPayBVO confirmPayBVO : childrenVO) {
						confirmPayBVO.setConfirmtime(new UFDate());
					}
				}
			}
			// end

		} catch (BusinessException e) {
			ExceptionUtils.wrappBusinessException(e.getMessage());
		} finally {
			if (elogBizLock != null) {
				elogBizLock.destory();
			}
		}
		return result;
	}

	public AggConfirmPayHVO[] update(AggConfirmPayHVO[] clientFullVOs, AggConfirmPayHVO[] originBills) throws BusinessException {
		return super.pubupdateBills(clientFullVOs, originBills);
	}

	public String[] queryPKs(IQueryScheme queryScheme) throws BusinessException {
		return super.pubquerypkbills(queryScheme);
	}

	public AggConfirmPayHVO[] queryBillByPK(String[] pks) throws BusinessException {
		return super.pubquerybillbypkbills(pks);
	}

	public AggConfirmPayHVO[] save(AggConfirmPayHVO[] clientFullVOs, AggConfirmPayHVO[] originBills) throws BusinessException {
		return super.pubsendapprovebills(clientFullVOs, originBills);
	}

	public AggConfirmPayHVO[] unsave(AggConfirmPayHVO[] clientFullVOs, AggConfirmPayHVO[] originBills) throws BusinessException {
		return super.pubunsendapprovebills(clientFullVOs, originBills);
	}

	public AggConfirmPayHVO[] approve(AggConfirmPayHVO[] clientFullVOs, AggConfirmPayHVO[] originBills) throws BusinessException {
		return super.pubapprovebills(clientFullVOs, originBills);
	}

	public AggConfirmPayHVO[] unapprove(AggConfirmPayHVO[] clientFullVOs, AggConfirmPayHVO[] originBills) throws BusinessException {
		return super.pubunapprovebills(clientFullVOs, originBills);
	}

	public AggConfirmPayHVO[] queryAggConfirmPayHVOsBySrcBillIDs(String[] srcID) throws BusinessException {
		List<AggConfirmPayHVO> volist = new ArrayList();
		String sqlWhere = SqlUtil.buildSqlForIn("sourcebillpk", srcID);

		NCObject[] objs = getMDService().queryBillOfNCObjectByCond(AggConfirmPayHVO.class, sqlWhere, false);

		if (!ArrayUtil.isNull(objs)) {
			for (NCObject obj : objs) {
				volist.add((AggConfirmPayHVO) obj.getContainmentObject());
			}
		}
		return (AggConfirmPayHVO[]) volist.toArray(new AggConfirmPayHVO[0]);
	}

	public AggConfirmPayHVO[] queryAggConfirmPayHVOsByYurrefs(String[] yurref) throws BusinessException {
		List<AggConfirmPayHVO> volist = new ArrayList();
		String sqlWhere = " pk_confirmpay_h in ( select cdtrdetail from ebank_confirmpay_b where " + SqlUtil.buildSqlForIn("yurref", yurref) + ") and dr = 0 ";

		NCObject[] objs = getMDService().queryBillOfNCObjectByCond(AggConfirmPayHVO.class, sqlWhere, false);

		if (!ArrayUtil.isNull(objs)) {
			for (NCObject obj : objs) {
				volist.add((AggConfirmPayHVO) obj.getContainmentObject());
			}
		}
		return (AggConfirmPayHVO[]) volist.toArray(new AggConfirmPayHVO[0]);
	}

	public AggConfirmPayHVO[] queryAggConfirmPayHVOsByYurrefsAndWhere(String[] yurref, String whereSql) throws BusinessException {
		List<AggConfirmPayHVO> volist = new ArrayList();
		StringBuffer sqlWhere = new StringBuffer();
		sqlWhere.append(" pk_confirmpay_h in ( select cdtrdetail from ebank_confirmpay_b where ").append(SqlUtil.buildSqlForIn("yurref", yurref))
				.append(") and dr = 0 ");

		if (StringUtil.isNotNull(whereSql)) {
			sqlWhere.append(" and ").append(whereSql);
		}
		NCObject[] objs = getMDService().queryBillOfNCObjectByCond(AggConfirmPayHVO.class, sqlWhere.toString(), false);

		if (!ArrayUtil.isNull(objs)) {
			for (NCObject obj : objs) {
				volist.add((AggConfirmPayHVO) obj.getContainmentObject());
			}
		}
		return (AggConfirmPayHVO[]) volist.toArray(new AggConfirmPayHVO[0]);
	}

	private IMDPersistenceQueryService getMDService() {
		return (IMDPersistenceQueryService) NCLocator.getInstance().lookup(IMDPersistenceQueryService.class);
	}

	public String getEbankLinkQueryModuleID(String[] yurrefs) throws BusinessException {
		if (ArrayUtil.isNull(yurrefs)) {
			throw new BusinessException(MLObm.getStr00911());
		}
		String ebankModuleID = "36100CONFM";
		IYurrefRelationRegister relSev = (IYurrefRelationRegister) NCLocator.getInstance().lookup(IYurrefRelationRegister.class);

		String[] destYurrefs = relSev.findDestYurrefs(yurrefs);
		if ((destYurrefs != null) && (destYurrefs.length > 0)) {
			yurrefs = destYurrefs;
		}
		AggConfirmPayHVO[] billvo = ((IEbankconfirmpayMaintain) NCLocator.getInstance().lookup(IEbankconfirmpayMaintain.class))
				.queryAggConfirmPayHVOsByYurrefs(yurrefs);

		if (ArrayUtil.isNull(billvo)) {
			ebankModuleID = "36100PC";
		}
		return ebankModuleID;
	}
}

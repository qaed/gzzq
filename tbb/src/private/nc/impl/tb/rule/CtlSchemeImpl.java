package nc.impl.tb.rule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.naming.NamingException;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.mw.sqltrans.TempTable;
import nc.itf.mdm.cube.ICubeDefQueryService;
import nc.itf.mdm.dim.INtbSuper;
import nc.itf.tb.control.IBusiSysExecAllDataProvider;
import nc.itf.tb.control.IBusiSysExecDataProvider;
import nc.itf.tb.control.IBusiSysReg;
import nc.itf.tb.control.OutEnum;
import nc.itf.tb.rule.ICtlScheme;
import nc.itf.tb.sysmaintain.BusiSysReg;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.jdbc.framework.JdbcSession;
import nc.jdbc.framework.PersistenceManager;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.exception.DbException;
import nc.ms.mdm.convertor.IStringConvertor;
import nc.ms.mdm.convertor.StringConvertorFactory;
import nc.ms.mdm.cube.CubeServiceGetter;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ms.tb.control.AccountQryCache;
import nc.ms.tb.control.AlertPercentHandler;
import nc.ms.tb.control.BudgetControlCTL;
import nc.ms.tb.control.CtlBdinfoCTL;
import nc.ms.tb.control.CtlSchemeCTL;
import nc.ms.tb.control.CtrlRuleCTL;
import nc.ms.tb.control.CtrltacticsCache;
import nc.ms.tb.control.DataGetterContext;
import nc.ms.tb.control.SqlPartlyTools;
import nc.ms.tb.formula.script.CtrlBusinessException;
import nc.ms.tb.formula.util.CountTimeCost;
import nc.ms.tb.pub.NtbSuperDMO;
import nc.ms.tb.pubutil.CostTime;
import nc.ms.tb.rule.RuleCacheManager;
import nc.ms.tb.rule.SingleSchema;
import nc.ms.tb.rule.SubLevelOrgGetter;
import nc.ms.tb.rule.fmlset.FormulaCTL;
import nc.pubitf.accperiod.AccountCalendar;
import nc.pubitf.bd.accessor.GeneralAccessorFactory;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.pubitf.org.IAccountingBookPubService;
import nc.pubitf.org.ILiabilityBookPubService;
import nc.vo.bd.accessor.IBDData;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.cube.DataCellValue;
import nc.vo.mdm.cube.DimVector;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.sm.UserVO;
import nc.vo.tb.control.ConvertToCtrlSchemeVO;
import nc.vo.tb.control.CtrlSchemeVO;
import nc.vo.tb.control.IdSysregVO;
import nc.vo.tb.formula.DimFormulaVO;
import nc.vo.tb.obj.NtbParamVO;
import nc.vo.tb.prealarm.IdAlarmDimVectorVO;
import nc.vo.tb.prealarm.IdAlarmschemeVO;
import nc.vo.tb.rule.IdCtrlformulaVO;
import nc.vo.tb.rule.IdCtrlschemeVO;
import nc.vo.tb.task.MdTask;
import org.apache.commons.lang.RandomStringUtils;

public class CtlSchemeImpl implements ICtlScheme {
	public HashMap<String, ArrayList<String>> reloadZeroCtrlScheme() throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		HashMap<String, ArrayList<String>> map = new HashMap();
		StringBuffer sbStr = new StringBuffer();
		sbStr.append("PK_RULECLASS='TBRULE000SCHEMA_ZERO'");
		DimFormulaVO[] vos = (DimFormulaVO[]) dmo.queryByWhereClause(DimFormulaVO.class, sbStr.toString());
		String ctrlName = null;
		for (int n = 0; n < (vos == null ? 0 : vos.length); n++) {
			DimFormulaVO vo = vos[n];
			sbStr.setLength(0);
			sbStr.append("PK_PARENT='" + vo.getPrimaryKey() + "'");
			IdCtrlformulaVO[] formulaVos = (IdCtrlformulaVO[]) dmo.queryByWhereClause(IdCtrlformulaVO.class, sbStr.toString());
			if ((formulaVos != null) && (formulaVos.length > 0)) {
				ctrlName = formulaVos[0].getCtrlname();

				sbStr.setLength(0);
				StringBuffer nameShow = new StringBuffer();
				ArrayList<String> nameAndEntity = new ArrayList();
				for (int m = 0; m < formulaVos.length; m++) {
					IdCtrlformulaVO formulaVo = formulaVos[m];
					sbStr.setLength(0);
					sbStr.append("pk_ctrlformula='" + formulaVo.getPrimaryKey() + "'");
					IdCtrlschemeVO[] schemeVos = (IdCtrlschemeVO[]) dmo.queryByWhereClause(IdCtrlschemeVO.class, sbStr.toString());
					if ((schemeVos != null) && (schemeVos.length > 0)) {
						List<String> orgList = new ArrayList();
						for (int i = 0; i < schemeVos.length; i++) {
							String pk_org = schemeVos[i].getPk_org();
							String[] names = schemeVos[i].getNameidx().split(":");
							String[] pkidx = schemeVos[i].getStridx().split(":");
							if (!orgList.contains(pk_org)) {
								for (int k = 0; k < pkidx.length; k++) {
									String pk = pkidx[k];
									if (pk_org.equals(pk)) {
										nameShow.append(names[k]).append(",");
									}
								}
								orgList.add(pk_org);
							}
						}
					}
				}
				if ((nameShow != null) && (nameShow.length() > 0))
					nameShow.replace(nameShow.length() - 1, nameShow.length(), "");
				nameAndEntity.add(ctrlName);
				nameAndEntity.add(nameShow.toString());
				IUserManageQuery query = (IUserManageQuery) NCLocator.getInstance().lookup(IUserManageQuery.class);
				String userName = "";
				if (vo.getCreatedby() != null)
					userName = query.getUser(vo.getCreatedby()).getUser_name();
				nameAndEntity.add(userName);
				map.put(vo.getPrimaryKey(), nameAndEntity);
			}
		}
		return map;
	}

	public void deleteZeroCtrlScheme(ArrayList<String> pks, boolean delFormulaVO) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		HashMap<String, ArrayList<String>> map = new HashMap();
		StringBuffer sbStr = new StringBuffer();
		sbStr.append("PK_OBJ IN (");
		for (int n = 0; n < pks.size(); n++) {
			if (n != pks.size() - 1) {
				sbStr.append("'").append((String) pks.get(n)).append("',");
			} else {
				sbStr.append("'").append((String) pks.get(n)).append("'");
			}
		}
		sbStr.append(")");
		if (delFormulaVO) {
			dmo.deleteByWhereClause(DimFormulaVO.class, sbStr.toString());
		}
		sbStr.setLength(0);

		sbStr.append("PK_PARENT IN (");
		for (int n = 0; n < pks.size(); n++) {
			if (n != pks.size() - 1) {
				sbStr.append("'").append((String) pks.get(n)).append("',");
			} else {
				sbStr.append("'").append((String) pks.get(n)).append("'");
			}
		}
		sbStr.append(")");

		IdCtrlformulaVO[] vos = (IdCtrlformulaVO[]) dmo.queryByWhereClause(IdCtrlformulaVO.class, sbStr.toString());
		dmo.deleteByWhereClause(IdCtrlformulaVO.class, sbStr.toString());
		sbStr.setLength(0);

		if ((vos != null) && (vos.length > 0)) {
			sbStr.append("PK_CTRLFORMULA IN(");
			for (int n = 0; n < vos.length; n++) {
				if (n != vos.length - 1) {
					sbStr.append("'").append(vos[n].getPrimaryKey()).append("',");
				} else {
					sbStr.append("'").append(vos[n].getPrimaryKey()).append("'");
				}
			}

			sbStr.append(")");
			dmo.deleteByWhereClause(IdCtrlschemeVO.class, sbStr.toString());
		}
	}

	public String[] addCtrlformulas(ArrayList<IdCtrlformulaVO> vos) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String[] pks = dmo.insertArray((SuperVO[]) vos.toArray(new IdCtrlformulaVO[0]));
		return pks;
	}

	public String[] addCtrlScheme(ArrayList<IdCtrlschemeVO> vos) throws BusinessException {
		NtbSuperDMO dmo = new NtbSuperDMO();
		String[] pks = dmo.insertArray((SuperVO[]) vos.toArray(new IdCtrlschemeVO[0]));
		return pks;
	}

	public ArrayList<DimFormulaVO> queryDimFormulas(ArrayList<String> pks) throws BusinessException {
		ArrayList<DimFormulaVO> vosList = new ArrayList();
		NtbSuperDMO dmo = new NtbSuperDMO();
		HashMap<String, ArrayList<String>> map = new HashMap();
		StringBuffer sbStr = new StringBuffer();
		sbStr.append("PK_OBJ IN (");
		for (int n = 0; n < pks.size(); n++) {
			if (n != pks.size() - 1) {
				sbStr.append("'").append((String) pks.get(n)).append("',");
			} else {
				sbStr.append("'").append((String) pks.get(n)).append("'");
			}
		}
		sbStr.append(")");
		DimFormulaVO[] vos = (DimFormulaVO[]) dmo.queryByWhereClause(DimFormulaVO.class, sbStr.toString());
		vosList.addAll(Arrays.asList(vos));
		return vosList;
	}

	public ArrayList<IdCtrlformulaVO> queryCtrlFormula(String sWhere) throws BusinessException {
		ArrayList<IdCtrlformulaVO> returnvo = new ArrayList();
		try {
			NtbSuperDMO dmo = new NtbSuperDMO();
			IdCtrlformulaVO[] sdtasks = (IdCtrlformulaVO[]) dmo.queryByWhereClause(IdCtrlformulaVO.class, sWhere);
			for (int i = 0; i < (sdtasks == null ? 0 : sdtasks.length); i++) {
				IdCtrlformulaVO vo = sdtasks[i];
				returnvo.add(vo);
			}
		} catch (Exception e) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000208"));
		}
		return returnvo;
	}

	public void updateCtrlSchemeTable(NtbParamVO[] param) throws BusinessException {
		PersistenceManager manager = null;
		try {
			if ((param == null) || (param.length == 0))
				return;
			manager = PersistenceManager.getInstance();
			ArrayList listValues = new ArrayList();
			JdbcSession session = manager.getJdbcSession();
			for (int i = 0; i < param.length; i++) {
				listValues.add(param[i].getCtrlscheme());
			}
			String[] sqlParts = SqlPartlyTools.getBatchSQL("pk_obj", (String[]) listValues.toArray(new String[0]));

			ArrayList listsqls = new ArrayList();
			String tmpSql = "update tb_ctrlscheme set rundata = ? ,readydata = ? where pk_obj= ?";
			SQLParameter parameter = null;
			for (int i = 0; i < listValues.size(); i++) {
				int currtype = param[i].getCurr_type();
				// parameter = new SQLParameter();
				// parameter.addParam(param[i].getRundata()[currtype] == null ?
				// 0.0D : param[i].getRundata() == null ? 0.0D :
				// param[i].getRundata()[currtype].doubleValue()); //修复空指针
				// modified by jss 20161017
				parameter.addParam(param[i].getRundata() == null ? 0 : (param[i].getRundata()[currtype] == null ? 0.0D : param[i].getRundata() == null ? 0.0D : param[i].getRundata()[currtype].doubleValue())); // 修复空指针
																																																					// modified
																																																					// by
																																																					// LinZB

				// parameter.addParam(param[i].getReadydata()[currtype] == null
				// ? 0.0D : param[i].getReadydata() == null ? 0.0D :
				// param[i].getReadydata()[currtype].doubleValue()); //修复空指针
				// modified by jss 20161017
				parameter.addParam(param[i].getReadydata() == null ? 0 : (param[i].getReadydata()[currtype] == null ? 0.0D : param[i].getReadydata() == null ? 0.0D : param[i].getReadydata()[currtype].doubleValue())); // 修复空指针
																																																							// modified
																																																							// by
																																																							// LinZB
																																																							// 20160906

				parameter.addParam(listValues.get(i));
				session.addBatch(tmpSql, parameter);
			}

			session.executeBatch();
		} catch (DbException dbe) {
			NtbLogger.error(dbe);
			throw new DAOException(dbe);
		} finally {
			if (manager != null)
				manager.release();
		}
	}

	public IdCtrlschemeVO[] convertIdCtrlscheme(SingleSchema schema) throws Exception {
		CountTimeCost getUfidScheme = new CountTimeCost();
		getUfidScheme.beginCost();

		String[] src_ufind = schema.getUFind();
		String[] src_prefind = schema.getPREUFind();
		List<ConvertToCtrlSchemeVO> convertorList = new ArrayList();
		List<IdCtrlschemeVO> schemeList = new ArrayList();

		if (src_ufind != null) {
			for (String ufind : src_ufind) {
				ConvertToCtrlSchemeVO vo = new ConvertToCtrlSchemeVO(ufind, "UFIND");

				convertorList.add(vo);
			}
		}
		if (src_prefind != null) {
			for (String prefind : src_prefind) {
				ConvertToCtrlSchemeVO vo = new ConvertToCtrlSchemeVO(prefind, "PREFIND");

				convertorList.add(vo);
			}
		}
		try {
			for (ConvertToCtrlSchemeVO convertor : convertorList) {
				IdCtrlschemeVO schemevos = new IdCtrlschemeVO();
				if ((convertor.getPkOrg().equals("null")) || (convertor.getPkOrg().equals(""))) {
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000212"));
				}

				schemevos.setPk_org(convertor.getPkOrg());

				if ((convertor.getPkCurrency().equals("null")) || (convertor.getPkCurrency().equals(""))) {
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000178"));
				}

				schemevos.setPk_currency(convertor.getPkCurrency());

				getUfidScheme.addCost("Ufida schema 1", getUfidScheme.getCost());

				schemevos.setStridx(CtlBdinfoCTL.getActualPk(convertor));
				getUfidScheme.addCost("Ufida schema 2", getUfidScheme.getCost());

				schemevos.setMethodname(convertor.getMethodFunc());
				schemevos.setCtrlsys(convertor.getCtrlSys());
				schemevos.setBilltype(convertor.getBillType());
				schemevos.setCtrldirection(convertor.getCtrlDirection());
				schemevos.setCtrlobj(convertor.getCtrlObject());
				schemevos.setCtrlobjValue(convertor.getCtrlObjectValue());
				schemevos.setIncludeuneffected(convertor.getUneffenctdata());
				schemevos.setStartdate(convertor.getStartDate());
				schemevos.setEnddate(convertor.getEndDate());
				schemevos.setAccctrollflag(convertor.getAccCtrlFlag());
				schemevos.setPk_org(convertor.getPkOrg());
				schemevos.setCurrtype(Integer.valueOf(getCurrencyType(convertor.getPkCurrency())));
				schemevos.setPk_currency(convertor.getPkCurrency());
				schemevos.setPk_ncentity(convertor.getPkOrg());
				schemevos.setFromitems(convertor.getFromItem());
				schemevos.setCodeidx(filterContent(convertor.getCodeIdx()));
				schemevos.setCtllevels(convertor.getCtrlLevel());
				schemevos.setRundata(new UFDouble(0));
				schemevos.setReadydata(new UFDouble(0));
				schemevos.setNameidx(filterContent(convertor.getNameIdx()));
				schemevos.setDatetype(convertor.getDataCatalg());
				schemeList.add(schemevos);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception ex) {
			NtbLogger.error(ex);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000213"));
		}

		return (IdCtrlschemeVO[]) schemeList.toArray(new IdCtrlschemeVO[0]);
	}

	private String filterContent(String content) {
		String _REPLACE = "@CONTENT@";
		if ((content != null) && (content.contains(_REPLACE)))
			content = content.replaceAll(_REPLACE, ",");
		return content;
	}

	public int getCurrencyType(String src) {
		int type = 3;

		if (src.equals("@@@@Z0GLOBE000000001")) {
			type = 0;
		}

		if (src.equals("@@@@Z0GROUP000000001")) {
			type = 1;
		}

		if (src.equals("@@@@Z0ORG00000000001")) {
			type = 2;
		}
		return type;
	}

	private HashMap<String, ArrayList<NtbParamVO>> sortVOsBySys(IdCtrlschemeVO[] ctlvos) throws Exception {
		NtbParamVO[] params = parseCtrls(ctlvos);
		return getParamMapBySys(params);
	}

	private HashMap<String, ArrayList<NtbParamVO>> sortVOsBySys(Map<Integer, IdCtrlschemeVO[]> schemeMap) throws Exception {
		List<NtbParamVO> paramList = new ArrayList();
		for (Map.Entry<Integer, IdCtrlschemeVO[]> entry : schemeMap.entrySet()) {
			NtbParamVO[] params = parseCtrls((IdCtrlschemeVO[]) entry.getValue());
			for (NtbParamVO param : params) {
				param.setNtbparamvoId(((Integer) entry.getKey()).toString());
				paramList.add(param);
			}
		}
		return getParamMapBySys((NtbParamVO[]) paramList.toArray(new NtbParamVO[0]));
	}

	private HashMap<String, ArrayList<NtbParamVO>> getParamMapBySys(NtbParamVO[] params) {
		HashMap<String, ArrayList<NtbParamVO>> map = new HashMap();
		for (int i = 0; i < params.length; i++) {
			String sys = params[i].getSys_id();
			if (map.containsKey(sys)) {
				ArrayList<NtbParamVO> list = (ArrayList) map.get(sys);

				list.add(params[i]);
			} else {
				ArrayList<NtbParamVO> list = new ArrayList();
				list.add(params[i]);
				map.put(sys, list);
			}
		}

		return map;
	}

	private String parseBillTypes(String billtyes) {
		if ((billtyes == null) || (billtyes.trim().length() == 0)) {
			return null;
		}
		// 20170123_1 tsy 用首先把全部中文去掉：[642-012-za]差旅费报销单 -> [642-012-za]
		// billtyes = billtyes.replaceAll("[\u4e00-\u9fa5]+", "");
		// 20170123_1 end
		// 20170209 tsy 上面缺少了2种情况:1.[642]报销单（什么、什么） 2.[642-012-za]X报销、B报销
		billtyes = billtyes.replaceAll("[\u4e00-\u9fa5 、（）]*", "");
		// 20170209 end
		StringBuffer buffer = new StringBuffer();
		if (billtyes.indexOf("[") >= 0) {
			while (billtyes.indexOf("]") >= 0) {
				if (buffer.toString().length() == 0) {
					buffer.append(billtyes.substring(billtyes.indexOf("[") + 1, billtyes.indexOf("]")));
				} else {
					buffer.append("#");
					buffer.append(billtyes.substring(billtyes.indexOf("[") + 1, billtyes.indexOf("]")));
				}

				billtyes = billtyes.substring(billtyes.indexOf("]") + 1);
			}
		}
		buffer.append(billtyes);

		return buffer.toString();
	}

	private String getPKORGByFINANCEId(String pk_finance) throws BusinessException {
		String[] pk_orgs = { pk_finance };
		String pk_accountingBook = null;

		IAccountingBookPubService bookPubService = (IAccountingBookPubService) NCLocator.getInstance().lookup(IAccountingBookPubService.class.getName());

		Map<String, String> map = bookPubService.queryAccountingBookIDByFinanceOrgIDWithMainAccountBook(pk_orgs);

		if (map == null) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000207"));
		}

		pk_accountingBook = (String) map.get(pk_finance);
		if (pk_accountingBook == null) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000207"));
		}

		return pk_accountingBook;
	}

	private AccountCalendar getAccountCalendar(String accperiod) throws BusinessException {
		AccountCalendar accountCalendar = AccountCalendar.getInstanceByPeriodScheme(accperiod);
		return accountCalendar;
	}

	public void dealAccountDate(NtbParamVO vo) throws BusinessException {
		String sysid = vo.getSys_id();
		boolean iskjqj = vo.isKjqj();
		IBusiSysReg resaReg = CtlSchemeCTL.getBusiSysReg(sysid);
		boolean isUseAccountDate = resaReg.isUseAccountDate(vo.getBill_type());
		if ((isUseAccountDate) || (iskjqj)) {
			if (sysid.equals(OutEnum.HRPSYS)) {
				return;
			}

			IGeneralAccessor financeorg_accesssor = GeneralAccessorFactory.getAccessor("2cfe13c5-9757-4ae8-9327-f5c2d34bcb46");

			IBDData financeorg_bddata = financeorg_accesssor.getDocByPk(vo.getPk_Org());

			String pk_accountingBook = null;
			if (OutEnum.RESASYS.equals(vo.getSys_id())) {
				financeorg_accesssor = GeneralAccessorFactory.getAccessor("13a0d3b2-4d5b-4314-9e75-481193f993f2");

				financeorg_bddata = financeorg_accesssor.getDocByPk(vo.getPk_Org());

				if (financeorg_bddata != null) {
					pk_accountingBook = vo.getPk_Org();
				}
			}
			if (financeorg_bddata == null) {
				String start = vo.getBegDate();
				String end = vo.getEndDate();
				String _strat = start.substring(0, 7);
				vo.setBegDate(_strat);
				String _end = end.substring(0, 7);
				vo.setEndDate(_end);
			} else {
				if (pk_accountingBook == null) {
					pk_accountingBook = getPKORGByFINANCEId(vo.getPk_Org());
				}
				if (pk_accountingBook == null) {
					String start = vo.getBegDate();
					String end = vo.getEndDate();
					String _strat = start.substring(0, 7);
					vo.setBegDate(_strat);
					String _end = end.substring(0, 7);
					vo.setEndDate(_end);
				} else {
					String accperiod = null;
					if (OutEnum.RESASYS.equals(vo.getSys_id())) {
						ILiabilityBookPubService bookPubService = (ILiabilityBookPubService) NCLocator.getInstance().lookup(ILiabilityBookPubService.class.getName());

						accperiod = bookPubService.queryAccperiodCalendarIDByLiabilityBookID(pk_accountingBook);
					} else {
						IAccountingBookPubService bookPubService = (IAccountingBookPubService) NCLocator.getInstance().lookup(IAccountingBookPubService.class.getName());

						accperiod = bookPubService.queryAccperiodSchemeByAccountingBookID(pk_accountingBook);
					}

					AccountCalendar accountCalendar = getAccountCalendar(accperiod);
					String start = vo.getBegDate();
					String end = vo.getEndDate();
					if (start.length() > 7) {
						accountCalendar.setDate(new UFDate(start));
						vo.setBegDate(accountCalendar.getMonthVO().getYearmth());
					}
					if (end.length() > 7) {
						accountCalendar.setDate(new UFDate(end));
						vo.setEndDate(accountCalendar.getMonthVO().getYearmth());
					}
				}
			}

			if ((OutEnum.IASYS.equals(sysid)) && ("JC".equals(vo.getBill_type())))
				vo.setBegDate(vo.getEndDate());
		}
	}

	public String[] filterStridx(IdCtrlschemeVO vo, String[] bdinfotypeidx) throws Exception {
		String sysid = vo.getCtrlsys();
		String[] att = new String[5];
		StringBuffer bf_PkDim = new StringBuffer();
		StringBuffer bf_BusiAttrs = new StringBuffer();
		StringBuffer bf_Includelower = new StringBuffer();
		StringBuffer bf_TypeDim = new StringBuffer();
		StringBuffer bf_Code_dims = new StringBuffer();

		String[] stridx = vo.getStridx().split(":");
		String[] fromitem = vo.getFromitems().split(":");
		String[] ctllevel = vo.getCtllevels().split(":");
		String[] nameidx = vo.getNameidx().split(":");
		String[] codeidx = vo.getCodeidx().split(":");
		String[] bdinfotype = BudgetControlCTL.getBdinfoType(fromitem, sysid).split(":");

		for (int i = 0; i < fromitem.length; i++) {
			if ((!stridx[i].equals(OutEnum.NOSUCHBASEPKATSUBCORP)) || (!bdinfotype[i].equals(OutEnum.CURRDOC))) {
				bf_PkDim.append(stridx[i] + ":");
				bf_BusiAttrs.append(fromitem[i] + ":");
				bf_Includelower.append(ctllevel[i] + ":");
				bf_TypeDim.append(bdinfotypeidx[i] + ":");
				bf_Code_dims.append(codeidx[i] + ":");
			}
		}
		att[0] = bf_PkDim.toString();
		att[1] = bf_BusiAttrs.toString();
		att[2] = bf_Includelower.toString();
		att[3] = bf_TypeDim.toString();
		att[4] = bf_Code_dims.toString();
		return att;
	}

	public String[] startCtrlSchemeVOs(IdCtrlschemeVO[] schvos) throws Exception {
		HashMap paraMap = sortVOsBySys(schvos);
		ArrayList<String> infolist = new ArrayList();
		Iterator itor = paraMap.keySet().iterator();
		while (itor.hasNext()) {
			String src = (String) itor.next();
			ArrayList ls = (ArrayList) paraMap.get(src);
			NtbParamVO[] params = (NtbParamVO[]) ls.toArray(new NtbParamVO[0]);
			IBusiSysExecDataProvider exeprovider = getExcProvider(src);

			setIncludeEff(exeprovider, params);

			ArrayList<NtbParamVO> ufindVO = new ArrayList();
			ArrayList<NtbParamVO> prefindVO = new ArrayList();
			for (int n = 0; n < params.length; n++) {
				if (params[n].getMethodCode().equals("UFIND")) {
					ufindVO.add(params[n]);
				} else if (params[n].getMethodCode().equals("PREFIND")) {
					prefindVO.add(params[n]);
				}
			}
			UFDouble[][] ufindretdatas = (UFDouble[][]) null;
			UFDouble[][] prefindretdatas = (UFDouble[][]) null;
			if ((exeprovider instanceof IBusiSysExecAllDataProvider)) {
				((IBusiSysExecAllDataProvider) exeprovider).setAllNtbParamVO(params);
			}
			if (ufindVO.size() > 0) {
				ufindretdatas = exeprovider.getExecDataBatch((NtbParamVO[]) ufindVO.toArray(new NtbParamVO[0]));
			}

			if (prefindVO.size() > 0) {
				prefindretdatas = exeprovider.getReadyDataBatch((NtbParamVO[]) prefindVO.toArray(new NtbParamVO[0]));
			}

			if (ufindretdatas != null) {
				for (int j = 0; j < ufindVO.size(); j++) {
					((NtbParamVO) ufindVO.get(j)).setRundata(ufindretdatas[j]);
				}
			}
			if (prefindretdatas != null) {
				for (int j = 0; j < prefindVO.size(); j++) {
					((NtbParamVO) prefindVO.get(j)).setReadydata(prefindretdatas[j]);
				}
			}

			ArrayList<NtbParamVO> allNtbParamVO = new ArrayList();
			allNtbParamVO.addAll(ufindVO);
			allNtbParamVO.addAll(prefindVO);

			params = (NtbParamVO[]) allNtbParamVO.toArray(new NtbParamVO[0]);

			NtbParamVO[] mergeParamVos = params;

			CtlSchemeCTL.updateCtrlSchemeTable(mergeParamVos);
		}
		return (String[]) infolist.toArray(new String[0]);
	}

	private NtbParamVO[] parseCtrls(IdCtrlschemeVO[] ctlvos) throws Exception {
		try {
			String spliter = ":";
			IBusiSysReg resaReg = null;
			ArrayList<NtbParamVO> listParams = new ArrayList();
			for (int i = 0; i < ctlvos.length; i++) {
				NtbParamVO paramvo = new NtbParamVO();
				String funName = null;
				if (ctlvos[i].getMethodname() != null) {
					funName = ctlvos[i].getMethodname();
				}

				String pk_org = ctlvos[i].getPk_org();
				String billtype = parseBillTypes(ctlvos[i].getBilltype());
				String sysId = ctlvos[i].getCtrlsys();
				paramvo.setMethodCode(funName);
				paramvo.setSys_id(sysId);

				if (billtype != null) {
					if (billtype.indexOf(",") <= 0) {
						CtrltacticsCache.getNewInstance();
						// 20170123_2 tsy
						// 简化代码:parseBillTypes(ctlvos[i].getBilltype()) ->
						// billtype
						// 没必要多次使用parseBillTypes对字符串进行解析
						// HashMap actionMap =
						// CtrltacticsCache.getActionByBillTypeAndSysId(paramvo.getSys_id(),
						// parseBillTypes(ctlvos[i].getBilltype()),
						// paramvo.getMethodCode());
						HashMap actionMap = CtrltacticsCache.getActionByBillTypeAndSysId(paramvo.getSys_id(), billtype, paramvo.getMethodCode());
						// 20170123_2 end
						paramvo.setActionMap(actionMap);
						HashMap _map = new HashMap();
						_map.put(billtype, actionMap);
						paramvo.setBillTypesActionMap(_map);
					} else {
						String[] billtypes = parseBillTypes(ctlvos[i].getBilltype()).split(",");

						HashMap map = new HashMap();
						for (int n = 0; n < billtypes.length; n++) {
							String _billtype = billtypes[n];
							CtrltacticsCache.getNewInstance();
							HashMap actionMap = CtrltacticsCache.getActionByBillTypeAndSysId(paramvo.getSys_id(), _billtype, paramvo.getMethodCode());

							map.put(_billtype, actionMap);
						}
						paramvo.setBillTypesActionMap(map);
					}

				}

				paramvo.setIsUnInure(ctlvos[i].getIncludeuneffected().booleanValue());

				if (OutEnum.RESASYS.equalsIgnoreCase(sysId)) {
					resaReg = CtlSchemeCTL.getBusiSysReg(OutEnum.RESASYS);
					paramvo.setIsKjqj(resaReg.isUseAccountDate(billtype));
				} else {
					paramvo.setIsKjqj(false);
				}
				if (ctlvos[i].getStartdate() != null) {
					paramvo.setBegDate(ctlvos[i].getStartdate());
					paramvo.setEndDate(ctlvos[i].getEnddate());
				}
				paramvo.setPk_Org(pk_org);
				paramvo.setBill_type(billtype);
				paramvo.setData_attr(ctlvos[i].getCtrlobj());
				paramvo.setData_attrExt(ctlvos[i].getCtrlobjValue());

				dealAccountDate(paramvo);

				paramvo.setPk_ctrl(ctlvos[i].getPrimaryKey());

				paramvo.setGroupname(ctlvos[i].getPk_ctrlformula());

				paramvo.setPk_org_book(pk_org);

				paramvo.setPk_Group(InvocationInfoProxy.getInstance().getGroupId());
				paramvo.setPk_accentity(pk_org);
				String bdinfotype = CtlSchemeCTL.getBdinfoType(ctlvos[i].getFromitems(), ctlvos[i].getCtrlsys());

				String[] bdinfotypeidx = bdinfotype.split(spliter);

				String[] ctrllevel = ctlvos[i].getCtllevels().split(spliter);

				boolean isControlDownCorp = false;
				for (int j = 0; j < bdinfotypeidx.length; j++) {
					if ((!bdinfotypeidx[j].equals(OutEnum.ZJORG)) && (!bdinfotypeidx[j].equals(OutEnum.XSOGR)) && (!bdinfotypeidx[j].equals(OutEnum.ZHANGBU)))
						continue;
					Boolean value = new Boolean(ctrllevel[j]);
					isControlDownCorp = value.booleanValue();
					break;
				}

				boolean isControlAllCorp = false;

				String pk_currency = CtlSchemeCTL.getPk_currency(ctlvos[i].getPk_currency(), ctlvos[i].getPk_org(), sysId);
				paramvo.setPk_currency(pk_currency);

				paramvo.setCurr_type(CtlSchemeCTL.getCurrencyType(ctlvos[i].getPk_currency()));

				paramvo.setSys_id(ctlvos[i].getCtrlsys());

				paramvo.setDateType(ctlvos[i].getDatetype());
				paramvo.setDirection(ctlvos[i].getCtrldirection());

				paramvo.setCtrlstatus(0);

				String[] att = filterStridx(ctlvos[i], bdinfotypeidx);
				paramvo.setPkDim(att[0].split(spliter));
				paramvo.setBusiAttrs(att[1].split(spliter));
				String[] ctrllevels = att[2].split(spliter);
				boolean[] value = new boolean[ctrllevels.length];
				HashMap<String, String[]> leveldownMap = new HashMap();
				for (int j = 0; j < ctrllevels.length; j++) {
					value[j] = UFBoolean.valueOf(ctrllevels[j]).booleanValue();

					if (value[j]) {
						if (!paramvo.getPkDim()[j].equals(paramvo.getPk_Org())) {
							String[] levelDowsPks = CtlBdinfoCTL.getBdChilddataVO(paramvo.getPkDim()[j], paramvo.getBusiAttrs()[j], paramvo.getPk_Org(), paramvo.getSys_id(), true);

							leveldownMap.put(paramvo.getBusiAttrs()[j], levelDowsPks);
						} else {
							SubLevelOrgGetter orgLevGetter = new SubLevelOrgGetter();
							String[] levelDownPks = orgLevGetter.getSubLevelOrgsByOrgAndBd(paramvo.getPk_Org(), paramvo.getBusiAttrs()[j], paramvo.getSys_id());

							leveldownMap.put(paramvo.getBusiAttrs()[j], levelDownPks);
						}
					}
				}
				paramvo.setLowerArrays(leveldownMap);
				paramvo.setIncludelower(value);
				paramvo.setTypeDim(att[3].split(spliter));
				paramvo.setCode_dims(att[4].split(spliter));
				paramvo.setVarno(ctlvos[i].getVarno());
				paramvo.setCtrlscheme(ctlvos[i].getPrimaryKey());

				if (isControlDownCorp) {
					listParams.add(paramvo);
				} else if (isControlAllCorp) {
					listParams.add(paramvo);
					CtlSchemeCTL.addGroupDownAllOrgParams(paramvo, listParams, ctlvos[i]);
				} else {
					listParams.add(paramvo);
				}
				CtlSchemeCTL.validateNtbParamVO(paramvo);
			}
			return (NtbParamVO[]) listParams.toArray(new NtbParamVO[0]);
		} catch (Exception ex) {
			NtbLogger.error(ex);
			throw ex;
		}
	}

	private IBusiSysExecDataProvider getExcProvider(String sys) throws BusinessException {
		IBusiSysExecDataProvider provider = null;
		try {
			BusiSysReg sysreg = BusiSysReg.getSharedInstance();
			IdSysregVO[] sysregvos = sysreg.getAllSysVOs();
			for (int i = 0; i < sysregvos.length; i++) {
				if (sysregvos[i].getSysid().equals(sys))
					provider = ((IBusiSysReg) Class.forName(sysregvos[i].getRegclass()).newInstance()).getExecDataProvider();
			}
		} catch (Exception ex) {
			NtbLogger.printException(ex);
			throw new BusinessException(ex);
		}
		return provider;
	}

	public NtbParamVO[] getExcProviderFunc(String sys, NtbParamVO[] params) throws BusinessException {
		IBusiSysExecDataProvider provider = getExcProvider(sys);
		try {
			setIncludeEff(provider, params);
		} catch (Exception ex) {
			NtbLogger.print(ex);
		}
		return params;
	}

	private NtbParamVO[] setIncludeEff(IBusiSysExecDataProvider exeprovider, NtbParamVO[] params) throws Exception {
		HashMap hashCorp2Point = new HashMap();
		for (int i = 0; i < params.length; i++) {
			int ctlpoint = 0;
			boolean isIncludeeff = false;

			if (hashCorp2Point.containsKey(params[i].getPk_Org())) {
				ctlpoint = ((Integer) hashCorp2Point.get(params[i].getPk_Org())).intValue();
			} else {
				try {
					ctlpoint = exeprovider.getCtlPoint(params[i].getPk_Org());
				} catch (Exception ex) {
					NtbLogger.error(ex);
					ctlpoint = 0;
				}
				hashCorp2Point.put(params[i].getPk_Org(), Integer.valueOf(ctlpoint));
			}

			if (ctlpoint == 0) {
				isIncludeeff = true;
			} else if (ctlpoint == 1) {
				isIncludeeff = false;
			}
			params[i].setIsUnInure(isIncludeeff);
		}

		return params;
	}

	public HashMap setFormulaMap(IdCtrlformulaVO vo, IdCtrlschemeVO[] vos, HashMap map) {
		if (map.containsKey(vo.getPrimaryKey())) {
			ArrayList list = (ArrayList) map.get(vo.getPrimaryKey());
			for (int i = 0; i < vos.length; i++) {
				list.add(vos[i]);
			}
		} else {
			ArrayList list = new ArrayList();
			for (int i = 0; i < vos.length; i++) {
				list.add(vos[i]);
			}
			map.put(vo.getPrimaryKey(), list);
		}
		return map;
	}

	private String getArraysStr(String[] values) {
		StringBuffer sbStr = new StringBuffer();
		for (int n = 0; n < values.length; n++) {
			if (n == values.length - 1) {
				sbStr.append(values[n]);
			} else {
				sbStr.append(values[n]).append(":");
			}
		}
		return sbStr.toString();
	}

	public String[] startCtrlScheme(ArrayList<CtrlSchemeVO> vos) throws BusinessException {
		ArrayList<IdCtrlformulaVO> formulavoList = new ArrayList();
		ArrayList<IdCtrlschemeVO> schemevoList = new ArrayList();
		HashMap<String, ArrayList<IdCtrlschemeVO>> m_map = new HashMap();
		HashMap<String, IdCtrlformulaVO> n_map = new HashMap();
		ArrayList<String> infolist = new ArrayList();
		HashMap<IdCtrlformulaVO, IdCtrlschemeVO[]> mapVos = new HashMap();
		CostTime time = new CostTime();
		for (int n = 0; n < vos.size(); n++) {
			CtrlSchemeVO vo = (CtrlSchemeVO) vos.get(n);
			String pk_formula = vo.getPk_formula();
			String pk_task = vo.getPk_task();
			DataCell datacell = vo.getAllotCell();
			String express = vo.getCellExpress();

			express = CtrlRuleCTL.deposeRuleExpress(express);

			try {
				DimFormulaVO dimformulaVO = RuleCacheManager.getNewInstance().getDimFormulaVOByPk(pk_formula);
				SingleSchema schemea = null;
				if (dimformulaVO == null)
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0", "01050ctrl003-0001") + pk_formula + NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0", "01050ctrl003-0002"));
				if ((dimformulaVO.getPk_ruleclass().equals("TBRULE0SCHEMA_SINGLE")) || (dimformulaVO.getPk_ruleclass().equals("TBRULE000SCHEMA_SPEC"))) {
					schemea = CtlSchemeCTL.singleAndSepSchema(express, datacell, datacell.getCubeDef(), dimformulaVO.getPk_ruleclass());
				} else if (dimformulaVO.getPk_ruleclass().equals("TBRULE00SCHEMA_GROUP")) {
					schemea = CtlSchemeCTL.groupSchema(express, datacell, datacell.getCubeDef());
				} else {
					schemea = CtlSchemeCTL.singleAndSepSchema(express, datacell, datacell.getCubeDef(), dimformulaVO.getPk_ruleclass());
				}
				ArrayList<IdCtrlschemeVO> lm = CtlSchemeCTL.convertIdCtrlscheme(schemea, pk_formula);
				IdCtrlschemeVO[] schemevos = (IdCtrlschemeVO[]) lm.toArray(new IdCtrlschemeVO[0]);
				vo.setSchemevos(schemevos);
				IdCtrlformulaVO formulavo = CtlSchemeCTL.convertIdCtrlFormula(datacell, schemea, schemevos, pk_formula);
				formulavo.setPk_plan(pk_task);
				vo.setFormulavo(formulavo);
				formulavoList.add(formulavo);
			} catch (Exception ex) {
				throw new BusinessException(ex.getMessage());
			}
		}
		time.printStepCost(vos.size() + NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0", "01050rule001-0040"));
		try {
			String[] pks = CtlSchemeCTL.addCtrlformulas(formulavoList);

			for (int n = 0; n < vos.size(); n++) {
				CtrlSchemeVO vo = (CtrlSchemeVO) vos.get(n);
				for (int m = 0; m < (vo.getSchemevos() == null ? 0 : vo.getSchemevos().length); m++) {
					vo.getSchemevos()[m].setPk_ctrlformula(pks[n]);
					vo.getSchemevos()[m].setPk_plan(vo.getPk_task());
				}
				schemevoList.addAll(Arrays.asList(vo.getSchemevos()));
				mapVos.put(vo.getFormulavo(), vo.getSchemevos());
			}
			String[] _pks = CtlSchemeCTL.addCtrlScheme(schemevoList);
			for (int n = 0; n < schemevoList.size(); n++) {
				IdCtrlschemeVO vo = (IdCtrlschemeVO) schemevoList.get(n);
				vo.setPrimaryKey(_pks[n]);
			}
			time.printStepCost(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0", "01050rule001-0041"));
			if (formulavoList.size() > 0) {
				Iterator iter = mapVos.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					IdCtrlformulaVO key = (IdCtrlformulaVO) entry.getKey();
					IdCtrlschemeVO[] schemeVOS = (IdCtrlschemeVO[]) entry.getValue();
					for (int n = 0; n < schemeVOS.length; n++) {
						schemeVOS[n].setPk_ctrlformula(key.getPrimaryKey());
					}
					setFormulaMap(key, schemeVOS, m_map);
					n_map.put(key.getPrimaryKey(), key);
				}
			}
			AlertPercentHandler.updSectionsWithCtrlScheme((CtrlSchemeVO[]) vos.toArray(new CtrlSchemeVO[0]));
			HashMap paraMap = CtlSchemeCTL.sortVOsBySys((IdCtrlschemeVO[]) schemevoList.toArray(new IdCtrlschemeVO[0]));
			Iterator itor = paraMap.keySet().iterator();
			while (itor.hasNext()) {
				String src = (String) itor.next();
				ArrayList ls = (ArrayList) paraMap.get(src);
				NtbParamVO[] params = (NtbParamVO[]) ls.toArray(new NtbParamVO[0]);
				IBusiSysExecDataProvider exeprovider = getExcProvider(src);

				CtlSchemeCTL.setIncludeEff(exeprovider, params);

				ArrayList ufindVO = new ArrayList();
				ArrayList prefindVO = new ArrayList();
				for (int n = 0; n < params.length; n++) {
					if (params[n].getMethodCode().equals("UFIND")) {
						ufindVO.add(params[n]);
					} else if (params[n].getMethodCode().equals("PREFIND")) {
						prefindVO.add(params[n]);
					}
				}
				UFDouble[][] ufindretdatas = (UFDouble[][]) null;
				UFDouble[][] prefindretdatas = (UFDouble[][]) null;
				if ((exeprovider instanceof IBusiSysExecAllDataProvider)) {
					((IBusiSysExecAllDataProvider) exeprovider).setAllNtbParamVO(params);
				}

				if (ufindVO.size() > 0) {
					ufindretdatas = exeprovider.getExecDataBatch((NtbParamVO[]) ufindVO.toArray(new NtbParamVO[0]));

					time.printStepCost(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0", "01050rule001-0042") + ufindVO.size() + NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0", "01050rule001-0043"));
				}
				if (prefindVO.size() > 0) {
					prefindretdatas = exeprovider.getReadyDataBatch((NtbParamVO[]) prefindVO.toArray(new NtbParamVO[0]));

					time.printStepCost(new StringBuilder().append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0", "01050rule001-0042")).append(prefindVO.size()).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0", "01050rule001-0044")).toString());
				}

				if (ufindretdatas != null) {
					for (int j = 0; j < ufindVO.size(); j++) {
						((NtbParamVO) ufindVO.get(j)).setRundata(ufindretdatas[j]);
					}
				}
				if (prefindretdatas != null) {
					for (int j = 0; j < prefindVO.size(); j++) {
						((NtbParamVO) prefindVO.get(j)).setReadydata(prefindretdatas[j]);
					}
				}

				ArrayList allNtbParamVO = new ArrayList();
				allNtbParamVO.addAll(ufindVO);
				allNtbParamVO.addAll(prefindVO);

				params = (NtbParamVO[]) allNtbParamVO.toArray(new NtbParamVO[0]);

				NtbParamVO[] mergeParamVos = params;
				String[] info = null;

				info = CtlSchemeCTL.compare(mergeParamVos, m_map, n_map, vos);
				for (int m = 0; m < info.length; m++) {
					infolist.add(info[m]);
				}
				CtlSchemeCTL.updateCtrlSchemeTable(mergeParamVos);
				time.printStepCost(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0", "01050rule001-0045"));
			}
		} catch (Exception ex) {
			throw new BusinessException(ex);
		}

		return (String[]) (String[]) infolist.toArray(new String[0]);
	}

	public void startCtrlSchemeBySchemeVO(List<IdCtrlschemeVO> schemevoList) throws BusinessException {
		try {
			HashMap paraMap = CtlSchemeCTL.sortVOsBySys((IdCtrlschemeVO[]) schemevoList.toArray(new IdCtrlschemeVO[0]));
			Iterator itor = paraMap.keySet().iterator();
			while (itor.hasNext()) {
				String src = (String) itor.next();
				ArrayList ls = (ArrayList) paraMap.get(src);
				NtbParamVO[] params = (NtbParamVO[]) (NtbParamVO[]) ls.toArray(new NtbParamVO[0]);
				IBusiSysExecDataProvider exeprovider = getExcProvider(src);

				CtlSchemeCTL.setIncludeEff(exeprovider, params);

				ArrayList ufindVO = new ArrayList();
				ArrayList prefindVO = new ArrayList();
				for (int n = 0; n < params.length; n++) {
					if (params[n].getMethodCode().equals("UFIND"))
						ufindVO.add(params[n]);
					else if (params[n].getMethodCode().equals("PREFIND")) {
						prefindVO.add(params[n]);
					}
				}
				UFDouble[][] ufindretdatas = (UFDouble[][]) null;
				UFDouble[][] prefindretdatas = (UFDouble[][]) null;
				if ((exeprovider instanceof IBusiSysExecAllDataProvider)) {
					((IBusiSysExecAllDataProvider) exeprovider).setAllNtbParamVO(params);
				}

				if (ufindVO.size() > 0) {
					ufindretdatas = exeprovider.getExecDataBatch((NtbParamVO[]) ufindVO.toArray(new NtbParamVO[0]));
				}

				if (prefindVO.size() > 0) {
					prefindretdatas = exeprovider.getReadyDataBatch((NtbParamVO[]) prefindVO.toArray(new NtbParamVO[0]));
				}

				if (ufindretdatas != null) {
					for (int j = 0; j < ufindVO.size(); j++) {
						((NtbParamVO) ufindVO.get(j)).setRundata(ufindretdatas[j]);
					}
				}
				if (prefindretdatas != null) {
					for (int j = 0; j < prefindVO.size(); j++) {
						((NtbParamVO) prefindVO.get(j)).setReadydata(prefindretdatas[j]);
					}
				}

				ArrayList allNtbParamVO = new ArrayList();
				allNtbParamVO.addAll(ufindVO);
				allNtbParamVO.addAll(prefindVO);

				params = (NtbParamVO[]) allNtbParamVO.toArray(new NtbParamVO[0]);

				NtbParamVO[] mergeParamVos = params;

				CtlSchemeCTL.updateCtrlSchemeTable(mergeParamVos);
			}
		} catch (Exception ex) {
			throw new BusinessException(ex);
		}
	}

	public NtbParamVO[] getExeData(String[] formulaExpress) throws BusinessException {
		try {
			CountTimeCost ufindGetData = new CountTimeCost();
			ufindGetData.beginCost();
			ArrayList<NtbParamVO> paramvos = new ArrayList();
			ArrayList<IdCtrlschemeVO> schemvos = new ArrayList();
			ArrayList<String> pkList = new ArrayList();
			HashMap<String, ArrayList<IdCtrlschemeVO>> map_cell = new HashMap();
			ArrayList<Integer> error_list = new ArrayList();

			Map<Integer, IdCtrlschemeVO[]> schemeMap = new HashMap();
			for (int i = 0; i < formulaExpress.length; i++) {
				String express = formulaExpress[i];
				SingleSchema schema = new SingleSchema(express);

				IdCtrlschemeVO[] vos = convertIdCtrlscheme(schema);

				ufindGetData.addCost("Ufind get data 0.5", ufindGetData.getCost());

				for (IdCtrlschemeVO vo : vos) {
					schemvos.add(vo);
				}
				schemeMap.put(Integer.valueOf(i), vos);
			}

			IdCtrlschemeVO[] vos = (IdCtrlschemeVO[]) (IdCtrlschemeVO[]) schemvos.toArray(new IdCtrlschemeVO[0]);

			HashMap paraMap = sortVOsBySys(schemeMap);

			Iterator itor = paraMap.keySet().iterator();
			ufindGetData.addCost("Ufind get data 1", ufindGetData.getCost());
			while (itor.hasNext()) {
				String src = (String) itor.next();
				ArrayList ls = (ArrayList) paraMap.get(src);
				NtbParamVO[] params = (NtbParamVO[]) (NtbParamVO[]) ls.toArray(new NtbParamVO[0]);

				ufindGetData.addCost("Ufind get data 2", ufindGetData.getCost());
				CountTimeCost expressCost = new CountTimeCost();
				expressCost.beginCost();

				IBusiSysExecDataProvider exeprovider = getExcProvider(src);

				expressCost.addCost("Ufind get service", expressCost.getCost());

				ArrayList ufindVO = new ArrayList();
				ArrayList prefindVO = new ArrayList();
				for (int n = 0; n < params.length; n++) {
					if (params[n].getMethodCode().equals("UFIND"))
						ufindVO.add(params[n]);
					else if (params[n].getMethodCode().equals("PREFIND")) {
						prefindVO.add(params[n]);
					}
				}
				UFDouble[][] ufindretdatas = (UFDouble[][]) null;
				UFDouble[][] prefindretdatas = (UFDouble[][]) null;
				ufindGetData.addCost("Ufind get data 3", ufindGetData.getCost());
				try {
					long start = System.currentTimeMillis();
					if (ufindVO.size() > 0) {
						CountTimeCost getDataCost = new CountTimeCost();
						getDataCost.beginCost();

						ufindretdatas = exeprovider.getExecDataBatch((NtbParamVO[]) ufindVO.toArray(new NtbParamVO[0]));

						getDataCost.addCost("Ufind get Data", getDataCost.getCost());
					}

					if (prefindVO.size() > 0) {
						prefindretdatas = exeprovider.getReadyDataBatch((NtbParamVO[]) prefindVO.toArray(new NtbParamVO[0]));
					}

				} catch (BusinessException ex) {
					NtbLogger.error(ex);
					String sysid = null;
					String formulaName = null;
					if (ufindVO.size() > 0) {
						sysid = ((NtbParamVO) ufindVO.get(0)).getSys_id();
					} else if (prefindVO.size() > 0) {
						sysid = ((NtbParamVO) prefindVO.get(0)).getSys_id();
					}
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000214", null, new String[] { CtlBdinfoCTL.getSelectSystem(sysid).getSysname(), ex.getMessage() }));
				}

				for (int j = 0; j < ufindVO.size(); j++) {
					((NtbParamVO) ufindVO.get(j)).setRundata(ufindretdatas[j]);
				}

				for (int j = 0; j < prefindVO.size(); j++) {
					((NtbParamVO) prefindVO.get(j)).setReadydata(prefindretdatas[j]);
				}

				ArrayList allNtbParamVO = new ArrayList();
				allNtbParamVO.addAll(ufindVO);
				allNtbParamVO.addAll(prefindVO);

				params = (NtbParamVO[]) allNtbParamVO.toArray(new NtbParamVO[0]);

				for (int i = 0; i < params.length; i++) {
					paramvos.add(params[i]);
				}
			}

			NtbParamVO[] sma = (NtbParamVO[]) (NtbParamVO[]) paramvos.toArray(new NtbParamVO[0]);

			ArrayList sortsVO = sortNtbParamVO(sma);
			ufindGetData.addCost("Ufind get data 5", ufindGetData.getCost());
			AccountQryCache.getInstance().clearCache();
			DataGetterContext.getInstance().clearContext();
			return (NtbParamVO[]) (NtbParamVO[]) sortsVO.toArray(new NtbParamVO[0]);
		} catch (Exception ex) {
			NtbLogger.error(ex);
			if (ex.getMessage() == null)
				NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000215");
			throw new BusinessException(ex.getMessage());
		}
	}

	public ArrayList sortNtbParamVO(NtbParamVO[] vos) {
		Map<String, NtbParamVO> realParamMap = new HashMap();
		for (NtbParamVO vo : vos) {
			if (realParamMap.containsKey(vo.getNtbparamvoId())) {
				NtbParamVO vo0 = (NtbParamVO) realParamMap.get(vo.getNtbparamvoId());
				vo0.add(vo, vo0.getMethodCode());
			} else {
				realParamMap.put(vo.getNtbparamvoId(), vo);
			}
		}
		NtbParamVO[] ntbParamArr = new NtbParamVO[realParamMap.keySet().size()];
		ArrayList linkedParamList = new ArrayList();
		for (Map.Entry entry : realParamMap.entrySet()) {
			int index = Integer.parseInt((String) entry.getKey());
			ntbParamArr[index] = ((NtbParamVO) entry.getValue());
		}
		for (NtbParamVO vo : ntbParamArr)
			linkedParamList.add(vo);
		return linkedParamList;
	}

	public String getExeFormulaExpress(DataCell cell, String exeExpress, String funcName) throws BusinessException {
		String express = null;
		try {
			express = CtrlRuleCTL.getExeFormulaExpress(cell, exeExpress.toString(), funcName);
		} catch (Exception ex) {
			throw new BusinessException(ex);
		}
		return express;
	}

	public TreeMap<Integer, ArrayList<NtbParamVO>> getUfNtbParamVOs(String[] formulaExpress) throws BusinessException {
		try {
			TreeMap ufNtbVoMap = new TreeMap();

			for (int i = 0; i < formulaExpress.length; i++) {
				ArrayList schemvos = new ArrayList();
				String express = formulaExpress[i];
				SingleSchema schema = new SingleSchema(express);

				IdCtrlschemeVO[] ctrlVos = convertIdCtrlscheme(schema);
				for (IdCtrlschemeVO ctrlVo : ctrlVos)
					schemvos.add(ctrlVo);
				IdCtrlschemeVO[] vos = (IdCtrlschemeVO[]) (IdCtrlschemeVO[]) schemvos.toArray(new IdCtrlschemeVO[0]);

				ArrayList voList = sortVOsByUf(vos);
				ufNtbVoMap.put(Integer.valueOf(i), voList);
			}
			return ufNtbVoMap;
		} catch (BusinessException be) {
			throw be;
		} catch (Exception e) {
		}
		throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("tbb_rule", "01420rule-000183"));
	}

	private ArrayList<NtbParamVO> sortVOsByUf(IdCtrlschemeVO[] ctlvos) throws Exception {
		try {
			ArrayList voList = new ArrayList();
			NtbParamVO[] params = parseCtrls(ctlvos);
			for (int i = 0; i < params.length; i++) {
				voList.add(params[i]);
			}
			return voList;
		} catch (Exception ex) {
			NtbLogger.error(ex);
			throw ex;
		}
	}

	public NtbParamVO[] getExeDataByVO(String sys, NtbParamVO[] vos) throws BusinessException {
		UFDouble[][] ufData = (UFDouble[][]) null;
		UFDouble[][] ufReadyData = (UFDouble[][]) null;
		NtbParamVO[] parms = vos;
		if ((parms != null) && (parms.length != 0)) {
			ArrayList ufindVO = new ArrayList();
			ArrayList prefindVO = new ArrayList();
			IBusiSysExecDataProvider exeprovider = getExcProvider(sys);

			for (int n = 0; n < vos.length; n++) {
				if (vos[n].getMethodCode().equals("UFIND"))
					ufindVO.add(vos[n]);
				else if (vos[n].getMethodCode().equals("PREFIND")) {
					prefindVO.add(vos[n]);
				}
			}

			if (exeprovider != null) {
				if (ufindVO.size() > 0) {
					ufData = exeprovider.getExecDataBatch((NtbParamVO[]) ufindVO.toArray(new NtbParamVO[0]));
				}

				if (prefindVO.size() > 0) {
					ufReadyData = exeprovider.getReadyDataBatch((NtbParamVO[]) prefindVO.toArray(new NtbParamVO[0]));
				}
				for (int i = 0; i < ufindVO.size(); i++) {
					((NtbParamVO) ufindVO.get(i)).setRundata(ufData[i]);
				}
				for (int j = 0; j < prefindVO.size(); j++) {
					((NtbParamVO) prefindVO.get(j)).setReadydata(ufReadyData[j]);
				}
				ArrayList allNtbParamVO = new ArrayList();
				allNtbParamVO.addAll(ufindVO);
				allNtbParamVO.addAll(prefindVO);
				parms = (NtbParamVO[]) allNtbParamVO.toArray(new NtbParamVO[0]);
			}
		}
		return parms;
	}

	public ArrayList<IdCtrlformulaVO> getPlanStartAndStopCtrlformulaVO(String pk_cube) throws BusinessException {
		return CtlSchemeCTL.getPlanStartAndStopCtrlformulaVO(pk_cube);
	}

	public void deleteTempTable(String name) throws Exception, BusinessException, NamingException {
		NtbSuperDMO.executeUpdate(new StringBuilder().append("drop table ").append(name).toString());
	}

	public HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> queryCtrlScheme(String sWhere) throws BusinessException {
		try {
			HashMap returnvo = new HashMap();

			HashMap tempMap = new HashMap();
			NtbSuperDMO dmo = new NtbSuperDMO();

			SuperVO[] vos_parent = dmo.queryByWhereClause(IdCtrlformulaVO.class, sWhere);
			ArrayList pkList = new ArrayList();

			Map cubeMap = new HashMap();
			ArrayList cubeList = new ArrayList();

			if ((vos_parent != null) && (vos_parent.length > 0)) {
				for (int i = 0; i < vos_parent.length; i++) {
					IdCtrlformulaVO parentVO = (IdCtrlformulaVO) vos_parent[i];
					ArrayList tmpList = new ArrayList();

					tmpList.add(parentVO.getPrimaryKey());
					tmpList.add(parentVO.getPrimaryKey());
					cubeList.add(tmpList);

					pkList.add(parentVO.getPrimaryKey());
					tempMap.put(parentVO.getPrimaryKey(), parentVO);
					cubeMap.put(parentVO.getPk_cube(), null);
				}

				String tmpTableName = new StringBuilder().append("NTB_TMP_FORMUAL_").append(RandomStringUtils.randomNumeric(3)).toString();
				tmpTableName = CtlSchemeCTL.createNtbTempTable_new(null, tmpTableName, cubeList);

				StringBuffer sWhere_cube = new StringBuffer();
				sWhere_cube.append("pk_ctrlformula in (");
				sWhere_cube.append("select DATACELLCODE from ").append(tmpTableName);
				sWhere_cube.append(")");

				SuperVO[] vos_children = dmo.queryByWhereClause(IdCtrlschemeVO.class, sWhere_cube.toString());

				for (int i = 0; i < vos_children.length; i++) {
					IdCtrlschemeVO childrenVO = (IdCtrlschemeVO) vos_children[i];
					if (tempMap.containsKey(childrenVO.getPk_ctrlformula())) {
						IdCtrlformulaVO tempvo = (IdCtrlformulaVO) tempMap.get(childrenVO.getPk_ctrlformula());

						if (returnvo.containsKey(tempvo)) {
							ArrayList schmvols = (ArrayList) returnvo.get(tempvo);

							schmvols.add(childrenVO);
						} else {
							ArrayList schmvols = new ArrayList();
							schmvols.add(childrenVO);
							returnvo.put(tempvo, schmvols);
						}
					}
				}
			}

			return returnvo;
		} catch (Exception e) {
		}
		throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052"));
	}

	public void deleteCtrlScheme(Map<String, List<String>> map) throws BusinessException {
		try {
			NtbSuperDMO dmo = new NtbSuperDMO();

			ArrayList parentList = new ArrayList();
			ArrayList childrenList = new ArrayList();
			Iterator iteraKey = map.keySet().iterator();

			while (iteraKey.hasNext()) {
				String parentVOPk = (String) iteraKey.next();

				parentList.add(parentVOPk);
				List templist = (List) map.get(parentVOPk);
				childrenList.addAll(templist);
			}

			dmo.deleteArrayByPKs(IdCtrlformulaVO.class, (String[]) parentList.toArray(new String[parentList.size()]));
			dmo.deleteArrayByPKs(IdCtrlschemeVO.class, (String[]) childrenList.toArray(new String[childrenList.size()]));

			AlertPercentHandler.updAlertPercentWhenStop(parentList);
		} catch (Exception e) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052"));
		}
	}

	public String createTempTable(String tableName, List<String> insertData) throws DAOException {
		createTempTable(tableName);
		insertTemp(insertData, tableName);
		return tableName;
	}

	public String insertTemp(List<String> insertData, String tableName) throws DAOException {
		PersistenceManager manager = null;
		try {
			manager = PersistenceManager.getInstance();
			JdbcSession session = manager.getJdbcSession();
			session.setAddTimeStamp(false);
			String sql = new StringBuilder().append("insert into ").append(tableName).append("(pk) values(?)").toString();
			for (String uk : insertData) {
				SQLParameter sp = new SQLParameter();
				sp.addParam(uk);
				session.addBatch(sql, sp);
			}
			session.executeBatch();
		} catch (DbException dbe) {
			throw new DAOException(dbe);
		} finally {
			if (manager != null)
				manager.release();
		}
		return tableName;
	}

	public String createTempTable(String tableName) {
		String vtn = null;
		JdbcSession session = null;
		try {
			session = new JdbcSession();
			String para2 = tableName;
			String para3 = "pk varchar(1000) not null";
			String para4 = "pk";
			vtn = new TempTable().createTempTable(session.getConnection(), para2, para3, new String[] { para4 });
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
		} catch (SQLException e) {
			Logger.error(e.getMessage(), e);
		} finally {
			if (session != null) {
				session.closeAll();
			}
		}
		return vtn;
	}

	public HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> createNtbTempTable(CubeDef cube, String name, ArrayList<ArrayList> listTempTableValue) throws Exception, BusinessException, NamingException {
		NtbSuperDMO demo = new NtbSuperDMO();
		StringBuffer sWhere_plan = new StringBuffer();
		String sTempTableName = null;
		sTempTableName = demo.getTempStringTable(name, new String[] { "DATACELLID", "DATACELLCODE" }, new String[] { "char(2000) not null ", "varchar(2000) " }, null, listTempTableValue);

		StringBuffer sWhere_cube = new StringBuffer();
		sWhere_cube.append("pk_dimvector in (");
		sWhere_cube.append("select DATACELLCODE from ").append(sTempTableName);
		sWhere_cube.append(") and isstarted = 'Y' and pk_cube = '");
		sWhere_cube.append(cube.getPrimaryKey());
		sWhere_cube.append("' and pk_parent is not null");
		HashMap ctlmap_cube = queryCtrlScheme(sWhere_cube.toString());
		return ctlmap_cube;
	}

	public void createBillType(NtbParamVO[] paramvs, String syscode) throws BusinessException {
		IBusiSysExecDataProvider exeprovider = null;
		try {
			exeprovider = getExcProvider(syscode);
		} catch (Exception ex) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000217", null, new String[] { syscode }));
		}

		exeprovider.createBillType(paramvs);
	}

	public void updateCtrlSchemeVOs(HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> notStartCtrlscheme) throws BusinessException {
		try {
			NtbSuperDMO dmo = new NtbSuperDMO();

			ArrayList parentls = new ArrayList();
			ArrayList childrenls = new ArrayList();

			Iterator iteraKey = notStartCtrlscheme.keySet().iterator();

			Iterator iteraValue = notStartCtrlscheme.values().iterator();

			while (iteraKey.hasNext()) {
				IdCtrlformulaVO parentVO = (IdCtrlformulaVO) iteraKey.next();
				parentls.add(parentVO);
			}
			while (iteraValue.hasNext()) {
				ArrayList childrenVO = (ArrayList) iteraValue.next();

				childrenls.addAll(childrenVO);
			}
			dmo.updateArray((SuperVO[]) parentls.toArray(new IdCtrlformulaVO[0]));
			dmo.updateArray((SuperVO[]) childrenls.toArray(new IdCtrlschemeVO[0]));
		} catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052"));
		}
	}

	public void updateCtrl(IdCtrlformulaVO[] vos) throws BusinessException {
		try {
			NtbSuperDMO dmo = new NtbSuperDMO();
			ArrayList parentls = new ArrayList();
			dmo.updateArray(vos);
		} catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052"));
		}
	}

	public String createNtbTempTable_new(CubeDef cube, String name, ArrayList<ArrayList> listTempTableValue) throws BusinessException, NamingException, Exception {
		NtbSuperDMO demo = new NtbSuperDMO("TEMP");
		StringBuffer sWhere_plan = new StringBuffer();
		String sTempTableName = null;
		sTempTableName = demo.getTempStringTable_New(name, new String[] { "DATACELLID", "DATACELLCODE" }, new String[] { "varchar(2000) not null ", "varchar(4000) " }, null, listTempTableValue);

		return sTempTableName;
	}

	public ArrayList<IdCtrlformulaVO> getPlanStartCtrlformulaVO(String pk_cube, String pk_task) throws BusinessException {
		try {
			HashMap map = new HashMap();
			String sWhere = new StringBuilder().append("pk_cube = '").append(pk_cube).append("' and PK_PLAN ='").append(pk_task).append("'").toString();
			IdCtrlformulaVO[] vos = (IdCtrlformulaVO[]) (IdCtrlformulaVO[]) NtbSuperServiceGetter.getINtbSuper().queryByCondition(IdCtrlformulaVO.class, sWhere);
			ArrayList list = new ArrayList();
			if (vos != null) {
				list.addAll(Arrays.asList(vos));
			}
			return list;
		} catch (Exception e) {
			NtbLogger.error(e);
		}
		throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052"));
	}

	public void checkExistCtrlSchemeFindByDv(HashMap<String, HashMap<DimVector, DataCellValue>> cubeMap) throws Exception {
		HashMap ctrlscheme = new HashMap();
		Iterator iterator = cubeMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry obj = (Map.Entry) iterator.next();
			String pk_cube = (String) obj.getKey();
			HashMap valueMap = (HashMap) obj.getValue();
			ArrayList dvList = new ArrayList();
			Iterator iter = valueMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry _obj = (Map.Entry) iter.next();
				DimVector vec = (DimVector) _obj.getKey();
				dvList.add(vec);
			}

			String tmpTableName = new StringBuilder().append("NTB_TMP_CUBE_").append(RandomStringUtils.randomNumeric(3)).toString();
			ArrayList _list = new ArrayList();
			IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
			for (int m = 0; m < dvList.size(); m++) {
				ArrayList tmpList = new ArrayList();
				tmpList.add(cvt.convertToString(dvList.get(m)) == null ? " " : cvt.convertToString(dvList.get(m)));
				tmpList.add(cvt.convertToString(dvList.get(m)) == null ? " " : cvt.convertToString(dvList.get(m)));
				_list.add(tmpList);
			}
			tmpTableName = CtlSchemeCTL.createNtbTempTable_new(null, tmpTableName, _list);
			StringBuffer sWhere_cube = new StringBuffer();
			sWhere_cube.append("isstarted = 'Y' and pk_dimvector in (");
			sWhere_cube.append("select DATACELLCODE from ").append(tmpTableName);
			sWhere_cube.append(")").append(" and pk_cube = '").append(pk_cube).append("'");
			ctrlscheme.putAll(CtlSchemeCTL.queryCtrlScheme(sWhere_cube.toString()));
		}
		HashMap map = new HashMap();
		HashMap pkMap = new HashMap();
		Iterator _iter = ctrlscheme.entrySet().iterator();
		while (_iter.hasNext()) {
			Map.Entry entry = (Map.Entry) _iter.next();
			IdCtrlformulaVO vo = (IdCtrlformulaVO) entry.getKey();
			if (pkMap.get(vo.getPk_cube()) == null) {
				ArrayList list = new ArrayList();
				list.add(vo.getPk_dimvector());
				pkMap.put(vo.getPk_cube(), list);
			} else {
				((ArrayList) pkMap.get(vo.getPk_cube())).add(vo.getPk_dimvector());
			}
		}
		Iterator iterPkMap = pkMap.entrySet().iterator();
		while (iterPkMap.hasNext()) {
			Map.Entry entry = (Map.Entry) iterPkMap.next();
			String str = (String) entry.getKey();
			ArrayList sList = (ArrayList) entry.getValue();
			map.putAll(CtlSchemeCTL.getDataCellPkCubeByDimVector(str, (String[]) sList.toArray(new String[0])));
		}

		ArrayList updatevo = new ArrayList();
		HashMap formulaMap = new HashMap();
		Iterator iter = ctrlscheme.entrySet().iterator();
		CtlSchemeCTL.getParserMap().clear();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			IdCtrlformulaVO vo = (IdCtrlformulaVO) entry.getKey();
			if (formulaMap.get(vo.getPk_parent()) == null) {
				DimFormulaVO dimformulavo = FormulaCTL.getDimFormulaByPrimaryKey(vo.getPk_parent());
				formulaMap.put(vo.getPk_parent(), dimformulavo);
			}
			ArrayList vos = (ArrayList) entry.getValue();
			String express = CtlSchemeCTL.againCalculate((DataCell) map.get(new StringBuilder().append(vo.getPk_cube()).append(vo.getPk_dimvector()).toString()), (IdCtrlschemeVO[]) vos.toArray(new IdCtrlschemeVO[0]), (DimFormulaVO) formulaMap.get(vo.getPk_parent()), (HashMap) cubeMap.get(vo.getPk_cube()));

			express = express.replaceAll("%", "/100");
			Boolean[] needctl = CtlSchemeCTL.needCtl(express);
			if (!needctl[0].booleanValue())
				throw new CtrlBusinessException(new StringBuilder().append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0", "01050rule001-0046")).append(((DataCell) map.get(new StringBuilder().append(vo.getPk_cube()).append(vo.getPk_dimvector()).toString())).toString()).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule_0", "01050rule001-0047")).append(express).append("]")
						.toString());
		}
	}

	public String[] addAlarmScheme(IdAlarmschemeVO[] vos) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		String[] pks = dao.insertVOArray(vos);
		return pks;
	}

	public String[] addAlarmDimVector(IdAlarmDimVectorVO[] vos) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		String[] pks = dao.insertVOArray(vos);
		return pks;
	}

	public Collection<IdAlarmschemeVO> queryAlarmScheme(String sqlWhere) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		Collection vos = dao.retrieveByClause(IdAlarmschemeVO.class, sqlWhere);
		return vos;
	}

	public Collection<IdAlarmDimVectorVO> queryAlarmDimvector(String sqlWhere) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		Collection vos = dao.retrieveByClause(IdAlarmDimVectorVO.class, sqlWhere);
		return vos;
	}

	public void updateAlarmScheme(IdAlarmschemeVO[] vo) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		dao.updateVOArray(vo);
	}

	public void deleteAlarmScheme(ArrayList<IdAlarmschemeVO> list) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		dao.deleteVOArray((SuperVO[]) list.toArray(new IdAlarmschemeVO[list.size()]));
	}

	public void deleteAlarmDimVector(List<IdAlarmDimVectorVO> list) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		dao.deleteVOArray((SuperVO[]) list.toArray(new IdAlarmDimVectorVO[list.size()]));
	}

	public HashMap<String, HashMap<DimVector, Boolean>> querAlarmScheme(MdTask task) throws BusinessException {
		HashMap alarmmap = null;
		IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
		try {
			if (task == null) {
				return alarmmap;
			}
			if (CtlSchemeCTL.checkTaskStatus(task)) {
				alarmmap = new HashMap();
				StringBuilder sql0 = new StringBuilder();
				sql0.append(" pk_plan = '").append(task.getPrimaryKey()).append("'");
				Collection<IdAlarmschemeVO> collection0 = queryAlarmScheme(sql0.toString());
				if ((collection0 == null) || (collection0.size() == 0)) {
					return null;
				}
				StringBuilder sql1 = new StringBuilder();
				ICubeDefQueryService cubeQuery = CubeServiceGetter.getCubeDefQueryService();
				String cubecode = null;
				sql1.append(" hasscheme = 'Y' and pk_alarmscheme in ('");
				for (IdAlarmschemeVO vo : collection0) {
					if (cubecode == null) {
						cubecode = cubeQuery.queryCubeDefByPK(vo.getPk_cube()).getObjcode();
					}
					sql1.append(vo.getPrimaryKey()).append("','");
				}
				sql1.append("')");
				Collection<IdAlarmDimVectorVO> collection1 = queryAlarmDimvector(sql1.toString());
				HashMap map = new HashMap();
				for (IdAlarmDimVectorVO vo : collection1) {
					if (("".equals(vo.getPk_dimvector())) || (null == vo.getPk_dimvector())) {
						continue;
					}
					DimVector dv = (DimVector) cvt.fromString(vo.getPk_dimvector());
					if (map.get(dv) == null) {
						map.put(dv, Boolean.valueOf(true));
					}
				}
				alarmmap.put(cubecode, map);
			}
		} catch (BusinessException e) {
			NtbLogger.printException(e);
		}
		return alarmmap;
	}

	public Map<String, List<String>> queryCtrlSchemeSimply(String sWhere) throws BusinessException {
		try {
			CostTime time2 = new CostTime();
			Map returnvo = new HashMap();

			HashMap tempMap = new HashMap();
			NtbSuperDMO dmo = new NtbSuperDMO();

			SuperVO[] vos_parent = dmo.queryByWhereClause(IdCtrlformulaVO.class, sWhere, new String[] { new IdCtrlformulaVO().getPKFieldName() });
			ArrayList pkList = new ArrayList();

			ArrayList cubeList = new ArrayList();

			if ((vos_parent != null) && (vos_parent.length > 0)) {
				for (int i = 0; i < vos_parent.length; i++) {
					IdCtrlformulaVO parentVO = (IdCtrlformulaVO) vos_parent[i];
					ArrayList tmpList = new ArrayList();

					tmpList.add(parentVO.getPrimaryKey());
					tmpList.add(parentVO.getPrimaryKey());
					cubeList.add(tmpList);

					pkList.add(parentVO.getPrimaryKey());
					tempMap.put(parentVO.getPrimaryKey(), parentVO);
				}

				String tmpTableName = new StringBuilder().append("NTB_TMP_FORMUAL_").append(RandomStringUtils.randomNumeric(3)).toString();
				tmpTableName = CtlSchemeCTL.createNtbTempTable_new(null, tmpTableName, cubeList);

				StringBuffer sWhere_cube = new StringBuffer();
				sWhere_cube.append("pk_ctrlformula in (");
				sWhere_cube.append("select DATACELLCODE from ").append(tmpTableName);
				sWhere_cube.append(")");

				SuperVO[] vos_children = dmo.queryByWhereClause(IdCtrlschemeVO.class, sWhere_cube.toString(), new String[] { "pk_obj", "pk_ctrlformula" });

				for (int i = 0; i < vos_children.length; i++) {
					IdCtrlschemeVO childrenVO = (IdCtrlschemeVO) vos_children[i];
					if (tempMap.containsKey(childrenVO.getPk_ctrlformula())) {
						IdCtrlformulaVO tempvo = (IdCtrlformulaVO) tempMap.get(childrenVO.getPk_ctrlformula());

						if (returnvo.containsKey(tempvo.getPrimaryKey())) {
							List schmvols = (List) returnvo.get(tempvo.getPrimaryKey());

							schmvols.add(childrenVO.getPrimaryKey());
						} else {
							List schmvols = new ArrayList();
							schmvols.add(childrenVO.getPrimaryKey());
							returnvo.put(tempvo.getPrimaryKey(), schmvols);
						}
					}
				}
			}

			return returnvo;
		} catch (Exception e) {
		}
		throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052"));
	}
}

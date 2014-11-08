
package edu.wustl.catissuecore.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.wustl.catissuecore.gridImpl.AbstractGridImpl;
import edu.wustl.catissuecore.gridImpl.GridSpecimenImpl;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.util.global.CommonServiceLocator;
import edu.wustl.common.util.logger.Logger;
import edu.wustl.common.velocity.VelocityManager;
import edu.wustl.dao.JDBCDAO;
import edu.wustl.dao.daofactory.DAOConfigFactory;
import edu.wustl.dao.daofactory.IDAOFactory;
import edu.wustl.dao.exception.DAOException;

public class SpecimenListGridAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String reqParam = (String) request.getParameter("reqParam");
		SessionDataBean sessionBean = (SessionDataBean) request.getSession().getAttribute(Constants.SESSION_DATA);
		JDBCDAO jdbcDAO = null;
		List list = null;
		try {
			String appName = CommonServiceLocator.getInstance().getAppName();
			IDAOFactory daofactory = DAOConfigFactory.getInstance().getDAOFactory(appName);
			jdbcDAO = daofactory.getJDBCDAO();
			AbstractGridImpl specGridImpl = new GridSpecimenImpl();
			String sql = specGridImpl.getGridQuery(reqParam, sessionBean);
			jdbcDAO.openSession(null);
			list = jdbcDAO.executeQuery(sql);
		}
		catch (DAOException e) {
			Logger.out.debug(e.getCause(), e);
		}
		finally {
			try {
				jdbcDAO.closeSession();
			}
			catch (DAOException e) {
				Logger.out.debug(e.getCause(), e);
			}
		}

		Map<String, Object> gridData = new HashMap<String, Object>();
		gridData.put("list", list);
		String responseString = VelocityManager.getInstance().evaluate(gridData, "specimenListGridTemplate.vm");
		response.setBufferSize(responseString.length());
		response.getWriter().write(responseString);
		response.setContentType("text/xml");
		return null;
	}

}

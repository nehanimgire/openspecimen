/*
 * $Name: 1.41.2.41.2.3 $
 *
 * */

package edu.wustl.catissuecore.util.listener;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.wustl.bulkoperator.util.BulkOperationUtility;
import edu.wustl.catissuecore.cpSync.SyncCPThreadExecuterImpl;
import edu.wustl.catissuecore.util.HelpXMLPropertyHandler;
import edu.wustl.catissuecore.util.global.Variables;
import edu.wustl.common.util.XMLPropertyHandler;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.util.global.ApplicationProperties;
import edu.wustl.common.util.global.CommonServiceLocator;
import edu.wustl.common.util.logger.Logger;
import edu.wustl.common.util.logger.LoggerConfig;
import edu.wustl.dao.exception.DAOException;

/**
 *
 * @author aarti_sharma 
 *
 * */
public class CatissueCoreServletContextListener implements ServletContextListener {

	
	private static final Logger logger = Logger.getCommonLogger(CatissueCoreServletContextListener.class);

	/**
	 * This method is called during server startup, It is used when want some initliazation before
	 * server start.
	 * @param sce ServletContextEvent
	 */
	public void contextInitialized(final ServletContextEvent sce) {
		try {
			logger.info("Initializing catissue application");
			final ServletContext servletContext = sce.getServletContext();
			ApplicationProperties.initBundle(servletContext.getInitParameter("resourcebundleclass"));
			this.setGlobalVariable();
			CommonServiceLocator.getInstance().setAppHome(sce.getServletContext().getRealPath(""));
			logger.info(":::::::::::::Application home ::::::::::::" + CommonServiceLocator.getInstance().getAppHome());
			
			LoggerConfig.configureLogger(CommonServiceLocator.getInstance().getPropDirPath());
		}
		catch (final Exception e) {
			CatissueCoreServletContextListener.logger.error("Application failed to initialize" + e.getMessage(), e);
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		try {
			BulkOperationUtility.changeBulkOperationStatusToFailed();
			SyncCPThreadExecuterImpl executerImpl = SyncCPThreadExecuterImpl.getInstance();
			executerImpl.shutdown();

		}
		catch (final DAOException e) {
			logger.error("Exception occured while updating "
					+ "the Bulk Operation job status." + e.getMessage(), e);
		}
	}
	/**
	 * Set Global variable.
	 * @throws Exception Exception
	 */
	private void setGlobalVariable() throws Exception {
		final String path = System.getProperty("app.propertiesFile");
		XMLPropertyHandler.init(path);
		new File(path);
		final int maximumTreeNodeLimit = Integer.parseInt(XMLPropertyHandler.getValue(Constants.MAXIMUM_TREE_NODE_LIMIT));
		Variables.maximumTreeNodeLimit = maximumTreeNodeLimit;
		Variables.isToDisplayAdminEmail = Boolean.parseBoolean(XMLPropertyHandler
				.getValue("display.admin.emails.onSummaryPage"));

		HelpXMLPropertyHandler
				.init(CommonServiceLocator.getInstance().getPropDirPath() + File.separator + "help_links.xml");
	}
	
}

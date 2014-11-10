/*
 * $Name: 1.41.2.41.2.3 $
 *
 * */

package edu.wustl.catissuecore.util.listener;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

<<<<<<< HEAD
import krishagni.catissueplus.csd.CatissueUserContextProviderImpl;
import krishagni.catissueplus.util.FormProcessor;
import krishagni.catissueplus.util.QuartzSchedulerJobUtil;

import org.apache.commons.io.FilenameUtils;

import titli.model.util.TitliResultGroup;
import au.com.bytecode.opencsv.CSVReader;

import com.krishagni.catissueplus.core.de.ui.StorageContainerControlFactory;
import com.krishagni.catissueplus.core.de.ui.StorageContainerMapper;
import com.krishagni.catissueplus.core.de.ui.UserControlFactory;
import com.krishagni.catissueplus.core.de.ui.UserFieldMapper;
import com.krishagni.catissueplus.core.notification.schedular.ExternalAppFailNotificationSchedular;
import com.krishagni.catissueplus.core.notification.schedular.ExternalAppNotificationSchedular;

import edu.common.dynamicextensions.domain.nui.factory.ControlManager;
import edu.common.dynamicextensions.nutility.BOUtil;
import edu.common.dynamicextensions.nutility.DEApp;
import edu.common.dynamicextensions.nutility.FormProperties;
import edu.common.dynamicextensions.query.PathConfig;
import edu.wustl.bulkoperator.util.BulkEMPIOperationsUtility;
=======
>>>>>>> origin/os_hsqldb
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
<<<<<<< HEAD
			this.initCatissueParams();
			logApplnInfo();
			DefaultValueManager.validateAndInitDefaultValueMap();
			BulkOperationUtility.changeBulkOperationStatusToFailed();
			//QueryCoreServletContextListenerUtil.contextInitialized(sce, "java:/query");
			if (XMLPropertyHandler.getValue(Constants.EMPI_ENABLED).equalsIgnoreCase("true"))
			{
				BulkEMPIOperationsUtility.changeBulkOperationStatusToFailed();
				// eMPI integration initialization
				initeMPI();
			}
			if (Constants.TRUE.equals(XMLPropertyHandler.getValue("Imaging.enabled")))
			{
				Variables.isImagingConfigurred = true;
			}
			SyncCPThreadExecuterImpl executerImpl = SyncCPThreadExecuterImpl.getInstance();
			executerImpl.init();
			initializeParticipantConfig();
            /** 
             * 	Details: Quartz Scheduler for executing nightly cron jobs
             *  Added By: Ashraf
             */
			
            QuartzSchedulerJobUtil.scheduleQuartzSchedulerJob();
            //QueryDataExportService.initialize();
            
//            ExternalAppNotificationSchedular.scheduleExtAppNotifSchedulerJob();
//            ExternalAppFailNotificationSchedular.scheduleExtAppFailNotifSchedulerJob();

			CSDProperties.getInstance().setUserContextProvider(new CatissueUserContextProviderImpl());
			
			FormProperties.getInstance().setPostProcessor(new FormProcessor());

            BOUtil.getInstance().setGenerator(new BOTemplateUpdater());

            InitialContext ic = new InitialContext();
			DataSource ds = (DataSource)ic.lookup(JNDI_NAME);
			String dateFomat = CommonServiceLocator.getInstance().getDatePattern();
			String timeFormat = CommonServiceLocator.getInstance().getTimePattern(); 
			
			String dir = new StringBuilder(XMLPropertyHandler.getValue("appserver.home.dir")).append(File.separator)
					.append("os-data").append(File.separator)
					.append("de-file-data").append(File.separator)
					.toString();
			File dirFile = new File(dir);
			if (!dirFile.exists()) {
				if (!dirFile.mkdirs()) {
					throw new RuntimeException("Error couldn't create directory for storing de file data");
				}
			}
						
			DEApp.init(ds, dir, dateFomat,timeFormat);
			initQueryPathsConfig();
			initFancyControls();
=======
			CommonServiceLocator.getInstance().setAppHome(sce.getServletContext().getRealPath(""));
			logger.info(":::::::::::::Application home ::::::::::::" + CommonServiceLocator.getInstance().getAppHome());
>>>>>>> origin/os_hsqldb
			
			LoggerConfig.configureLogger(CommonServiceLocator.getInstance().getPropDirPath());
		}
		catch (final Exception e) {
			CatissueCoreServletContextListener.logger.error("Application failed to initialize" + e.getMessage(), e);
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

<<<<<<< HEAD
 	private void initQueryPathsConfig() {
 		String path = System.getProperty("app.propertiesDir") + File.separatorChar + "paths.xml";
 		PathConfig.intialize(path);
	}
 	
 	private void initFancyControls() {
 		ControlManager ctrlMgr = ControlManager.getInstance();
		ctrlMgr.registerFactory(UserControlFactory.getInstance());
		ctrlMgr.registerFactory(StorageContainerControlFactory.getInstance());
		
		ControlMapper ctrlMapper = ControlMapper.getInstance();
		ctrlMapper.registerControlMapper("userField", new UserFieldMapper());
		ctrlMapper.registerControlMapper("storageContainer", new StorageContainerMapper());
 	}

	/**
	 * Inite mpi.
	 */
	private void initeMPI()
	{
		try
		{
			checkEMPIAdminUser();
			RaceGenderCodesProperyHandler.init("HL7MesRaceGenderCodes.xml");
			participantManagerUtility.registerWMQListener();
			try
			{
				ParticipantManagerUtility.initialiseParticiapntMatchScheduler();
			}
			catch (Exception excep)
			{
				logger.error(" ####### ERROR WHILE INITIALISING THE SHECUDER FOR PROCESSING THE PARTICIPANTS ######### ");
				logger.error(excep.getMessage(), excep);
			}

		}
		catch (Exception excep)
		{
			logger.error("Could not initialized application, Error in loading the HL7 race gender code property handler.");
			logger.error(excep.getMessage(), excep);
		}
		catch (Error excep)
		{
			logger.error("EMPI : ERROR WHILE REGISTERING WMQ LISTENER ");
			logger.error(excep.getMessage(), excep);
		}

	}

	private void checkEMPIAdminUser() throws ApplicationException, MessagingException
	{
		String eMPIAdminUName = XMLPropertyHandler.getValue(Constants.HL7_LISTENER_ADMIN_USER);
		User validUser = AppUtility.getUser(eMPIAdminUName);
		EmailHandler emailHandlrObj = new EmailHandler();
		if (validUser == null)
		{
			emailHandlrObj.sendEMPIAdminUserNotExitsEmail();
		}
		if (validUser != null
				&& Constants.ACTIVITY_STATUS_CLOSED.equals(validUser.getActivityStatus()))
		{
			emailHandlrObj.sendEMPIAdminUserClosedEmail(validUser);
		}
	}

	/**
	 * Initialize caTissue default properties.
	 * @throws ClassNotFoundException ClassNotFoundException
	 * @throws DAOException DAOException
	 * @throws ParseException ParseException
	 * @throws IOException 
	 */
	public void initCatissueParams() throws ClassNotFoundException, DAOException, ParseException,
			IOException
	{
		//edu.wustl.query.util.global.Utility.setReadDeniedAndEntitySqlMap();
		this.addDefaultProtectionGroupsToMap();
		final QueryBizLogic bLogic = new QueryBizLogic();
		bLogic.initializeQueryData();
		this.createAliasAndPageOfMap();
		LabelAndBarcodeGeneratorInitializer.init();
		this.initialiseVariablesForEdinburgh();
		this.initialiseVariablesForDFCI();
		this.initialiseVariableForAppInfo();
		Utility.initializePrivilegesMap();
		this.initTitliIndex();
		this.initCDEManager();
		this.initDashboardCache();
		this.initReportScheduler();
		initThrottlingModule();
		final String absolutePath = CommonServiceLocator.getInstance().getPropDirPath()
				+ File.separator + "PrintServiceImplementor.properties";
		Variables.setPrinterInfo(absolutePath);
		System.setProperty("app.propertiesDir", CommonServiceLocator.getInstance().getPropDirPath());
	}
	

	private void initThrottlingModule() 
	{
		String timeIntervalInMinutes = XMLPropertyHandler.getValue(Constants.MAXIMUM_TREE_NODE_LIMIT);
		String maxLimits = XMLPropertyHandler.getValue(Constants.MAXIMUM_TREE_NODE_LIMIT);
		final int maximumTreeNodeLimit = Integer.parseInt(maxLimits);
		Variables.throttlingMaxLimit = maximumTreeNodeLimit;
		final long timeInterval = Long.parseLong(timeIntervalInMinutes);
		Variables.throttlingTimeInterval = timeInterval*60*1000;
		
	}
=======
	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		try {
			BulkOperationUtility.changeBulkOperationStatusToFailed();
			SyncCPThreadExecuterImpl executerImpl = SyncCPThreadExecuterImpl.getInstance();
			executerImpl.shutdown();
>>>>>>> origin/os_hsqldb

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

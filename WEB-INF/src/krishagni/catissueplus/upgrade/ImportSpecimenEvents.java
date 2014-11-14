package krishagni.catissueplus.upgrade;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.common.dynamicextensions.domain.nui.Container;
import edu.common.dynamicextensions.domain.nui.UserContext;
import edu.common.dynamicextensions.ndao.DbSettingsFactory;
import edu.common.dynamicextensions.ndao.JdbcDao;
import edu.common.dynamicextensions.ndao.JdbcDaoFactory;
import edu.common.dynamicextensions.ndao.ResultExtractor;
import edu.common.dynamicextensions.ndao.TransactionManager;
import edu.common.dynamicextensions.ndao.TransactionManager.Transaction;

public class ImportSpecimenEvents {
	private static final Logger logger = Logger.getLogger(ImportSpecimenEvents.class);

	private String eventName;
	
	private String eventFormDef;
	
	private String eventTable;
	
	private boolean systemEvent;
	
	@SuppressWarnings("unchecked")
	public static void importForms(String username, String configFile) 
	throws Exception {
		logger.setLevel(Level.INFO);
		logger.info("Importing Specimen Events ...");

		UserContext ctx = getUserCtxt(username);
		if (ctx == null) {
			return;
		}
		
		List<Map<String, String>> eventsInfo = 
				new ObjectMapper().readValue(new File(configFile), List.class);		
		String pathDir = new File(configFile).getParentFile().getParent();
		
		for (Map<String, String> eventInfo : eventsInfo) {
			try {
				eventInfo.put("form", pathDir + File.separator + eventInfo.get("form"));
				
				ImportSpecimenEvents importer = new ImportSpecimenEvents(eventInfo);
				importer.importEventForm(ctx);				
			} catch (Exception e) {
			}
		}
	}

	private static UserContext getUserCtxt(final String username)
	throws Exception {
		final Long userId = UpgradeUtil.getUserId(username);
		if (userId == null) {
			logger.error("Invalid username: " + username);
			return null;
		}

		return new UserContext() {
			@Override
			public String getUserName() {
				return username;
			}

			@Override
			public Long getUserId() {
				return userId;
			}

			@Override
			public String getIpAddress() {
				return null;
			}
		};
	}

	public ImportSpecimenEvents(Map<String, String> eventInfo) {
		eventName = eventInfo.get("name");
		eventFormDef = eventInfo.get("form");
		eventTable = eventInfo.get("dbTable");
		
		String systemEventStr = eventInfo.get("systemEvent");
		if (systemEventStr != null && systemEventStr.trim().equalsIgnoreCase("true")) {
			systemEvent = true;
		}
	}
	
	public void importEventForm(UserContext ctx) 
	throws Exception {
		try {
			long t1 = System.currentTimeMillis();
			logger.info("Importing : " + eventName);

			if (doesFormExists(eventName)) {
				logger.info("Specimen event " + eventName + " already exists. "
						+ "Stopping import for this event");
				return;
			}

			logger.info("Creating form for event: " + eventName);
			Long formId = createForm(ctx, eventFormDef);
			if (formId == null) {
				logger.error("Error creating form for event: " + eventName);
				throw new RuntimeException("Error creating form for event: " + eventName);
			}

			logger.info("Imported : " + eventName + " in " + timeDiff(t1) + " ms");
		} catch (Exception e) {
			logger.error("Error importing data for event: " + eventName, e);
			throw e;
		}
	}
	
	private long timeDiff(long time) {
		return System.currentTimeMillis() - time;
	}

	private boolean doesFormExists(String formName) {
		return JdbcDaoFactory.getJdbcDao().getResultSet(
				GET_FORM_ID_BY_NAME_SQL, Collections.singletonList(formName),
				new ResultExtractor<Boolean>() {
					@Override
					public Boolean extract(ResultSet rs) throws SQLException {
						return rs.next() && rs.getObject(1) != null;
					}
				});
	}

	private Long createForm(UserContext ctx, String formFile) throws Exception {
		Transaction txn = TransactionManager.getInstance().newTxn();
		try {
			Long formId = Container.createContainer(ctx, formFile, ".", false);
			Long formCtxId = insertFormCtx(formId);
			TransactionManager.getInstance().commit(txn);
			return formCtxId;
		} catch (Exception e) {
			TransactionManager.getInstance().rollback(txn);
			logger.error("Error creating form", e);
		}

		return null;
	}

	private Long insertFormCtx(Long formId) throws Exception {
		JdbcDao jdbcDao = JdbcDaoFactory.getJdbcDao();
		String sql = DbSettingsFactory.isOracle() ? INSERT_FORM_CTX_ORA_SQL : INSERT_FORM_CTX_MY_SQL;

		List<? extends Object> params = Arrays.asList(formId, "SpecimenEvent", -1, null, 1, systemEvent ? 1 : 0);
		Number id = jdbcDao.executeUpdateAndGetKey(sql, params, "IDENTIFIER");
		if (id == null) {
			logger.error("Error creating form context for form: " + formId);
			return null;
		}

		return id.longValue();
	}
	

	private static final String INSERT_FORM_CTX_MY_SQL = "insert into catissue_form_context( "
			+ "  identifier, container_id, entity_type, cp_id, sort_order, is_multirecord, is_sys_form) "
			+ "values (" + "  default, ?, ?, ?, ?, ?, ?)";

	private static final String INSERT_FORM_CTX_ORA_SQL = "insert into catissue_form_context( "
			+ "  identifier, container_id, entity_type, cp_id, sort_order, is_multirecord, is_sys_form) "
			+ "values ("
			+ "  catissue_form_context_seq.nextval, ?, ?, ?, ?, ?, ?)";

	private static final String GET_FORM_ID_BY_NAME_SQL = "select identifier from dyextn_containers where name = ?";
}

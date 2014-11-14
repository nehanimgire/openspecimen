package krishagni.catissueplus.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.common.dynamicextensions.domain.nui.Container;
import edu.common.dynamicextensions.domain.nui.UserContext;
import edu.common.dynamicextensions.ndao.DbSettingsFactory;
import edu.common.dynamicextensions.ndao.JdbcDao;
import edu.common.dynamicextensions.ndao.JdbcDaoFactory;
import edu.common.dynamicextensions.ndao.ResultExtractor;
import edu.common.dynamicextensions.ndao.TransactionManager;
import edu.common.dynamicextensions.ndao.TransactionManager.Transaction;

public class ImportQueryForms {
	private static final Logger logger = Logger.getLogger(ImportQueryForms.class);

	public static void importForms(String username, String dir) 
	throws Exception {
		UserContext ctx = getUserCtxt(username);
		if (ctx == null) {
			return;
		}
		
		if (doQueryFormsExist()) {
			System.err.println(">>>>>> Quiting");
			logger.info("Query forms already imported. Quiting");
			return;
		}
		
		File dirFile = new File(dir);
		Properties sortProps = new Properties();
		sortProps.load(new FileInputStream(dir + File.separator + "sortOrder.properties"));
		
		
		Transaction txn = null;
		for (File file : dirFile.listFiles()) {
			try {
				txn = TransactionManager.getInstance().newTxn();
				System.err.println(">>>>>>Reading file: " + file.getAbsolutePath());
				if (!file.getAbsolutePath().endsWith(".xml")) {
					continue;
				}

				System.err.println("Creating form");
				Long formId = Container.createContainer(ctx, file.getAbsolutePath(), file.getParent(), false);
				Container form = Container.getContainer(formId);
				insertFormCtx(formId, Integer.parseInt(sortProps.getProperty(form.getName())));
				System.err.println("Created form context");
				TransactionManager.getInstance().commit(txn);
			} catch (Exception e) {
				e.printStackTrace();
				TransactionManager.getInstance().rollback(txn);
				System.err.println("Error inserting form " + file.getAbsolutePath());
			}
		}
	}
	
	private static boolean doQueryFormsExist() {
		return JdbcDaoFactory.getJdbcDao().getResultSet(
				QUERY_FORM_CNT_SQL, null,
				new ResultExtractor<Boolean>() {
					@Override
					public Boolean extract(ResultSet rs) throws SQLException {
						if (!rs.next()) {
							return false;
						}
						
						Long cnt = rs.getLong(1);
						return cnt != null && cnt > 0L ? true : false;
					}
				});
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
	
	private static Long insertFormCtx(Long formId, Integer sortOrder) throws Exception {
		JdbcDao jdbcDao = JdbcDaoFactory.getJdbcDao();
		String sql = DbSettingsFactory.isOracle() ? INSERT_FORM_CTX_ORA_SQL : INSERT_FORM_CTX_MY_SQL;

		List<? extends Object> params = Arrays.asList(formId, "Query", -1, sortOrder, 0, 0);
		Number id = jdbcDao.executeUpdateAndGetKey(sql, params, "IDENTIFIER");
		if (id == null) {
			logger.error("Error creating form context for form: " + formId);
			return null;
		}

		return id.longValue();
	}
	
	
	private static final String QUERY_FORM_CNT_SQL = "select count(*) from catissue_form_context where entity_type = 'Query'";
	
	private static final String INSERT_FORM_CTX_MY_SQL = "insert into catissue_form_context( "
			+ "  identifier, container_id, entity_type, cp_id, sort_order, is_multirecord, is_sys_form) "
			+ "values (" + "  default, ?, ?, ?, ?, ?, ?)";

	private static final String INSERT_FORM_CTX_ORA_SQL = "insert into catissue_form_context( "
			+ "  identifier, container_id, entity_type, cp_id, sort_order, is_multirecord, is_sys_form) "
			+ "values ("
			+ "  catissue_form_context_seq.nextval, ?, ?, ?, ?, ?, ?)";	
}
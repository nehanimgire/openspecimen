package krishagni.catissueplus.csd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.common.dynamicextensions.domain.nui.UserContext;
import edu.common.dynamicextensions.ndao.JdbcDaoFactory;
import edu.common.dynamicextensions.ndao.ResultExtractor;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.dynamicextensions.formdesigner.usercontext.AppUserContextProvider;

public class CatissueUserContextProviderImpl implements AppUserContextProvider {

	@Override
	public UserContext getUserContext(HttpServletRequest request) {
		final SessionDataBean sessionDataBean = (SessionDataBean) request.getSession().getAttribute("sessionData");
		
		return new UserContext() {	
			@Override
			public String getUserName() {
				return sessionDataBean.getUserName();
			}

			@Override
			public Long getUserId() {
				return sessionDataBean.getUserId();
			}

			@Override
			public String getIpAddress() {
				return sessionDataBean.getIpAddress();
			}
		};
	}
	
	@Override
	public UserContext getUserContext(final String userName) {
		
		return new UserContext() {	
			@Override
			public String getUserName() {
				return userName;
			}

			@Override
			public Long getUserId() {
				return getUserIdByName(userName);
			}

			@Override
			public String getIpAddress() {
				return null;
			}
		};
	}

	@Override
	public String getUserNameById(Long id) {
		List<Object> params = new ArrayList<Object>();
		params.add(id);
		
		return JdbcDaoFactory.getJdbcDao().getResultSet(GET_USER_NAME_SQL, params, new ResultExtractor<String>() {
			@Override
			public String extract(ResultSet rs) throws SQLException {
				if(rs.next()) {
					return new StringBuilder().append(rs.getString(1))
							.append(" ").append(rs.getString(2)).toString();
				}
				return null;
			}
		});
	}
	
	private Long getUserIdByName(String loginName) {
		List<Object> params = new ArrayList<Object>();
		params.add(loginName);
		
		return JdbcDaoFactory.getJdbcDao().getResultSet(GET_USER_ID_SQL, params, new ResultExtractor<Long>() {
			@Override
			public Long extract(ResultSet rs) throws SQLException {
				if(rs.next()) {
					return rs.getLong(1);
				}
				return null;
			}
		});
	}
	
	
	private static final String GET_USER_ID_SQL = 
			"select	identifier from catissue_user " +
			"where login_name = ? AND activity_status = 'Active'";
	
	private static String GET_USER_NAME_SQL = "select last_name, first_name from catissue_user where identifier = ?";
}

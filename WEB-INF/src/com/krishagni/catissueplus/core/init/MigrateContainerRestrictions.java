package com.krishagni.catissueplus.core.init;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.krishagni.catissueplus.core.administrative.domain.StorageContainer;
import com.krishagni.catissueplus.core.administrative.domain.factory.StorageContainerErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.errors.ParameterizedError;


public class MigrateContainerRestrictions implements InitializingBean {

	private PlatformTransactionManager txnMgr;

	private DaoFactory daoFactory;

	private JdbcTemplate jdbcTemplate;

	private Map<String, Set<String>> specimenTypes = new HashMap<String, Set<String>>();

	public void setTxnMgr(PlatformTransactionManager txnMgr) {
		this.txnMgr = txnMgr;
	}

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		migrate();
	}

	public void migrate() throws Exception {
		TransactionTemplate txnTmpl = new TransactionTemplate(txnMgr);
		txnTmpl.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);
		txnTmpl.execute(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				try {
					if (isMigrated()) {
						return null;
					}

					for (String type : specimenTypeNames) {
						specimenTypes.put(type, getTypes(type));
					}

					List<Long> parentCntIds = getParentCntrIds();
					for (Long contId : parentCntIds) {
						System.out.println("Processing container with  ID: " + contId);
						StorageContainer existingContainer = daoFactory.getStorageContainerDao().getById(contId);
						migrateRestrictions(existingContainer);
					}
					
					return null;
				}
				catch (Exception e) {
					status.setRollbackOnly();
					throw new RuntimeException(e);
				}
			}
		});
	}

	private void migrateRestrictions(StorageContainer container) throws IllegalAccessException,
			InvocationTargetException {
		System.out.println("Processing container with  ID: " + container.getId());

		Set<String> specimenTypeRestrictions = getTypeRestrictions(container.getId());
		Set<String> specimenClassRestrictions = getClassRestrictions(specimenTypeRestrictions);
		Set<String> allowedCps = getCpRestrictions(container.getId());

		List<CollectionProtocol> cps = new ArrayList<CollectionProtocol>();
		if (!CollectionUtils.isEmpty(allowedCps)) {
			cps = daoFactory.getCollectionProtocolDao().getCpsByShortTitle(allowedCps);
		}

		container.getAllowedCps().addAll(cps);
		container.getAllowedSpecimenClasses().addAll(specimenClassRestrictions);
		container.getAllowedSpecimenTypes().addAll(specimenTypeRestrictions);

		updateParentRestrictions(container.getParentContainer(), container);
		setComputedRestrictions(container);

		for (StorageContainer childContainer : container.getChildContainers()) {
			migrateRestrictions(childContainer);
		}
		
		try {
			container.validateRestrictions();
		} catch (OpenSpecimenException e) {
			System.out.println("Error for container with ID: " + container.getId());
			System.out.println("Error for container with Name: " + container.getName());
			
			for (ParameterizedError error : e.getErrors()) {
				System.out.println("Error: " + error.error());
				System.out.println("params: " + Arrays.asList(error.params()).toString());
				
				if (!StorageContainerErrorCode.CANNOT_HOLD_SPECIMEN.equals(error.error())) {
					continue;
				}
				
				Specimen specimen = daoFactory.getSpecimenDao().getByLabel(error.params()[1].toString());
				if (specimen != null) {
					container.getAllowedSpecimenTypes().add(specimen.getSpecimenType());
					container.getAllowedCps().add(specimen.getCollectionProtocol());
					
					migrateRestrictions(container);
				}
			}
		}
		System.out.println("Container saved with Id: "+ container.getId());
		daoFactory.getStorageContainerDao().saveOrUpdate(container, true);
	}

	private void updateParentRestrictions(StorageContainer parentContainer, StorageContainer childContainer) {
		if (parentContainer == null) {
			return;
		}
		
		parentContainer.getAllowedCps().addAll(childContainer.getAllowedCps());
		parentContainer.getAllowedSpecimenClasses().addAll(childContainer.getAllowedSpecimenClasses());
		parentContainer.getAllowedSpecimenTypes().addAll(childContainer.getAllowedSpecimenTypes());
		
		setComputedRestrictions(parentContainer);

		List<String> allAllowedTypes = daoFactory.getPermissibleValueDao().getSpecimenTypes(
				parentContainer.getAllowedSpecimenClasses());
		parentContainer.getAllowedSpecimenTypes().removeAll(allAllowedTypes);

		if (CollectionUtils.isEqualCollection(parentContainer.getAllowedCps(), childContainer.getAllowedCps())) {
			childContainer.getAllowedCps().clear();
		}
		
		if (CollectionUtils.isEqualCollection(parentContainer.getAllowedSpecimenClasses(), 
				childContainer.getAllowedSpecimenClasses())) {
			childContainer.getAllowedSpecimenClasses().clear();
		}
		
		if (CollectionUtils.isEqualCollection(parentContainer.getAllowedSpecimenTypes(),
				childContainer.getAllowedSpecimenTypes())) {
			childContainer.getAllowedSpecimenTypes().clear();
		}
		
		updateParentRestrictions(parentContainer.getParentContainer(), parentContainer);
	}
	
	private void setComputedRestrictions(StorageContainer container) {
		container.getCompAllowedSpecimenClasses().addAll(container.computeAllowedSpecimenClasses());
		container.getCompAllowedSpecimenTypes().addAll(container.computeAllowedSpecimenTypes());
		container.getCompAllowedCps().addAll(container.computeAllowedCps());
	}

	private Set<String> getClassRestrictions(Set<String> specimenTypeRestrictions) {
		Set<String> classRestrictions = new HashSet<String>();

		for (String type : specimenTypeNames) {
			if (specimenTypeRestrictions.containsAll(specimenTypes.get(type))) {
				classRestrictions.add(type);
				specimenTypeRestrictions.removeAll(specimenTypes.get(type));
			}
		}
		return classRestrictions;
	}
	
	//Currently I have checked for the existing types. OR 
	//We can add one flag in config, and after migration that flag will be deleted, so that it won't run again.
	//Please let me know your views too
	private boolean isMigrated() {
		List<Integer> result = jdbcTemplate.queryForList(GET_RESTRICTION_COUNT, Integer.class);
		return result.get(0) > 0;
	}

	private List<Long> getParentCntrIds() {
		return jdbcTemplate.queryForList(GET_PARENT_CONT_IDS_SQL, Long.class);
	}
	
	private Set<String> getTypes(String specimenClass) {
		List<String> types = daoFactory.getPermissibleValueDao().getSpecimenTypes(Arrays.asList(specimenClass));
		return new HashSet<String>(types);
	}
	
	private Set<String> getTypeRestrictions(Long contId) {
		List<String> result = jdbcTemplate.queryForList(GET_TYPE_RESTRICTION_SQL, String.class, contId);
		return new HashSet<String>(result);
	}

	private Set<String> getCpRestrictions(Long contId) {
		List<String> result = jdbcTemplate.queryForList(GET_CP_RESTRICTION_SQL, String.class, contId);
		return new HashSet<String>(result);
	}

	private static final String[] specimenTypeNames = new String[]{"Tissue", "Fluid", "Cell", "Molecular"};

	private static final String GET_RESTRICTION_COUNT = 
			" select sum(restriction_count) from ( " +
			" select count(storage_container_id) as restriction_count from os_stor_cont_spec_types " + 
			" union all " +
			" select count(storage_container_id) as restriction_count from os_stor_cont_spec_classes " + 
			" union all " +
			" select count(storage_container_id) as restriction_count from os_stor_container_cps " + 
			" ) as count_table ";

	private static final String GET_PARENT_CONT_IDS_SQL = "select identifier from os_storage_containers where parent_container_id is null and activity_status <> 'Disabled'";

	private static final String GET_TYPE_RESTRICTION_SQL = "select specimen_type as specimen_type from catissue_stor_cont_spec_type where storage_container_id = ?";

	private static final String GET_CP_RESTRICTION_SQL = 
			" select cp.short_title as short_title from catissue_st_cont_coll_prot_rel cpRel " +
			" join catissue_collection_protocol cp on cpRel.collection_protocol_id = cp.identifier" +
			" where storage_container_id = ?";

}
package com.luminos.woosh.dao.hibernate;

import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.LogEntryDao;
import com.luminos.woosh.domain.common.LogEntry;

/**
 * 
 * @author Ben
 */
@Repository
public class LogEntryHibernateDaoImpl extends GenericWooshDaoHibernateImpl<LogEntry> implements LogEntryDao {

}

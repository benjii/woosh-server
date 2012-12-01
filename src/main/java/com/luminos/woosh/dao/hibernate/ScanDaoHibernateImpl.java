package com.luminos.woosh.dao.hibernate;

import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.ScanDao;
import com.luminos.woosh.domain.Scan;

/**
 * 
 * @author Ben
 */
@Repository
public class ScanDaoHibernateImpl extends GenericLuminosDaoHibernateImpl<Scan> implements ScanDao {

}

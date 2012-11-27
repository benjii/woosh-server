package com.luminos.woosh.dao.hibernate;

import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.CardDataDao;
import com.luminos.woosh.domain.CardData;

/**
 * 
 * @author Ben
 */
@Repository
public class CardDataDaoHibernateImpl extends GenericLuminosDaoHibernateImpl<CardData> implements CardDataDao {

}

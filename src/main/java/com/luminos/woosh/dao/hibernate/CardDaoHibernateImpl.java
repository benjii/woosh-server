package com.luminos.woosh.dao.hibernate;

import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.CardDao;
import com.luminos.woosh.domain.Card;

/**
 * 
 * @author Ben
 */
@Repository
public class CardDaoHibernateImpl extends GenericLuminosDaoHibernateImpl<Card> implements CardDao {

}

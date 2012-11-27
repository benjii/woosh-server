package com.luminos.woosh.dao.hibernate;

import org.springframework.stereotype.Repository;

import com.luminos.woosh.dao.OfferDao;
import com.luminos.woosh.domain.Offer;

/**
 * 
 * @author Ben
 */
@Repository
public class OfferDaoHibernateImpl extends GenericLuminosDaoHibernateImpl<Offer>implements OfferDao {

}

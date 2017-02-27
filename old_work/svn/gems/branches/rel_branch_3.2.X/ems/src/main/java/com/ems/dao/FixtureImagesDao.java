package com.ems.dao;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.FixtureImages;

@Repository("fixtureImagesDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FixtureImagesDao extends BaseDaoHibernate {

    public FixtureImages getFixtureImageById(Long id) {
        Criteria criteria = getSession().createCriteria(FixtureImages.class);
        criteria.add(Restrictions.eq("id", id));
        return (FixtureImages) criteria.list().get(0);
    }

    public Boolean getFixtureImageById(String fileName) {
        SQLQuery sqlQuery = getSession().createSQLQuery(
                "select id from fixture_images where image_name='" + fileName + "'");
        if (sqlQuery.list().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public FixtureImages getFixtureImageByName(String name) {
        Criteria criteria = getSession().createCriteria(FixtureImages.class);
        criteria.add(Restrictions.eq("imageName", name));
        if (criteria.list().size() > 0) {
            return (FixtureImages) criteria.list().get(0);
        } else {
            return null;
        }

    }

}

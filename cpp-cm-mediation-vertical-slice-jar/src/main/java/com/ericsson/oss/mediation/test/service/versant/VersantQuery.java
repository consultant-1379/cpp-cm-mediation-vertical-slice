package com.ericsson.oss.mediation.test.service.versant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;

public class VersantQuery {

    private final List<Object[]> params = new ArrayList<>();
    private final String hql;

    public VersantQuery(final String hql) {
        this.hql = hql;
    }

    public VersantQuery withParameter(final String name, final Object value) {
        params.add(new Object[] { name, value });
        return this;
    }

    public VersantQuery withParameter(final String name, final Date value, final TemporalType type) {
        params.add(new Object[] { name, value, type });
        return this;
    }

    public Query build(final EntityManager entityManager) {
        final Query query = entityManager.createQuery(this.hql);
        for (final Object[] param : params) {
            if (param.length == 2) {
                query.setParameter((String) param[0], param[1]);
            } else {
                query.setParameter((String) param[0], (Date) param[1], (TemporalType) param[2]);
            }
        }

        return query;
    }

    public String toString() {
        return this.hql;
    }
}
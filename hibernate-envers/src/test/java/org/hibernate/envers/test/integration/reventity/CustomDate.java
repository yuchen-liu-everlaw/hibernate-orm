/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test.integration.reventity;

import java.util.Arrays;
import java.util.Date;
import javax.persistence.EntityManager;

import org.hibernate.dialect.CockroachDialect;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.hibernate.envers.test.BaseEnversJPAFunctionalTestCase;
import org.hibernate.envers.test.Priority;
import org.hibernate.envers.test.entities.StrTestEntity;
import org.hibernate.envers.test.entities.reventity.CustomDateRevEntity;

import org.hibernate.testing.SkipForDialect;
import org.junit.Test;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class CustomDate extends BaseEnversJPAFunctionalTestCase {
	private Integer id;
	private long timestamp1;
	private long timestamp2;
	private long timestamp3;

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] {StrTestEntity.class, CustomDateRevEntity.class};
	}

	@Test
	@Priority(10)
	public void initData() throws InterruptedException {
		timestamp1 = System.currentTimeMillis();

		Thread.sleep( 1100 ); // CustomDateRevEntity.dateTimestamp field maps to date type which on some RDBMSs gets
		// truncated to seconds (for example MySQL 5.1).

		// Revision 1
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		StrTestEntity te = new StrTestEntity( "x" );
		em.persist( te );
		id = te.getId();
		em.getTransaction().commit();

		timestamp2 = System.currentTimeMillis();

		Thread.sleep( 1100 ); // CustomDateRevEntity.dateTimestamp field maps to date type which on some RDBMSs gets
		// truncated to seconds (for example MySQL 5.1).

		// Revision 2
		em.getTransaction().begin();
		te = em.find( StrTestEntity.class, id );
		te.setStr( "y" );
		em.getTransaction().commit();

		timestamp3 = System.currentTimeMillis();
	}

	@Test(expected = RevisionDoesNotExistException.class)
	public void testTimestamps1() {
		getAuditReader().getRevisionNumberForDate( new Date( timestamp1 ) );
	}

	@Test
	@SkipForDialect(value = CockroachDialect.class, comment = "Fails because of int size")
	public void testTimestamps() {
		assert getAuditReader().getRevisionNumberForDate( new Date( timestamp2 ) ).intValue() == 1;
		assert getAuditReader().getRevisionNumberForDate( new Date( timestamp3 ) ).intValue() == 2;
	}

	@Test
	public void testDatesForRevisions() {
		AuditReader vr = getAuditReader();
		assert vr.getRevisionNumberForDate( vr.getRevisionDate( 1 ) ).intValue() == 1;
		assert vr.getRevisionNumberForDate( vr.getRevisionDate( 2 ) ).intValue() == 2;
	}

	@Test
	public void testRevisionsForDates() {
		AuditReader vr = getAuditReader();

		assert vr.getRevisionDate( vr.getRevisionNumberForDate( new Date( timestamp2 ) ) ).getTime() <= timestamp2;
		assert vr.getRevisionDate( vr.getRevisionNumberForDate( new Date( timestamp2 ) ).intValue() + 1 )
				.getTime() > timestamp2;

		assert vr.getRevisionDate( vr.getRevisionNumberForDate( new Date( timestamp3 ) ) ).getTime() <= timestamp3;
	}

	@Test
	public void testFindRevision() {
		AuditReader vr = getAuditReader();

		long rev1Timestamp = vr.findRevision( CustomDateRevEntity.class, 1 ).getDateTimestamp().getTime();
		assert rev1Timestamp > timestamp1;
		assert rev1Timestamp <= timestamp2;

		long rev2Timestamp = vr.findRevision( CustomDateRevEntity.class, 2 ).getDateTimestamp().getTime();
		assert rev2Timestamp > timestamp2;
		assert rev2Timestamp <= timestamp3;
	}

	@Test
	public void testRevisionsCounts() {
		assert Arrays.asList( 1, 2 ).equals( getAuditReader().getRevisions( StrTestEntity.class, id ) );
	}

	@Test
	public void testHistoryOfId1() {
		StrTestEntity ver1 = new StrTestEntity( "x", id );
		StrTestEntity ver2 = new StrTestEntity( "y", id );

		assert getAuditReader().find( StrTestEntity.class, id, 1 ).equals( ver1 );
		assert getAuditReader().find( StrTestEntity.class, id, 2 ).equals( ver2 );
	}
}
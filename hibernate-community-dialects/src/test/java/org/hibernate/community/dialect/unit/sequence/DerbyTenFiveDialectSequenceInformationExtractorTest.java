/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.community.dialect.unit.sequence;

import org.hibernate.community.dialect.DerbyLegacyDialect;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.Dialect;
import org.hibernate.orm.test.dialect.unit.sequence.AbstractSequenceInformationExtractorTest;
import org.hibernate.tool.schema.extract.internal.SequenceInformationExtractorNoOpImpl;
import org.hibernate.tool.schema.extract.spi.SequenceInformationExtractor;

import org.hibernate.testing.TestForIssue;

/**
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HHH-11470")
public class DerbyTenFiveDialectSequenceInformationExtractorTest extends AbstractSequenceInformationExtractorTest {

	@Override
	public Dialect getDialect() {
		return new DerbyLegacyDialect( DatabaseVersion.make( 10, 5 ) );
	}

	@Override
	public String expectedQuerySequencesString() {
		return null;
	}

	@Override
	public Class<? extends SequenceInformationExtractor> expectedSequenceInformationExtractor() {
		return SequenceInformationExtractorNoOpImpl.class;
	}

}
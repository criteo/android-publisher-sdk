package com.criteo.publisher.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class BannerAdUnitTest {

	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(BannerAdUnit.class)
				.withRedefinedSuperclass()
				.verify();
	}

}
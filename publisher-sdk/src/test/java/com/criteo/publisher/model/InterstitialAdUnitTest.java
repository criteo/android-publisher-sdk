package com.criteo.publisher.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class InterstitialAdUnitTest {

	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(InterstitialAdUnit.class)
				.withRedefinedSuperclass()
				.verify();
	}

}
package com.nistapp.uda.index.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FieldLimitValidationTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeAll
	static void setUp() {
		factory = Validation.byDefaultProvider().configure()
				.messageInterpolator(new ParameterMessageInterpolator()).buildValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterAll
	static void tearDown() {
		factory.close();
	}

	@ParameterizedTest
	@CsvSource({"ClickTrack,clicktype,200", "ClickTrack,clickedname,2000", "SequenceList,name,5000",
			"SequenceList,usersessionid,500", "SequenceList,userclicknodelist,5000", "SequenceList,domain,500"})
	void sizeLimitIsEnforced(String type, String field, int max) {
		Class<?> beanClass = type.equals("ClickTrack") ? ClickTrack.class : SequenceList.class;
		assertEquals(0, validator.validateValue(beanClass, field, "a".repeat(max)).size());
		assertEquals(1, validator.validateValue(beanClass, field, "a".repeat(max + 1)).size());
	}
}

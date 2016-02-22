package it.unipi.di.acube.batframework.utils;

import static org.junit.Assert.*;

import it.unipi.di.acube.batframework.utils.FreebaseApi;

import org.junit.Test;

public class FreebaseApiTest {

	@Test
	public void testMidToTitle() throws Exception {
		FreebaseApi api = new FreebaseApi(null);
		assertEquals("East Ridge High School (Kentucky)", api.midToTitle("/m/03ck4lv"));
		assertEquals("East Ridge High School (Kentucky)", api.midToTitle("m/03ck4lv"));
		assertEquals("Bowflex", api.midToTitle("/m/04cnvy"));
		assertEquals("Brooks Brothers", api.midToTitle("/m/03d452"));
		assertEquals("Cass County, Missouri", api.midToTitle("/m/0nfgq"));
	}

}

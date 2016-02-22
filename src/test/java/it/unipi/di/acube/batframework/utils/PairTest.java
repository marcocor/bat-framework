package it.unipi.di.acube.batframework.utils;

import static org.junit.Assert.*;

import java.util.HashSet;

import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.utils.Pair;

import org.junit.Before;
import org.junit.Test;

public class PairTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		String s1 = "aaa";
		String s2 = "aaa";
		String s3 = "bbb";
		
		HashSet<Mention> set1 = new HashSet<>();
		set1.add(new Mention(12,  6));
		set1.add(new Mention(19,  6));
		
		HashSet<Mention> set2 = new HashSet<>();
		set2.add(new Mention(12,  6));
		set2.add(new Mention(19,  6));

		HashSet<Mention> set3 = new HashSet<>();
		set3.add(new Mention(12,  6));

		
		Pair<String, HashSet<Mention>> p1 = new Pair<>(s1, set1);
		Pair<String, HashSet<Mention>> p2 = new Pair<>(s2, set2);
		Pair<String, HashSet<Mention>> p3 = new Pair<>(s3, set3);
		
		Pair<String, HashSet<Mention>> p4 = new Pair<>(s2, set3);
		Pair<String, HashSet<Mention>> p5 = new Pair<>(s3, set2);
		
		assertEquals(p1, p2);
		assertTrue(p1.equals(p2));
		assertTrue(p2.equals(p1));
		
		assertFalse(p3.equals(p2));
		assertFalse(p2.equals(p3));
		
		assertFalse(p3.equals(p4));
		assertFalse(p4.equals(p3));

		assertFalse(p3.equals(p5));
		assertFalse(p5.equals(p3));
		
		assertEquals(p1.hashCode(), p2.hashCode());
		assertFalse(p3.hashCode() == p2.hashCode());
		assertFalse(p3.hashCode() == p4.hashCode());
		assertFalse(p3.hashCode() == p5.hashCode());
	}

}

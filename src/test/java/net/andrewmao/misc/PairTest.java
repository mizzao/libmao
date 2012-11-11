package net.andrewmao.misc;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PairTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEquals() {
		
		Pair<String, String> p1 = new Pair<String, String>("haha", "yaha");		
		Pair<String, String> p2 = new Pair<String, String>("haha", "yaha");
		
		assertEquals(p1, p2);
		assertEquals(p1.hashCode(), p2.hashCode());
		
		Pair<String, String> p3 = new Pair<String, String>("haha", "gaha");
		Pair<String, String> p4 = new Pair<String, String>("gaha", "yaha");
		Pair<String, String> p5 = new Pair<String, String>("gaha", "gaha");
		
		assertFalse(p1.equals(p3));
		assertFalse(p1.equals(p4));
		assertFalse(p1.equals(p5));
		
		assertFalse( p1.hashCode() == p3.hashCode() );
		assertFalse( p1.hashCode() == p4.hashCode() );
		assertFalse( p1.hashCode() == p5.hashCode() );		
		
	}

}

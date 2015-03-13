package iop.storet;

import org.junit.Before;
import org.junit.BeforeClass;

public class CCD2D4MTest {
	
	static CCD2D4M app;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String[] ss = {"-i", "", "-h", "", "-u", "", "-f", "", "-m", "", "-t", "text|textt|ttext|", "-z", "-1"};
		app = new CCD2D4M(ss);
	}

	@Before
	public void setUp() throws Exception {
	}
}

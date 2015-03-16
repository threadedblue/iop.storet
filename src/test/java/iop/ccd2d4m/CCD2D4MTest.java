package iop.ccd2d4m;

import iop.ccd.shread.util.C;

import java.io.File;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.ll.cloud.connection.ConnectionProperties;
import edu.mit.ll.d4m.db.cloud.accumulo.AccumuloConnection;

public class CCD2D4MTest {
	
	static CCD2D4M app;
	static ConnectionProperties props;
	static AccumuloConnection conn;
	static final String STEM = "test_ccd";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String[] ss = {"-i", "accumulo", "-h", "localhost:2181", "-u", "evolent", "-f", "", "-m", STEM, "-x", "strip-text.xsl", "-z", "-1", "-s"};
		app = new CCD2D4M(ss);
		props = new ConnectionProperties("localhost:2181", "evolent", "", "accumulo", null);
		props.setMaxNumThreads(10);
		conn = new AccumuloConnection(props);
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		conn.deleteTable(STEM + C.TEXT);
		conn.deleteTable(STEM + C.DEGREE);
		conn.deleteTable(STEM + C.TRANSFORM);
		conn.deleteTable(STEM);
	}

	@Test
	public void testCCD2D4MConstructor() throws Exception {
		URL url = getClass().getClassLoader().getResource("ccd.xml");
		File ccd = new File(url.toURI());
		app.insertCCD(ccd);
	}
}

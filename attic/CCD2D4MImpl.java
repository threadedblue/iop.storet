package iox.xml2xpath;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.dom4j.Document;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.mit.ll.cloud.connection.ConnectionProperties;
import edu.mit.ll.d4m.db.cloud.D4mException;
import edu.mit.ll.d4m.db.cloud.accumulo.AccumuloConnection;
import edu.mit.ll.d4m.db.cloud.accumulo.AccumuloInsert;
import edu.mit.ll.d4m.db.cloud.accumulo.AccumuloTableOperations;

public class CCD2D4MImpl implements Runnable, CCD2D4M {

	private static final Logger log = LoggerFactory.getLogger(CCD2D4MImpl.class);

	private CmdLineParser CLI = new CmdLineParser(this);

	@Option(name = "-i", aliases = "--instance", required = false, usage = "Accumulo instance name")
	private String instance;

	@Option(name = "-h", aliases = "--host", required = false, usage = "Zookeeper host name with port")
	private String host;

	@Option(name = "-u", aliases = "--user", required = false, usage = "Accumulo user name")
	private String user;

	@Option(name = "-p", aliases = "--password", required = false, usage = "Accumulo password")
	private String password = "";

	@Option(name = "-m", aliases = "--stem", required = true, usage = "Stem value for D4M table names")
	private String stem;

	@Option(name = "-l", aliases = "--dblogi", required = false, usage = "Contruct input for dblLogi.")
	private boolean dblLogi;

	@Option(name = "-f", aliases = "--path", required = true, usage = "Path to the directory or file.")
	private String path;

	@Option(name = "-x", aliases = "--style", required = false, usage = "XSL file name")
	private String xsl;
	
	@Option(name = "-in", aliases = "--input", required = false, usage = "Path to input CCDs.")
	String input;

	@Option(name = "-out", aliases = "--output", required = false, usage = "Path to output data.")
	String output;

	private String tab;
	private String tabT;
	private String tabDeg;
	private String tabTxt;

	AccumuloInsert ins;
	AccumuloInsert insT;
	AccumuloInsert insDeg;
	AccumuloInsert insTxt;

	int documentCount;
	int elementCountper;
	int elementCount;

	XSLTProcessor xslt;
	
	public CCD2D4MImpl(String[] args) throws CmdLineException {
		super();
		try {
			CLI.parseArgument(args);
			ConnectionProperties props = new ConnectionProperties();
			props.setInstanceName(instance);
			props.setHost(host);
			props.setUser(user);
			props.setPass(password);
			props.setMaxNumThreads(50);
			init(props);
		} catch (CmdLineException e) {
			CLI.printUsage(System.out);
			throw e;
		}
	}
	
	public CCD2D4MImpl(ConnectionProperties props) {
		init(props);
	}
	
	public void init(ConnectionProperties props) {
		AccumuloConnection conn = new AccumuloConnection(props);
		tab = stem;
		ins = new AccumuloInsert(props, tab);
		conn.createTable(tab);
		tabT = stem + C.TRANSFORM;
		insT = new AccumuloInsert(props, tabT);
		conn.createTable(tabT);
		tabDeg = stem + C.DEGREE;
		insDeg = new AccumuloInsert(props, tabDeg);
		conn.createTable(tabDeg);
		tabTxt = stem + C.TEXT;
		insTxt = new AccumuloInsert(props, tabTxt);
		conn.createTable(tabTxt);
	}
	
	/* (non-Javadoc)
	 * @see iop.ccd2d4m.CCD2D4M#run()
	 */
	@Override
	public void run() {
		log.trace("run==>");
		File fileList = new File(path);
		File[] list = fileList.listFiles();
		ConnectionProperties props = new ConnectionProperties();
		props.setInstanceName(instance);
		props.setHost(host);
		props.setUser(user);
		props.setPass(password);

		props.setMaxNumThreads(10);
		AccumuloTableOperations ops = new AccumuloTableOperations(props);
		try {
			ops.designateCombiningColumns(tabDeg, "Degree", "sum", "");
		} catch (D4mException e) {
			log.error("", e);
		}
		for (File file : list) {
			insertCCD(file);
			documentCount++;
			heartBeat();
		}
		log.trace("<==run");
	}

	void insertCCD(File file) {
		log.trace("insertCCD==>");
		String ccd = readFileContents(file);
		Document tranformedDoc = transformDoc(ccd);
		XML2Set xml2Set = new XML2PathSetImpl(tranformedDoc);
		RowsColsVals rcvs = xml2Set.build();
		elementCountper = rcvs.size();
		elementCount += elementCountper;
		String[] rcv4Ingest = null;
		rcv4Ingest = rcvs.assemble4Ingest();
		ins.doProcessing(rcv4Ingest[C.ROW], rcv4Ingest[C.COL],
				rcv4Ingest[C.VAL], "", "");
		insT.doProcessing(rcv4Ingest[C.COL], rcv4Ingest[C.ROW],
				rcv4Ingest[C.VAL], "", "");
		String[] rcv4Degree = assemble4Degree(rcv4Ingest);
		insDeg.doProcessing(rcv4Degree[C.ROW], rcv4Degree[C.COL],
				rcv4Degree[C.VAL], "", "");
		log.trace("<==insertCCD");
	}

	Document transformDoc(String ccd) {
		Document doc = AbstractXML2SetImpl.parse(ccd);
		return transformDoc(doc);
	}

	Document transformDoc(Document doc) {
		XSLTProcessor xslt = null;
		xslt = getXSLTProcessor();
		return xslt.run(doc);
	}

	XSLTProcessor getXSLTProcessor() {
		try {
			if (xslt == null) {
				String xslContents = readFileContents(xsl);
				xslt = new XSLTProcessor(xslContents);
			}
		} catch (URISyntaxException e) {
			log.error("", e);
		}
		return xslt;
	}

	String lastCharOf(String s) {
		return s.substring(s.length() - 1);
	}

	public String readFileContents(String filename) throws URISyntaxException {
		log.debug("filename=" + filename);
		URL url = getClass().getClassLoader().getResource(filename);
		log.debug("url=" + url);
		return readFileContents(url.toURI());
	}

	public String readFileContents(File filename) {
		java.net.URI uri = filename.toURI();
		return readFileContents(uri);
	}

	public String readFileContents(URI uri) {
		String s = null;
		try {
			s = new String(Files.readAllBytes(Paths.get(uri)));
		} catch (IOException e) {
			log.error("", e);
		}
		return s;
	}

	String[] assemble4Degree(String[] rcv) {
		String[] sss = new String[3];
		sss[C.ROW] = rcv[C.ROW].split("\t")[0] + C.DEFAULT_DELIMITER;
		sss[C.COL] = "Degree" + C.DEFAULT_DELIMITER;
		sss[C.VAL] = String.valueOf(rcv[C.VAL].split("\t").length)
				+ C.DEFAULT_DELIMITER;
		return sss;
	}

	void heartBeat() {
		if (documentCount % 100 == 0) {
			log.info("documents=" + documentCount);
			log.info("elements per=" + elementCountper);
			log.info("elements=" + elementCount);
		}
	}

	public static void main(String[] args) {
		try {
			CCD2D4M app = new CCD2D4MImpl(args);
			log.info("Start==>");
			app.run();
			log.info("<==Finish");
		} catch (CmdLineException e) {
			log.error("Soaping is wrong.", e);
		}
	}

	public AccumuloInsert getIns() {
		return ins;
	}

	public AccumuloInsert getInsT() {
		return insT;
	}

	public AccumuloInsert getInsDeg() {
		return insDeg;
	}

	public AccumuloInsert getInsTxt() {
		return insTxt;
	}
}

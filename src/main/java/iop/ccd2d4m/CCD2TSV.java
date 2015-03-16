package iop.ccd2d4m;

import iop.ccd.shread.RowColVal;
import iop.ccd.shread.XML2Set;
import iop.ccd.shread.impl.AbstractXML2SetImpl;
import iop.ccd.shread.impl.XML2PathSetImpl;
import iop.ccd.shread.util.CCDUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Reads all files in the sample cda directory and writes them to a set of *.tsv files.
// There is one *.tsv file per cda.
// Each *.tsv file has two rows: (1) a header and (2) values.
// The file(s) can successfully be read in using D4M and inserted into Accumulo.

public class CCD2TSV {

	private static final Logger log = LoggerFactory.getLogger(CCD2D4M.class);

	private CmdLineParser CLI = new CmdLineParser(this);

	@Option(name = "-m", aliases = "--stem", required = true, usage = "Stem value for D4M table names")
	private String stem;

	@Option(name = "-s", aliases = "--sansroot", required = false, usage = "Construct without path root.")
	private Boolean sansroot;

	@Option(name = "-e", aliases = "--endian", required = false, usage = "Construct T big or F little endian values.")
	private Boolean endian;

	@Option(name = "-i", aliases = "--input", required = true, usage = "Path to the input directory.")
	private String input;

	@Option(name = "-o", aliases = "--output", required = true, usage = "Path to the output directory.")
	private String output;
	
	public CCD2TSV(String[] args) throws CmdLineException {
		super();
		try {
			CLI.parseArgument(args);
		} catch (CmdLineException e) {
			CLI.printUsage(System.out);
			throw e;
		}
		log.info("CCD2TSV==>");
	}

	public void run() throws IOException {
		File dirIn = new File(input);
		File dirOut = new File(output);
		CCDUtils util = new CCDUtils();

		File[] list = CollectionSampler.assembleRands(2, dirIn);
		for (File file : list) {
			String[] fileName = file.getName().split(".xml");
			fileName[0] += ".tsv";
			log.info("fileName=" + fileName.toString());
			String scd = util.readCCD(file);
			Document doc = AbstractXML2SetImpl.parse(scd);
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
					dirOut, fileName[0])));
			XML2Set tedge = new XML2PathSetImpl(doc);
			List<RowColVal> rcvs = tedge.build();
			StringBuilder head = new StringBuilder();
			StringBuilder tail = new StringBuilder();
			boolean first = true;
			for (RowColVal rcv : rcvs) {
				if (first) {
					head.append('\t');
					tail.append(rcv.getRow()).append('\t');
					first = false;
				} else {
					head.append(rcv.getCol()).append('\t');
					tail.append(rcv.getVal()).append('\t');
				}
			}
			head.append('\n');
			tail.append('\n');
			writer.write(head.toString());
			writer.write(tail.toString());
			writer.close();
		}
	}

	public static void main(String[] args) {
		try {
			CCD2TSV app = new CCD2TSV(args);
			app.run();
		} catch (CmdLineException e) {
			log.error("Soaping is wrong.", e);
		} catch (IOException e) {
			log.error("Soaping is wrong.", e);
		}
	}
}
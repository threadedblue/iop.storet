package iop.storet.marshaler.impl;

import iop.account.account.Account;
import iop.storet.accumulod4m.XML2Pair;
import iop.storet.accumulod4m.builders.XML2PairImpl;
import iop.storet.accumulod4m.builders.XML2PairImpl.RowColVal;
import iop.storet.accumulod4m.util.C;
import iop.storet.marshaler.TedgeMarshaler;

import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public abstract class AbstractMarhalerImpl implements TedgeMarshaler {

	private static final Logger log = LoggerFactory
			.getLogger(AbstractMarhalerImpl.class);

	String scd;
	Account acct;
	C.ENDIAN endian;
	C.SANS_ROOT root;
	C.DELIMITER delim;

	@Override
	public abstract void build();

	public String formatAcctNo(long acctNo) {
		NumberFormat fmt = DecimalFormat.getInstance();
		if (fmt instanceof DecimalFormat) {
			((DecimalFormat) fmt).applyPattern(ACCOUNT_NO_FORMAT);
		}
		return fmt.format(acctNo);
	}

	@SuppressWarnings("rawtypes")
	List<RowColVal> getFamsNVals(String scd) {
		XML2Pair xml = new XML2PairImpl(scd, endian, root, delim, true);
		return xml.build();
	}

	String getDocumentId(String scd) throws Exception {
		Reader reader = new StringReader(scd);
		InputSource is = new InputSource(reader);
		ClinicalDocument cd = CDAUtil.load(is);
		return getDocumentId(cd);
	}

	String getDocumentId(ClinicalDocument cd) {
		II ii = cd.getId();
		StringBuilder bld = new StringBuilder();
		bld.append(ii.getRoot() != null ? ii.getRoot() : "was null");
		bld.append(C.DEFAULT_DELIMITER);
		bld.append(ii.getExtension() != null ? ii.getExtension() : "");
		return bld.toString();
	}

	String getDocumentDate(ClinicalDocument cd) {
		TS ts = cd.getEffectiveTime();
		return ts.getValue();
	}
}

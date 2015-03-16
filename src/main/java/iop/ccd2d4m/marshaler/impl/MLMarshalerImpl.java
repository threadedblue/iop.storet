package iop.storet.marshaler.impl;

import iop.storet.accumulod4m.builders.XML2PairImpl.RowColVal;
import iop.storet.accumulod4m.util.C;
import iop.storet.accumulod4m.util.U;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mtf.MTF;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MLMarshalerImpl extends AbstractMarhalerImpl {

	private static final Logger log = LoggerFactory
			.getLogger(MLMarshalerImpl.class);

	private static final String VAL = "1";

	public enum KEY {
		ROW, COL, VAL
	};

	private Map<KEY, Object> result = new HashMap<KEY, Object>();

	public MLMarshalerImpl(String scd, C.ENDIAN endian, C.SANS_ROOT root,
			C.DELIMITER delim) {
		this.scd = scd;
		this.endian = endian;
		this.root = root;
		this.delim = delim;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void build() {
		int no = MTF.getNextAsInteger(4);
		String documentId = null;
		try {
			documentId = getDocumentId(scd);
		} catch (Exception e) {
			log.error("", e);
		}
		List<RowColVal> famsNvals = getFamsNVals(scd);
		// DocumentId with account No prepended is too much to handle.
		// String rowID = U.assembleRowId(formatAcctNo(new Long(no)),
		// documentId);
		String rowID = documentId;
		result.put(KEY.ROW, rowID);
		result.put(KEY.COL, famsNvals);
	}

	public Map<KEY, Object> getResult() {
		return result;
	}
}

package iop.ccd2d4m;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mtf.MTF;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionSampler {

	private static final Logger log = LoggerFactory.getLogger(CollectionSampler.class);

	public static File[] assembleRands(File dir) {
		return assembleRands(MTF.DEFAULT_SAMPLE_SIZE, dir);
	}
	
	public static File[] assembleRands(int sampleSize, File dir) {
		File[] files = dir.listFiles();
		if (sampleSize < 0) {
			return files;
		}
		Integer[] rands = MTF.generateRands(files.length, sampleSize, true);
		List<File> list = getSample(files, rands);
		return list.toArray(new File[list.size()]);
	}

	static List<File> getSample(File[] files, Integer[] rands) {
		List<File> list = new ArrayList<File>();
		try {
			// We iterate through the file list selecting those items
			// whose index is found in the random set
			// returning a randomized selection from the file list.
			for (int i = 0; i < files.length; i++) {
				if (Arrays.binarySearch(rands, i) >= 0) {
					list.add(files[i]);
				}
				if (list.size() >= rands.length) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("count=" + files.length, e);
		}
		return list;
	}
}

package ua.mariko.dupfinder;

import java.text.DecimalFormat;

public class DupFinderInfo {

	public int totalCount;
	public int duplicateCount;
	public long sizeDeleted;
	public long sizeTotal;
	public int possibleDuplicatedFiles;
	public long possibleDuplicatedFilesSize;
	
	public static String formatBytes(long value) {

		DecimalFormat format = (DecimalFormat) DecimalFormat
				.getNumberInstance();
		format.applyPattern("#,###.##");

		String suffix = "b";
		long divisor = 1024;
		String nbsp = "&nbsp;";
		String[] scale = { nbsp, "K", "M", "G", "T", };

		float scaledValue = 0;
		String scaleSuffix = scale[0];
		if (value != 0) {
			for (int i = scale.length - 1; i >= 0; i--) {
				long div = (long) Math.pow(divisor, i);
				if (value >= div) {
					scaledValue = (float) (1.0 * value / div);
					scaleSuffix = scale[i];
					break;
				}
			}
		}
		StringBuilder sb = new StringBuilder(3);
		sb.append(format.format(scaledValue));

		sb.append(" ");
		if (!scaleSuffix.equals(scale[0])) {
			sb.append(scaleSuffix);
		}

		sb.append(suffix);
		return sb.toString();
	}
}

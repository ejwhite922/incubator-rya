package mvm.rya.cloudbase.utils.input;

import cloudbase.core.CBConstants;
import cloudbase.core.client.*;
import cloudbase.core.client.impl.Tables;
import cloudbase.core.client.impl.TabletLocator;
import cloudbase.core.data.*;
import cloudbase.core.security.Authorizations;
import cloudbase.core.security.TablePermission;
import cloudbase.core.security.thrift.AuthInfo;
import cloudbase.core.util.ArgumentChecker;
import cloudbase.core.util.Pair;
import cloudbase.core.util.TextUtil;
import cloudbase.core.util.UtilWaitThread;
import cloudbase.core.util.format.DefaultFormatter;
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class allows MapReduce jobs to use Cloudbase as the source of data. This
 * input format provides keys and values of type Key and Value to the Map() and
 * Reduce() functions.
 *
 * The user must specify the following via static methods:
 *
 * <ul>
 * <li>CloudbaseInputFormat.setInputTableInfo(job, username, password, table,
 * auths)
 * <li>CloudbaseInputFormat.setZooKeeperInstance(job, instanceName, hosts)
 * </ul>
 *
 * Other static methods are optional
 */
public class CloudbaseBatchScannerInputFormat extends InputFormat<Key, Value> {
	private static final Logger log = Logger.getLogger(CloudbaseBatchScannerInputFormat.class);

	private static final String PREFIX = CloudbaseBatchScannerInputFormat.class.getSimpleName();
	private static final String INPUT_INFO_HAS_BEEN_SET = PREFIX + ".configured";
	private static final String INSTANCE_HAS_BEEN_SET = PREFIX + ".instanceConfigured";
	private static final String USERNAME = PREFIX + ".username";
	private static final String PASSWORD = PREFIX + ".password";
	private static final String TABLE_NAME = PREFIX + ".tablename";
	private static final String AUTHORIZATIONS = PREFIX + ".authorizations";

	private static final String INSTANCE_NAME = PREFIX + ".instanceName";
	private static final String ZOOKEEPERS = PREFIX + ".zooKeepers";
	private static final String MOCK = ".useMockInstance";

	private static final String RANGES = PREFIX + ".ranges";
	private static final String AUTO_ADJUST_RANGES = PREFIX + ".ranges.autoAdjust";

	private static final String ROW_REGEX = PREFIX + ".regex.row";
	private static final String COLUMN_FAMILY_REGEX = PREFIX + ".regex.cf";
	private static final String COLUMN_QUALIFIER_REGEX = PREFIX + ".regex.cq";
	private static final String VALUE_REGEX = PREFIX + ".regex.value";

	private static final String COLUMNS = PREFIX + ".columns";
	private static final String LOGLEVEL = PREFIX + ".loglevel";

	private static final String ISOLATED = PREFIX + ".isolated";

	//Used to specify the maximum # of versions of a Cloudbase cell value to return
	private static final String MAX_VERSIONS = PREFIX + ".maxVersions";

	//Used for specifying the iterators to be applied
	private static final String ITERATORS = PREFIX + ".iterators";
	private static final String ITERATORS_OPTIONS = PREFIX + ".iterators.options";
	private static final String ITERATORS_DELIM = ",";
    private BatchScanner bScanner;

    /**
	 * Enable or disable use of the {@link cloudbase.core.client.IsolatedScanner}.  By default it is not enabled.
	 *
	 * @param job
	 * @param enable
	 */
	public static void setIsolated(JobContext job, boolean enable){
		Configuration conf = job.getConfiguration();
		conf.setBoolean(ISOLATED, enable);
	}

	public static void setInputInfo(JobContext job, String user, byte[] passwd, String table, Authorizations auths) {
		Configuration conf = job.getConfiguration();
		if (conf.getBoolean(INPUT_INFO_HAS_BEEN_SET, false))
			throw new IllegalStateException("Input info can only be set once per job");
		conf.setBoolean(INPUT_INFO_HAS_BEEN_SET, true);

		ArgumentChecker.notNull(user, passwd, table);
		conf.set(USERNAME, user);
		conf.set(PASSWORD, new String(Base64.encodeBase64(passwd)));
		conf.set(TABLE_NAME, table);
		if (auths != null && !auths.isEmpty())
			conf.set(AUTHORIZATIONS, auths.serialize());
	}

	public static void setZooKeeperInstance(JobContext job, String instanceName, String zooKeepers) {
		Configuration conf = job.getConfiguration();
		if (conf.getBoolean(INSTANCE_HAS_BEEN_SET, false))
			throw new IllegalStateException("Instance info can only be set once per job");
		conf.setBoolean(INSTANCE_HAS_BEEN_SET, true);

		ArgumentChecker.notNull(instanceName, zooKeepers);
		conf.set(INSTANCE_NAME, instanceName);
		conf.set(ZOOKEEPERS, zooKeepers);
	}

	public static void setMockInstance(JobContext job, String instanceName) {
	    Configuration conf = job.getConfiguration();
	    conf.setBoolean(INSTANCE_HAS_BEEN_SET, true);
	    conf.setBoolean(MOCK, true);
		conf.set(INSTANCE_NAME, instanceName);
	}

	public static void setRanges(JobContext job, Collection<Range> ranges) {
		ArgumentChecker.notNull(ranges);
		ArrayList<String> rangeStrings = new ArrayList<String>(ranges.size());
		try {
		    for (Range r : ranges) {
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        r.write(new DataOutputStream(baos));
		        rangeStrings.add(new String(Base64.encodeBase64(baos.toByteArray())));
		    }
		} catch (IOException ex) {
		    throw new IllegalArgumentException("Unable to encode ranges to Base64", ex);
		}
		job.getConfiguration().setStrings(RANGES, rangeStrings.toArray(new String[0]));
	}

	public static void disableAutoAdjustRanges(JobContext job) {
		job.getConfiguration().setBoolean(AUTO_ADJUST_RANGES, false);
	}

	public static enum RegexType {
		ROW, COLUMN_FAMILY, COLUMN_QUALIFIER, VALUE
	}

	public static void setRegex(JobContext job, RegexType type, String regex) {
		ArgumentChecker.notNull(type, regex);
		String key = null;
		switch (type) {
		case ROW:
			key = ROW_REGEX;
			break;
		case COLUMN_FAMILY:
			key = COLUMN_FAMILY_REGEX;
			break;
		case COLUMN_QUALIFIER:
			key = COLUMN_QUALIFIER_REGEX;
			break;
		case VALUE:
			key = VALUE_REGEX;
			break;
		default:
			throw new NoSuchElementException();
		}
		try {
			job.getConfiguration().set(key, URLEncoder.encode(regex, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("Failedd to encode regular expression",e);
			throw new RuntimeException(e);
		}
	}




	/**
	 * Sets the max # of values that may be returned for an individual Cloudbase cell. By default, applied before all other
	 * Cloudbase iterators (highest priority) leveraged in the scan by the record reader.  To adjust priority use
	 * setIterator() & setIteratorOptions() w/ the VersioningIterator type explicitly.
	 *
	 * @param job the job
	 * @param maxVersions the max versions
	 * @throws java.io.IOException
	 */
	public static void setMaxVersions(JobContext job, int maxVersions) throws IOException{
		if (maxVersions < 1) throw new IOException("Invalid maxVersions: " + maxVersions + ".  Must be >= 1");
		job.getConfiguration().setInt(MAX_VERSIONS, maxVersions);
	}

	/**
	 *
	 * @param columnFamilyColumnQualifierPairs
	 *            A pair of {@link org.apache.hadoop.io.Text} objects corresponding to column family
	 *            and column qualifier. If the column qualifier is null, the
	 *            entire column family is selected. An empty set is the default
	 *            and is equivalent to scanning the all columns.
	 */
	public static void fetchColumns(JobContext job, Collection<Pair<Text, Text>> columnFamilyColumnQualifierPairs) {
		ArgumentChecker.notNull(columnFamilyColumnQualifierPairs);
		ArrayList<String> columnStrings = new ArrayList<String>(columnFamilyColumnQualifierPairs.size());
		for (Pair<Text, Text> column : columnFamilyColumnQualifierPairs) {
			if(column.getFirst() == null)
				throw new IllegalArgumentException("Column family can not be null");

			String col = new String(Base64.encodeBase64(TextUtil.getBytes(column.getFirst())));
			if (column.getSecond() != null)
				col += ":" + new String(Base64.encodeBase64(TextUtil.getBytes(column.getSecond())));
			columnStrings.add(col);
		}
		job.getConfiguration().setStrings(COLUMNS, columnStrings.toArray(new String[0]));
	}
//
//	public static void setLogLevel(JobContext job, Level level) {
//		ArgumentChecker.notNull(level);
//		log.setLevel(level);
//		job.getConfiguration().setInt(LOGLEVEL, level.toInt());
//	}


	/**
	 * Specify a Cloudbase iterator type to manage the behavior of the underlying table scan this InputFormat's Record Reader will conduct, w/ priority dictating the order
	 * in which specified iterators are applied. Repeat calls to specify multiple iterators are allowed.
	 *
	 * @param job the job
	 * @param priority the priority
	 * @param iteratorClass the iterator class
	 * @param iteratorName the iterator name
	 */
	public static void setIterator(JobContext job, int priority, String iteratorClass, String iteratorName){
		//First check to see if anything has been set already
		String iterators = job.getConfiguration().get(ITERATORS);

		//No iterators specified yet, create a new string
		if (iterators == null || iterators.isEmpty()) {
			iterators = new CBIterator(priority, iteratorClass, iteratorName).toString();
		}
		else {
			//append the next iterator & reset
			iterators = iterators.concat(ITERATORS_DELIM + new CBIterator(priority, iteratorClass, iteratorName).toString());
		}
		//Store the iterators w/ the job
		job.getConfiguration().set(ITERATORS, iterators);
	}


	/**
	 * Specify an option for a named Cloudbase iterator, further specifying that iterator's
	 * behavior.
	 *
	 * @param job the job
	 * @param iteratorName the iterator name.  Should correspond to an iterator set w/ a prior setIterator call.
	 * @param key the key
	 * @param value the value
	 */
	public static void setIteratorOption(JobContext job, String iteratorName, String key, String value){
	    if (value == null) return;

		String iteratorOptions = job.getConfiguration().get(ITERATORS_OPTIONS);

		//No options specified yet, create a new string
		if (iteratorOptions == null || iteratorOptions.isEmpty()){
			iteratorOptions = new CBIteratorOption(iteratorName, key, value).toString();
		}
		else {
			//append the next option & reset
			iteratorOptions = iteratorOptions.concat(ITERATORS_DELIM + new CBIteratorOption(iteratorName, key, value));
		}

		//Store the options w/ the job
		job.getConfiguration().set(ITERATORS_OPTIONS, iteratorOptions);
	}

	protected static boolean isIsolated(JobContext job){
		return job.getConfiguration().getBoolean(ISOLATED, false);
	}

	protected static String getUsername(JobContext job) {
		return job.getConfiguration().get(USERNAME);
	}


	/**
	 * WARNING: The password is stored in the Configuration and shared with all
	 * MapReduce tasks; It is BASE64 encoded to provide a charset safe
	 * conversion to a string, and is not intended to be secure.
	 */
	protected static byte[] getPassword(JobContext job) {
		return Base64.decodeBase64(job.getConfiguration().get(PASSWORD, "").getBytes());
	}

	protected static String getTablename(JobContext job) {
		return job.getConfiguration().get(TABLE_NAME);
	}

	protected static Authorizations getAuthorizations(JobContext job) {
		String authString = job.getConfiguration().get(AUTHORIZATIONS);
		return authString == null ? CBConstants.NO_AUTHS : new Authorizations(authString.split(","));
	}

	protected static Instance getInstance(JobContext job) {
		Configuration conf = job.getConfiguration();
//		if (conf.getBoolean(MOCK, false))
//		    return new MockInstance(conf.get(INSTANCE_NAME));
		return new ZooKeeperInstance(conf.get(INSTANCE_NAME), conf.get(ZOOKEEPERS));
	}

	protected static TabletLocator getTabletLocator(JobContext job) throws TableNotFoundException {
//		if (job.getConfiguration().getBoolean(MOCK, false))
//			return new MockTabletLocator();
		Instance instance = getInstance(job);
		String username = getUsername(job);
		byte[] password = getPassword(job);
		String tableName = getTablename(job);
		return TabletLocator.getInstance(instance, new AuthInfo(username, password, instance.getInstanceID()), new Text(Tables.getTableId(instance, tableName)));
	}

	protected static List<Range> getRanges(JobContext job) throws IOException {
		ArrayList<Range> ranges = new ArrayList<Range>();
		for (String rangeString : job.getConfiguration().getStringCollection(RANGES)) {
			ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decodeBase64(rangeString.getBytes()));
			Range range = new Range();
			range.readFields(new DataInputStream(bais));
			ranges.add(range);
		}
		return ranges;
	}

	protected static String getRegex(JobContext job, RegexType type) {
		String key = null;
		switch (type) {
		case ROW:
			key = ROW_REGEX;
			break;
		case COLUMN_FAMILY:
			key = COLUMN_FAMILY_REGEX;
			break;
		case COLUMN_QUALIFIER:
			key = COLUMN_QUALIFIER_REGEX;
			break;
		case VALUE:
			key = VALUE_REGEX;
			break;
		default:
			throw new NoSuchElementException();
		}
		try {
			String s = job.getConfiguration().get(key);
			if(s == null)
				return null;
			return URLDecoder.decode(s,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("Failed to decode regular expression", e);
			throw new RuntimeException(e);
		}
	}

	protected static Set<Pair<Text, Text>> getFetchedColumns(JobContext job) {
		Set<Pair<Text, Text>> columns = new HashSet<Pair<Text, Text>>();
		for (String col : job.getConfiguration().getStringCollection(COLUMNS)) {
			int idx = col.indexOf(":");
			Text cf = new Text(idx < 0 ? Base64.decodeBase64(col.getBytes()) : Base64.decodeBase64(col.substring(0, idx).getBytes()));
			Text cq = idx < 0 ? null : new Text(Base64.decodeBase64(col.substring(idx + 1).getBytes()));
			columns.add(new Pair<Text, Text>(cf, cq));
		}
		return columns;
	}

	protected static boolean getAutoAdjustRanges(JobContext job) {
		return job.getConfiguration().getBoolean(AUTO_ADJUST_RANGES, true);
	}

	protected static Level getLogLevel(JobContext job) {
		return Level.toLevel(job.getConfiguration().getInt(LOGLEVEL, Level.INFO.toInt()));
	}

	// InputFormat doesn't have the equivalent of OutputFormat's
	// checkOutputSpecs(JobContext job)
	protected static void validateOptions(JobContext job) throws IOException {
		Configuration conf = job.getConfiguration();
		if (!conf.getBoolean(INPUT_INFO_HAS_BEEN_SET, false))
			throw new IOException("Input info has not been set.");
		if (!conf.getBoolean(INSTANCE_HAS_BEEN_SET, false))
			throw new IOException("Instance info has not been set.");
		// validate that we can connect as configured
		try {
			Connector c = getInstance(job).getConnector(getUsername(job), getPassword(job));
			if (!c.securityOperations().authenticateUser(getUsername(job), getPassword(job)))
				throw new IOException("Unable to authenticate user");
			if (!c.securityOperations().hasTablePermission(getUsername(job), getTablename(job), TablePermission.READ))
				throw new IOException("Unable to access table");
		} catch (CBException e) {
			throw new IOException(e);
		} catch (CBSecurityException e) {
			throw new IOException(e);
		}
	}

	//Get the maxVersions the VersionsIterator should be configured with.  Return -1 if none.
	protected static int getMaxVersions(JobContext job) {
		return job.getConfiguration().getInt(MAX_VERSIONS, -1);
	}


	//Return a list of the iterator settings (for iterators to apply to a scanner)
	protected static List<CBIterator> getIterators(JobContext job){

		String iterators = job.getConfiguration().get(ITERATORS);

		//If no iterators are present, return an empty list
		if (iterators == null || iterators.isEmpty()) return new ArrayList<CBIterator>();

		//Compose the set of iterators encoded in the job configuration
		StringTokenizer tokens = new StringTokenizer(job.getConfiguration().get(ITERATORS),ITERATORS_DELIM);
		List<CBIterator> list = new ArrayList<CBIterator>();
		while(tokens.hasMoreTokens()){
			String itstring = tokens.nextToken();
			list.add(new CBIterator(itstring));
		}
		return list;
	}


	//Return a list of the iterator options specified
	protected static List<CBIteratorOption> getIteratorOptions(JobContext job){
		String iteratorOptions = job.getConfiguration().get(ITERATORS_OPTIONS);

		//If no options are present, return an empty list
		if (iteratorOptions == null || iteratorOptions.isEmpty()) return new ArrayList<CBIteratorOption>();

		//Compose the set of options encoded in the job configuration
		StringTokenizer tokens = new StringTokenizer(job.getConfiguration().get(ITERATORS_OPTIONS), ITERATORS_DELIM);
		List<CBIteratorOption> list = new ArrayList<CBIteratorOption>();
		while (tokens.hasMoreTokens()){
			String optionString = tokens.nextToken();
			list.add(new CBIteratorOption(optionString));
		}
		return list;
	}




	@Override
	public RecordReader<Key, Value> createRecordReader(InputSplit inSplit, TaskAttemptContext attempt) throws IOException, InterruptedException {
//		log.setLevel(getLogLevel(attempt));
		return new RecordReader<Key, Value>() {
			private int recordsRead;
			private Iterator<Entry<Key, Value>> scannerIterator;
			private boolean scannerRegexEnabled = false;
			private RangeInputSplit split;

			private void checkAndEnableRegex(String regex, BatchScanner scanner, String CBIMethodName) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
				if (regex != null) {
					if (scannerRegexEnabled == false) {
						scanner.setupRegex(PREFIX + ".regex.iterator", 50);
						scannerRegexEnabled = true;
					}
					scanner.getClass().getMethod(CBIMethodName, String.class).invoke(scanner, regex);
					log.info("Setting " + CBIMethodName + " to " + regex);
				}
			}

			private boolean setupRegex(TaskAttemptContext attempt, BatchScanner scanner) throws CBException {
				try {
					checkAndEnableRegex(getRegex(attempt, RegexType.ROW), scanner, "setRowRegex");
					checkAndEnableRegex(getRegex(attempt, RegexType.COLUMN_FAMILY), scanner, "setColumnFamilyRegex");
					checkAndEnableRegex(getRegex(attempt, RegexType.COLUMN_QUALIFIER), scanner, "setColumnQualifierRegex");
					checkAndEnableRegex(getRegex(attempt, RegexType.VALUE), scanner, "setValueRegex");
					return true;
				} catch (Exception e) {
					throw new CBException("Can't set up regex for scanner");
				}
			}

			//Apply the configured iterators from the job to the scanner
			private void setupIterators(TaskAttemptContext attempt, BatchScanner scanner) throws CBException {
				List<CBIterator> iterators = getIterators(attempt);
				List<CBIteratorOption> options = getIteratorOptions(attempt);

				//Loop through the iterators & options, wiring them up to the scanner.
				try {
					for(CBIterator iterator: iterators){
						scanner.setScanIterators(iterator.getPriority(), iterator.getIteratorClass(), iterator.getIteratorName());
					}
					for (CBIteratorOption option: options){
						scanner.setScanIteratorOption(option.getIteratorName(), option.getKey(), option.getValue());
					}
				}
				catch (Exception e) {
					throw new CBException(e);
				}
			}

			//Apply the VersioningIterator at priority 0 based on the job config
			private void setupMaxVersions(TaskAttemptContext attempt, BatchScanner scanner) throws CBException {
				int maxVersions = getMaxVersions(attempt);
				//Check to make sure its a legit value
				if (maxVersions >= 1) {
					try {
						scanner.setScanIterators(0, cloudbase.core.iterators.VersioningIterator.class.getName(), "vers");
					}
					catch (Exception e){
						throw new CBException(e);
					}
					scanner.setScanIteratorOption("vers", "maxVersions", new Integer(maxVersions).toString());
				}
			}

			public void initialize(InputSplit inSplit, TaskAttemptContext attempt) throws IOException {
                split = (RangeInputSplit) inSplit;
				log.debug("Initializing input split: " + split.range);
				Instance instance = getInstance(attempt);
				String user = getUsername(attempt);
				byte[] password = getPassword(attempt);
				Authorizations authorizations = getAuthorizations(attempt);

				try {
					log.debug("Creating connector with user: " + user);
					Connector conn = instance.getConnector(user, password);
					log.debug("Creating scanner for table: " + getTablename(attempt));
					log.debug("Authorizations are: " + authorizations);
					bScanner = conn.createBatchScanner(getTablename(attempt), authorizations, 10);
//					if(isIsolated(attempt)){
//						log.info("Creating isolated scanner");
//						bScanner = new IsolatedScanner(bScanner);
//					}
					setupMaxVersions(attempt, bScanner);
					setupRegex(attempt, bScanner);
					setupIterators(attempt, bScanner);
				} catch (Exception e) {
					throw new IOException(e);
				}

				// setup a scanner within the bounds of this split
				for (Pair<Text, Text> c : getFetchedColumns(attempt)) {
					if (c.getSecond() != null)
						bScanner.fetchColumn(c.getFirst(), c.getSecond());
					else
						bScanner.fetchColumnFamily(c.getFirst());
				}

				bScanner.setRanges(Collections.singleton(split.range));

				recordsRead = 0;

				// do this last after setting all scanner options
				scannerIterator = bScanner.iterator();
			}

			public void close() {
                bScanner.close();
			}

			public float getProgress() throws IOException {
				if(recordsRead > 0 && currentKey == null)
					return 1.0f;
				return split.getProgress(currentKey);
			}

			private Key currentKey = null;
			private Value currentValue = null;

			@Override
			public Key getCurrentKey() throws IOException, InterruptedException {
				return currentKey;
			}

			@Override
			public Value getCurrentValue() throws IOException, InterruptedException {
				return currentValue;
			}

			@Override
			public boolean nextKeyValue() throws IOException, InterruptedException {
				if (scannerIterator.hasNext()) {
					++recordsRead;
					Entry<Key, Value> entry = scannerIterator.next();
					currentKey = entry.getKey();
					currentValue = entry.getValue();
					if (log.isTraceEnabled())
						log.trace("Processing key/value pair: " + DefaultFormatter.formatEntry(entry, true));
					return true;
				}
				return false;
			}
		};
	}

	/**
	 * read the metadata table to get tablets of interest these each become a
	 * split
	 */
	public List<InputSplit> getSplits(JobContext job) throws IOException {
//		log.setLevel(getLogLevel(job));
		validateOptions(job);

		String tableName = getTablename(job);
		boolean autoAdjust = getAutoAdjustRanges(job);
		List<Range> ranges = autoAdjust ? Range.mergeOverlapping(getRanges(job)) : getRanges(job);

		if (ranges.isEmpty()) {
			ranges = new ArrayList<Range>(1);
			ranges.add(new Range());
		}

		// get the metadata information for these ranges
		Map<String, Map<KeyExtent, List<Range>>> binnedRanges = new HashMap<String, Map<KeyExtent, List<Range>>>();
		TabletLocator tl;
		try {
			tl = getTabletLocator(job);
			while (!tl.binRanges(ranges, binnedRanges).isEmpty()) {
				log.warn("Unable to locate bins for specified ranges. Retrying.");
				UtilWaitThread.sleep(100 + (int) (Math.random() * 100)); // sleep
				// randomly
				// between
				// 100
				// and
				// 200
				// ms
			}
		} catch (Exception e) {
			throw new IOException(e);
		}

		ArrayList<InputSplit> splits = new ArrayList<InputSplit>(ranges.size());
		HashMap<Range, ArrayList<String>> splitsToAdd = null;

		if (!autoAdjust)
			splitsToAdd = new HashMap<Range, ArrayList<String>>();

		HashMap<String,String> hostNameCache = new HashMap<String,String>();

		for (Entry<String, Map<KeyExtent, List<Range>>> tserverBin : binnedRanges.entrySet()) {
			String ip = tserverBin.getKey().split(":", 2)[0];
			String location = hostNameCache.get(ip);
			if (location == null) {
				InetAddress inetAddress = InetAddress.getByName(ip);
				location = inetAddress.getHostName();
				hostNameCache.put(ip, location);
			}

			for (Entry<KeyExtent, List<Range>> extentRanges : tserverBin.getValue().entrySet()) {
				Range ke = extentRanges.getKey().toDataRange();
				for (Range r : extentRanges.getValue()) {
					if (autoAdjust) {
						// divide ranges into smaller ranges, based on the
						// tablets
						splits.add(new RangeInputSplit(tableName, ke.clip(r), new String[] { location }));
					} else {
						// don't divide ranges
						ArrayList<String> locations = splitsToAdd.get(r);
						if (locations == null)
							locations = new ArrayList<String>(1);
						locations.add(location);
						splitsToAdd.put(r, locations);
					}
				}
			}
		}

		if (!autoAdjust)
			for (Entry<Range, ArrayList<String>> entry : splitsToAdd.entrySet())
				splits.add(new RangeInputSplit(tableName, entry.getKey(), entry.getValue().toArray(new String[0])));
		return splits;
	}



	/**
	 * The Class RangeInputSplit.   Encapsulates a Cloudbase range for use in Map Reduce jobs.
	 */
	public static class RangeInputSplit extends InputSplit implements Writable {
		private Range range;
		private String[] locations;

		public RangeInputSplit() {
			range = new Range();
			locations = new String[0];
		}

		private static byte[] extractBytes(ByteSequence seq, int numBytes)
		{
			byte [] bytes = new byte[numBytes+1];
			bytes[0] = 0;
			for(int i = 0; i < numBytes; i++)
			{
				if(i >= seq.length())
					bytes[i+1] = 0;
				else
					bytes[i+1] = seq.byteAt(i);
			}
			return bytes;
		}

		public static float getProgress(ByteSequence start, ByteSequence end, ByteSequence position)
		{
			int maxDepth = Math.min(Math.max(end.length(),start.length()),position.length());
			BigInteger startBI = new BigInteger(extractBytes(start,maxDepth));
			BigInteger endBI = new BigInteger(extractBytes(end,maxDepth));
			BigInteger positionBI = new BigInteger(extractBytes(position,maxDepth));
			return (float)(positionBI.subtract(startBI).doubleValue() / endBI.subtract(startBI).doubleValue());
		}

		public float getProgress(Key currentKey) {
			if(currentKey == null)
				return 0f;
			if(range.getStartKey() != null && range.getEndKey() != null)
			{
				if(range.getStartKey().compareTo(range.getEndKey(), PartialKey.ROW)!= 0)
				{
					// just look at the row progress
					return getProgress(range.getStartKey().getRowData(),range.getEndKey().getRowData(),currentKey.getRowData());
				}
				else if(range.getStartKey().compareTo(range.getEndKey(), PartialKey.ROW_COLFAM)!= 0)
				{
					// just look at the column family progress
					return getProgress(range.getStartKey().getColumnFamilyData(),range.getEndKey().getColumnFamilyData(),currentKey.getColumnFamilyData());
				}
				else if(range.getStartKey().compareTo(range.getEndKey(), PartialKey.ROW_COLFAM_COLQUAL)!= 0)
				{
					// just look at the column qualifier progress
					return getProgress(range.getStartKey().getColumnQualifierData(),range.getEndKey().getColumnQualifierData(),currentKey.getColumnQualifierData());
				}
			}
			// if we can't figure it out, then claim no progress
			return 0f;
		}

		RangeInputSplit(String table, Range range, String[] locations) {
			this.range = range;
			this.locations = locations;
		}

	    /**
	     * @deprecated Since 1.3; Don't use this method to compute any reasonable distance metric.}
	     */
		@Deprecated
		public long getLength() throws IOException {
			Text startRow = range.isInfiniteStartKey() ? new Text(new byte[] { Byte.MIN_VALUE }) : range.getStartKey().getRow();
			Text stopRow = range.isInfiniteStopKey() ? new Text(new byte[] { Byte.MAX_VALUE }) : range.getEndKey().getRow();
			int maxCommon = Math.min(7, Math.min(startRow.getLength(), stopRow.getLength()));
			long diff = 0;

			byte[] start = startRow.getBytes();
			byte[] stop = stopRow.getBytes();
			for (int i = 0; i < maxCommon; ++i) {
				diff |= 0xff & (start[i] ^ stop[i]);
				diff <<= Byte.SIZE;
			}

			if (startRow.getLength() != stopRow.getLength())
				diff |= 0xff;

			return diff + 1;
		}

		public String[] getLocations() throws IOException {
			return locations;
		}

		public void readFields(DataInput in) throws IOException {
			range.readFields(in);
			int numLocs = in.readInt();
			locations = new String[numLocs];
			for (int i = 0; i < numLocs; ++i)
				locations[i] = in.readUTF();
		}

		public void write(DataOutput out) throws IOException {
			range.write(out);
			out.writeInt(locations.length);
			for (int i = 0; i < locations.length; ++i)
				out.writeUTF(locations[i]);
		}
	}

	/**
	 * The Class IteratorSetting.  Encapsulates specifics for an Cloudbase iterator's name & priority.
	 */
	static class CBIterator{

		private static final String FIELD_SEP = ":";

		private int priority;
		private String iteratorClass;
		private String iteratorName;


		public CBIterator (int priority, String iteratorClass, String iteratorName){
			this.priority = priority;
			this.iteratorClass = iteratorClass;
			this.iteratorName = iteratorName;
		}

		//Parses out a setting given an string supplied from an earlier toString() call
		public CBIterator (String iteratorSetting){
			//Parse the string to expand the iterator
			StringTokenizer tokenizer = new StringTokenizer(iteratorSetting, FIELD_SEP);
			priority = Integer.parseInt(tokenizer.nextToken());
			iteratorClass = tokenizer.nextToken();
			iteratorName = tokenizer.nextToken();
		}

		public int getPriority() {
			return priority;
		}

		public String getIteratorClass() {
			return iteratorClass;
		}

		public String getIteratorName() {
			return iteratorName;
		}

		@Override
		public String toString(){
			return new String(priority + FIELD_SEP + iteratorClass + FIELD_SEP + iteratorName);
		}

	}

	/**
	 * The Class CBIteratorOption. Encapsulates specifics for a Cloudbase iterator's optional configuration
	 * details - associated via the iteratorName.
	 */
	static class CBIteratorOption {
		private static final String FIELD_SEP = ":";

		private String iteratorName;
		private String key;
		private String value;

		public CBIteratorOption(String iteratorName, String key, String value){
			this.iteratorName = iteratorName;
			this.key = key;
			this.value = value;
		}

		//Parses out an option given a string supplied from an earlier toString() call
		public CBIteratorOption(String iteratorOption){
			StringTokenizer tokenizer = new StringTokenizer(iteratorOption, FIELD_SEP);
			this.iteratorName = tokenizer.nextToken();
			this.key = tokenizer.nextToken();
			this.value = tokenizer.nextToken();
		}

		public String getIteratorName() {
			return iteratorName;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return new String(iteratorName + FIELD_SEP + key + FIELD_SEP + value);
		}

	}

}

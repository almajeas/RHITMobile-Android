package edu.rosehulman.android.directory.beta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;

import org.xmlpull.v1.XmlSerializer;

import android.os.Bundle;
import android.os.Environment;
import android.test.AndroidTestRunner;
import android.test.InstrumentationTestRunner;
import android.util.Log;
import android.util.Xml;

public class CustomInstrumentationTestRunner extends InstrumentationTestRunner {

	public static String TAG = "CustomInstrumentationTestRunner";
	
	private static final String TAG_TESTSUITES = "testsuites";
	private static final String TAG_TESTSUITE = "testsuite";
	private static final String TAG_TESTCASE = "testcase";
	private static final String TAG_ERROR = "error";
	private static final String TAG_FAILURE = "failure";
	
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_CLASSNAME = "classname";
	private static final String ATTRIBUTE_TIME = "time";
	private static final String ATTRIBUTE_MESSAGE = "message";
	private static final String ATTRIBUTE_TYPE = "type";
	
	private AndroidTestRunner runner;
	
	private FileOutputStream fout;
	private XmlSerializer serializer;
	
	@Override
	public void onCreate(Bundle arguments) {
		Log.w(TAG, "creating instrumentation");
		
		super.onCreate(arguments);
		
		/*
		File path;
		//path = new File(Environment.getExternalStorageDirectory(), "testResults.xml");
		path = new File(getContext().getFilesDir(), "testResults.xml");
		
		Log.d(TAG, path.toString());
		try {
			path.createNewFile();
		} catch (IOException e1) {
			Log.d(TAG, "Failed to create test results");
			return;
		}
		Log.d(TAG, path.toString());
		*/
		
		try {
			//fout = new FileOutputStream(path);
			fout = getTargetContext().openFileOutput("testResults.xml", 0);
			
			serializer = Xml.newSerializer();
			serializer.setOutput(fout, "UTF-8");
			serializer.startDocument(null, true);
			
			serializer.startTag(null, TAG_TESTSUITES);
			serializer.startTag(null, TAG_TESTSUITE);
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to create test results", e);
			serializer = null;
			return;
		} catch (IOException e) {
			Log.e(TAG, "Failed to create test results", e);
			serializer = null;
			return;
		}
	}
	
	@Override
	public void finish(final int resultCode, final Bundle results) {
		if (serializer != null) {
			try {
				serializer.endTag(null, TAG_TESTSUITE);
				serializer.endTag(null, TAG_TESTSUITES);
				serializer.flush();
			} catch (IOException e) { }
		}
		
		if (fout != null) {
			try {
				fout.close();
			} catch (IOException e) { }
		}
		
		Log.d(TAG, "Write log file...");
		super.finish(resultCode, results);
	}
	
	@Override
	protected AndroidTestRunner getAndroidTestRunner() {
		this.runner = super.getAndroidTestRunner();
		runner.addTestListener(new CustomTestListener());
		return runner;
	}
	
	private class CustomTestListener implements TestListener {
				
		private String currentSuite;
		private long testStartTime;
		private boolean isFailure;

		public void startTest(Test test) {
			Log.d(TAG, "Start Test: " + test.toString());
			
			TestCase testCase = (TestCase)test;
			isFailure = false;
			
			try {
				String currentSuite = testCase.getClass().getName(); 
				if (!currentSuite.equals(this.currentSuite)) {

					serializer.endTag(null, TAG_TESTSUITE);
					serializer.startTag(null, TAG_TESTSUITE);
					serializer.attribute(null, ATTRIBUTE_NAME, currentSuite);
					
					this.currentSuite = currentSuite;
				}
				
				serializer.startTag(null, TAG_TESTCASE);
				serializer.attribute(null, ATTRIBUTE_CLASSNAME, currentSuite);
				serializer.attribute(null, ATTRIBUTE_NAME, testCase.getName());
				testStartTime = System.currentTimeMillis();
				
			} catch (IOException ex) {
				Log.e(TAG, "Failed to start a test case", ex);
			}
		}
		
		private void writeTime() throws IOException {
			String time = String.format("%.4f", (System.currentTimeMillis() - testStartTime) / 1000.0);
			serializer.attribute(null, ATTRIBUTE_TIME, time);
		}
		
		private void writeError(final String tag, Throwable t) throws IOException {
			writeTime();
			
			serializer.startTag(null, tag);
			
			String message = t.getMessage();
			if (message == null) {
				message = "(null)";
			}
			serializer.attribute(null, ATTRIBUTE_MESSAGE, message);			
			serializer.attribute(null, ATTRIBUTE_TYPE, t.getClass().getName());
			serializer.text(t.toString());
			
			serializer.endTag(null, tag);
		}
		
		public void addError(Test test, Throwable t) {
			Log.e(TAG, "Caught error", t);
			isFailure = true;
			
			try {
				writeError(TAG_ERROR, t);				
			} catch (IOException ex) {
				Log.e(TAG, "Failed to write error", ex);
			}
		}

		public void addFailure(Test test, AssertionFailedError t) {
			Log.e(TAG, "Test Failed: " + test.toString());
			Log.e(TAG, "Message: " + t.getMessage());
			isFailure = true;

			try {
				writeError(TAG_FAILURE, t);
			} catch (IOException ex) {
				Log.e(TAG, "Failed to write failure", ex);
			}
		}

		public void endTest(Test test) {
			Log.d(TAG, "End Test: " + test.toString());
			
			try {
				if (!isFailure) {
					writeTime();
				}
				
				serializer.endTag(null, TAG_TESTCASE);
			} catch (IOException ex) {
				Log.e(TAG, "Failed to end test case", ex);
			}
		}

	}
	

	/*
	@SuppressWarnings("unchecked")
	@Override
	public void onStart() {
		Log.w(TAG, "starting instrumentation");
		
		AndroidTestRunner runner = getAndroidTestRunner();
		TestSuite suite = getTestSuite();
		runner.setContext(getContext());
		runner.setInstrumentaiton(this);
		runner.setTest(suite);
		TestResult result = new TestResult();
		result.addListener(this);
		runner.runTest(result);
		
		Log.e(TAG, String.format("Errors: %d", result.failureCount()));
		Enumeration<TestFailure> failures = (Enumeration<TestFailure>)result.failures();
		while (failures.hasMoreElements()) {
			TestFailure failure = failures.nextElement();
			Test test = failure.failedTest();
			Log.e(TAG, String.format("Test %s failed at\n%s with %s", test.toString(), failure.trace(), failure.thrownException().toString()));
			/*
			Log.e(TAG, "Trace: " + failure.trace());
			Log.e(TAG, "Exception Message: " + failure.exceptionMessage());
			Log.e(TAG, "Thrown Exception: " + failure.thrownException().toString());
			Log.e(TAG, "Test: " + test.toString());
			//Log.e(TAG, failure.getClass().getName());
			* /
		}

		Bundle bundle = new Bundle();
		finish(REPORT_VALUE_RESULT_OK, bundle);
	}
	
	@Override
	public void onDestroy() {
		Log.w(TAG, "destroying instrumentation");
		//TODO show log		
		super.onDestroy();
	}
	
	//*
    @Override
    public TestSuite getAllTests() {
        InstrumentationTestSuite suite = new InstrumentationTestSuite(this);

        //TODO add more test cases
        suite.addTestSuite(MainActivityTest.class);
        
        return suite;
    }

    @Override
    public ClassLoader getLoader() {
        return CustomInstrumentationTestRunner.class.getClassLoader();
    }
    //*/
    
    /*
	public void addError(Test test, Throwable t) {
		Log.e(TAG, "Caught error", t);		
	}

	public void addFailure(Test test, AssertionFailedError t) {
		Log.e(TAG, "Test Failed: " + test.toString());
		Log.e(TAG, "Message: " + t.getMessage());
	}

	public void endTest(Test test) {
		Log.d(TAG, "End Test: " + test.toString());
	}

	public void startTest(Test test) {
		Log.d(TAG, "Start Test: " + test.toString());
	}
	*/

	
}

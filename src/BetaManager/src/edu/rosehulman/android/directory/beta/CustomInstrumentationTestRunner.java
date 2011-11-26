package edu.rosehulman.android.directory.beta;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.AndroidTestRunner;
import android.test.InstrumentationTestRunner;
import android.util.Log;
import android.util.Xml;

/**
 * Handler for running MobileDirectory test cases from within BetaManager
 * 
 * For the most part, this class does not change how the test cases are run.
 * It only logs the results of running the test cases and notifies BetaManger
 * when the testing is completed.
 * 
 * The results of a test run will be stored in testResults.xml
 */
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

	List<TestRunnerTestSuite> testSuites;
	
	private class TestRunnerTestCase {
		public TestCase testCase;
		public long startTime;
		public long endTime;
		public boolean isFailure;
		public Throwable error;
		public String className;
		
		public TestRunnerTestCase(TestCase testCase) {
			this.testCase = testCase;
			this.isFailure = false;
			className = testCase.getClass().getName();
			startTime = System.currentTimeMillis();
		}
		
		public void serialize(XmlSerializer serializer) throws IOException {
			serializer.startTag(null, TAG_TESTCASE);
			serializer.attribute(null, ATTRIBUTE_CLASSNAME, className);
			serializer.attribute(null, ATTRIBUTE_NAME, testCase.getName());
			
			if (startTime != 0 && endTime != 0) {
				String time = String.format("%.4f", (endTime - startTime) / 1000.0);
				serializer.attribute(null, ATTRIBUTE_TIME, time);
			}
			
			if (error != null) {
				String tag;
				if (isFailure)
					tag = TAG_FAILURE;
				else
					tag = TAG_ERROR;
				
				serializer.startTag(null, tag);
				
				String message = error.getMessage();
				if (message == null) {
					message = "(null)";
				}
				serializer.attribute(null, ATTRIBUTE_MESSAGE, message);			
				serializer.attribute(null, ATTRIBUTE_TYPE, error.getClass().getName());
				
				StringWriter text = new StringWriter();
				error.printStackTrace(new PrintWriter(text));
				serializer.text(text.toString());
				
				serializer.endTag(null, tag);
			}

			serializer.endTag(null, TAG_TESTCASE);
		}
	}
	
	private class TestRunnerTestSuite {
		
		public String name;
		public List<TestRunnerTestCase> testCases;
		
		public TestRunnerTestSuite(String className) {
			this.name = className;
			this.testCases = new LinkedList<TestRunnerTestCase>();
		}

		public void serialize(XmlSerializer serializer) throws IOException {
			serializer.startTag(null, TAG_TESTSUITE);
			serializer.attribute(null, ATTRIBUTE_NAME, name);
			
			for (TestRunnerTestCase testCase : testCases) {
				testCase.serialize(serializer);
			}
			
			serializer.endTag(null, TAG_TESTSUITE);
		}
	}
	
	
	@Override
	public void onCreate(Bundle arguments) {
		Log.w(TAG, "creating instrumentation");
		
		super.onCreate(arguments);
		
    	BetaPrefs.setUseMocks(this.getContext(), true);
		
		testSuites = new LinkedList<TestRunnerTestSuite>();
	}
	
	@Override
	public void finish(final int resultCode, final Bundle results) {
		
		try {
			writeTestResults();
			Log.d(TAG, "Wrote JUnit test results XML file");
		} catch (IOException e) {
			Log.e(TAG, "Failed to write test results", e);
		}
		
		//let BetaManager know that the unit tests are done
		Context context = getContext();
		context.sendBroadcast(new Intent(context, InstrumentationCompleted.class));
		
		super.finish(resultCode, results);
	}
	
	@Override
	protected AndroidTestRunner getAndroidTestRunner() {
		this.runner = super.getAndroidTestRunner();
		runner.addTestListener(new CustomTestListener());
		return runner;
	}
	
	private void writeTestResults() throws IOException {

		FileOutputStream fout;
		XmlSerializer serializer;
		
		try {
			fout = getTargetContext().openFileOutput("testResults.xml", 0);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to create test results output file", e);
			return;
		}

		serializer = Xml.newSerializer();
		serializer.setOutput(fout, "UTF-8");
		serializer.startDocument(null, true);
		
		serializer.startTag(null, TAG_TESTSUITES);
		
		for (TestRunnerTestSuite testSuite : testSuites) {
			testSuite.serialize(serializer);
		}
		
		serializer.endTag(null, TAG_TESTSUITES);
		
		serializer.flush();
		fout.close();
	}
	
	private class CustomTestListener implements TestListener {
				
		private TestRunnerTestSuite currentSuite;
		private TestRunnerTestCase currentTest;

		public void startTest(Test test) {
			Log.d(TAG, "Start Test: " + test.toString());
			
			currentTest = new TestRunnerTestCase((TestCase)test);
			
			if (currentSuite == null || !currentSuite.name.equals(currentTest.className)) {
				currentSuite = new TestRunnerTestSuite(currentTest.className);
				testSuites.add(currentSuite);
			}
			
			currentSuite.testCases.add(currentTest);
		}
		
		public void addError(Test test, Throwable t) {
			Log.e(TAG, "Caught error", t);
			
			currentTest.error = t;
		}

		public void addFailure(Test test, AssertionFailedError t) {
			Log.e(TAG, "Test Failed: " + test.toString());
			Log.e(TAG, "Message: " + t.getMessage());
			
			currentTest.isFailure = true;
			currentTest.error = t;
		}

		public void endTest(Test test) {
			Log.d(TAG, "End Test: " + test.toString());
			
			currentTest.endTime = System.currentTimeMillis();
		}

	}	
}

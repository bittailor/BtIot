package ch.bittailor.iot.core.integrationtest;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

import ch.bittailor.iot.core.integrationtest.devices.nrf24.RfDeviceTest;
import ch.bittailor.iot.core.integrationtest.wsn.RfPacketSocketImplTest;

public class Activator implements BundleActivator {

	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		
		BundleWiring bundleWiring = context.getBundle().adapt( BundleWiring.class );
		
		Collection<String> resources = bundleWiring.listResources("/", "*Test.class", BundleWiring.LISTRESOURCES_LOCAL | BundleWiring.LISTRESOURCES_RECURSE);	
		List<Class<?>> testClasses = new LinkedList<Class<?>>();
		
		for (String resource : resources) {
			testClasses.add(loadClass(context.getBundle(), toClassName(resource)));
			System.out.println(toClassName(resource));
		}
		
		System.out.println("Run all tests ...");
		JUnitCore jUnitCore = new org.junit.runner.JUnitCore();
		
		jUnitCore.addListener(new RunListener(){

			@Override
			public void testAssumptionFailure(Failure failure) {
				System.out.println("    ! Assumption Failure: " + failure.toString());
			}

			@Override
			public void testFailure(Failure failure) throws Exception {
				System.out.println("    ! Failure: " + failure.toString());
				if(failure.getMessage() == null) {
					System.out.println("  exception :" + failure.getException());
					System.out.println("  trace     :" + failure.getTrace());		
				}
			}

			@Override
			public void testIgnored(Description description) throws Exception {
				System.out.println("  ignore " + description.getDisplayName());
			}

			@Override
			public void testRunFinished(Result result) throws Exception {
			}

			@Override
			public void testRunStarted(Description description) throws Exception {
			}

			@Override
			public void testStarted(Description description) throws Exception {
				System.out.println("  run " + description.getDisplayName() + " ...");
			}
			
			@Override
			public void testFinished(Description description) throws Exception {
				System.out.println("  ... done");
			}
			
		});
		System.out.println("... done");
		
		
		//Result result = jUnitCore.run(RfDeviceTest.class);
		//Result result = jUnitCore.run(RfPacketSocketImplTest.class);
		
		Result result = jUnitCore.run(testClasses.toArray(new Class<?>[0]));
		if(result.getFailureCount() == 0){
			System.out.println("*** ALL " + result.getRunCount() + " TESTS PASSED ***");
			return;
		}
		
		System.out.println("!!! " + result.getFailureCount() + " TESTS FAILED !!!");
		for (Failure failure : result.getFailures()) {
			System.out.println("  " + failure.toString());
		}
		
		
	}
	
	private Class<?> loadClass(Bundle bundle, String className ) throws ClassNotFoundException  {
    return bundle.loadClass( className );  
  }
	
	private static String toClassName( String string ) {
    String result = string.replace( '/', '.' );
    if( result.endsWith( ".class" ) ) {
      result = result.substring( 0, result.length() - ".class".length() );
    }
    return result;
  }
	
	
	
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		System.out.println("Goodbye World!!");
	}

}

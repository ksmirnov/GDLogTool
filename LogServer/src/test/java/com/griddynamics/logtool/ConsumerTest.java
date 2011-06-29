package com.griddynamics.logtool;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class ConsumerTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ConsumerTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( 
            ConsumerTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testConsumer()
    {
        assertTrue( true );
        
    }
}



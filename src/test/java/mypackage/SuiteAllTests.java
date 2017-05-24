/*
 * THE LICENSE AND COPYRIGHT HEADER
 */

package mypackage;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  TestMyPluginPr.class
})
public class SuiteAllTests {
  // so we can run this test from the command line 
  public static void main(String args[]) {
    org.junit.runner.JUnitCore.main(SuiteAllTests.class.getCanonicalName());
  }  
  
}

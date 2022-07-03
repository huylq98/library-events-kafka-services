package vn.com.huylq.libraryeventsproducer.unit;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@SelectPackages({
    "vn.com.huylq.libraryeventsproducer.unit.controller",
    "vn.com.huylq.libraryeventsproducer.unit.producer"
})
@Suite
@SuppressWarnings("java:S2187")
public class LibraryEventsProducerSuiteTest {

}

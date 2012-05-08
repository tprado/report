package spock.damagecontrol.testresults

import spock.lang.Specification

import static org.apache.commons.io.FileUtils.copyFileToDirectory
import static org.apache.commons.io.FileUtils.deleteDirectory

class TestResultsCollectorTest extends Specification {

    private static final String SAMPLE_FOLDER = 'src/test/resources/samples'

    private static final File XML_WITH_ONE_TEST_CASE = new File(SAMPLE_FOLDER + '/TEST-spock.damagecontrol.TestResultsParserTest.xml')
    private static final File XML_WITH_TWO_TEST_CASES = new File(SAMPLE_FOLDER + '/TEST-spock.damagecontrol.TestResultsWith2TestCases.xml')
    private static final File XML_WITH_NO_TEST_CASE = new File(SAMPLE_FOLDER + '/TEST-no-test-case.xml')
    private static final File EMPTY = new File(SAMPLE_FOLDER + '/empty.xml')

    private static final File RESULTS_FOLDER = new File('build/' + TestResultsCollectorTest.class.name)

    def setup() {
        deleteDirectory(RESULTS_FOLDER);
        RESULTS_FOLDER.mkdirs()
    }

    def cleanup() {
        deleteDirectory(RESULTS_FOLDER);
    }

    def 'should collect all specification names in the folder'() {
        given:
        copyFileToDirectory(XML_WITH_ONE_TEST_CASE, RESULTS_FOLDER)
        copyFileToDirectory(XML_WITH_TWO_TEST_CASES, RESULTS_FOLDER)

        when:
        def testResults = new TestResultsCollector(RESULTS_FOLDER).collect();

        then:
        assert testResults.specs['spock.damagecontrol.TestResultsCollectorTest1']
        assert testResults.specs['spock.damagecontrol.TestResultsCollectorTest2']
        assert testResults.specs['spock.damagecontrol.AnotherTestResultsCollectorTest']
    }

    def 'should collect all features for the same specification'() {
        given:
        copyFileToDirectory(XML_WITH_TWO_TEST_CASES, RESULTS_FOLDER)

        when:
        def testResults = new TestResultsCollector(RESULTS_FOLDER).collect();

        then:
        assert testResults.specs['spock.damagecontrol.AnotherTestResultsCollectorTest'].features['shouldParseXml']
        assert testResults.specs['spock.damagecontrol.AnotherTestResultsCollectorTest'].features['shouldFail']
    }

    def 'should collect failure message for each feature'() {
        given:
        copyFileToDirectory(XML_WITH_TWO_TEST_CASES, RESULTS_FOLDER)

        when:
        def testResults = new TestResultsCollector(RESULTS_FOLDER).collect();
        def feature = testResults.specs['spock.damagecontrol.AnotherTestResultsCollectorTest'].features['shouldFail']

        then:
        assert feature.failure.message == 'java.lang.AssertionError: \nExpected: is <true>\n     got: <false>\n'
    }

    def 'should collect failure details for each feature'() {
        given:
        copyFileToDirectory(XML_WITH_TWO_TEST_CASES, RESULTS_FOLDER)

        when:
        def testResults = new TestResultsCollector(RESULTS_FOLDER).collect();
        def feature = testResults.specs['spock.damagecontrol.AnotherTestResultsCollectorTest'].features['shouldFail']

        then:
        assert feature.failure.details.contains('at spock.damagecontrol.TestResultsCollectorTest.shouldFail(TestResultsParserTest.groovy:19)')
    }

    def 'should collect anything when result file has no test cases'() {
        given:
        copyFileToDirectory(XML_WITH_NO_TEST_CASE, RESULTS_FOLDER)

        when:
        def testResults = new TestResultsCollector(RESULTS_FOLDER).collect();

        then:
        assert testResults.specs.size() == 0
    }

    def 'should collect anything when result file is empty'() {
        given:
        copyFileToDirectory(EMPTY, RESULTS_FOLDER)

        when:
        def testResults = new TestResultsCollector(RESULTS_FOLDER).collect();

        then:
        assert testResults.specs.size() == 0
    }
}

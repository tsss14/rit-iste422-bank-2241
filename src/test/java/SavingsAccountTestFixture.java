import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SavingsAccountTestFixture {
    public static Logger logger = LogManager.getLogger(SavingsAccountTestFixture.class);
    // Note that we could also load the file from the classpath instead of hardcoding the pathname
    static String TEST_FILE = "src/test/resources/SavingsAccountTest.csv".replace('/', File.separatorChar);

    record TestScenario(double initBalance,
                        double interestRate,
                        List<Double> withdrawals,
                        List<Double> deposits,
                        int runMonthEndNTimes,
                        double endBalance
    ) { }

    private static List<TestScenario> testScenarios;

    @Test
    public void runTestScenarios() throws Exception {
        if (testScenarios == null) {
            System.err.println("\n\n");
            System.err.println("************************************");
            System.err.println("************************************");
            System.err.println();
            System.err.println("Note: NOT running any Test Scenarios");
            System.err.println("Run main() method to run scenarios!!");
            System.err.println();
            System.err.println("************************************");
            System.err.println("************************************");
            System.err.println("\n\n");
            return;
        }

        // iterate over all test scenarios
        for (int testNum = 0; testNum < testScenarios.size(); testNum++) {
            TestScenario scenario = testScenarios.get(testNum);
            logger.info("**** Running test for {}", scenario);

            // set up account with specified starting balance and interest rate
            // TODO: Add code to create account....
            SavingsAccount sa = new SavingsAccount();
            sa.deposit(scenario.initBalance);
            sa.setMinimumBalance(100);
            sa.setBelowMinimumFee(10);

            for (double withdrawalAmount : scenario.withdrawals) {
                sa.withdraw(withdrawalAmount);
            }
            for (double depositAmount : scenario.deposits) {
                sa.deposit(depositAmount);
            }

            // run month-end if desired and output register
            if (scenario.runMonthEndNTimes > 0) {
                sa.monthEnd();
                for (RegisterEntry entry : sa.getRegisterEntries()) {
                    logger.info("Register Entry {} -- {}: {}", entry.id(), entry.entryName(), entry.amount());

                }
            }

            assertThat("Test #" + testNum + ":" + scenario, sa.getBalance(), is(scenario.endBalance));
        }
    }

    private static void runJunitTests() {
        JUnitCore jc = new JUnitCore();
        jc.addListener(new TextListener(System.out));
        Result r = jc.run(SavingsAccountTestFixture.class);
        System.out.printf("Tests run: %d Passed: %d Failed: %d\n",
                r.getRunCount(), r.getRunCount() - r.getFailureCount(), r.getFailureCount());
        System.out.println("Failures:");
        for (Failure f : r.getFailures()) {
            System.out.println("\t"+f);
        }
    }

    // NOTE: this could be added to TestScenario class
    private static List<Double> parseListOfAmounts(String amounts) {
        if (amounts.trim().isEmpty()) {
            return List.of();
        }
        List<Double> ret = new ArrayList<>();
        logger.debug("Amounts to split: {}", amounts);
        for (String amtStr : amounts.trim().split("\\|")) {
            logger.debug("An Amount: {}", amtStr);
            ret.add(Double.parseDouble(amtStr));
        }
        return ret;
    }

    // NOTE: this could be added to TestScenario class
    private static TestScenario parseScenarioString(String scenarioAsString) {
        String [] scenarioValues = scenarioAsString.split(",");
        // should probably validate length here
        double initialBalance = Double.parseDouble(scenarioValues[0]);
        double interestRate = Double.parseDouble(scenarioValues[1]);
        double endBalance = Double.parseDouble(scenarioValues[5]);
        int runMonthEndNTimes = Integer.parseInt(scenarioValues[4]);
        List<Double> wds = parseListOfAmounts(scenarioValues[2]);
        List<Double> dps = parseListOfAmounts(scenarioValues[3]);
        TestScenario scenario = new TestScenario(
                initialBalance, interestRate, wds, dps, runMonthEndNTimes, endBalance
        );
        return scenario;
    }

    private static List<TestScenario> parseScenarioStrings(List<String> scenarioStrings) {
        logger.info("Parsing test scenarios...");
        List<TestScenario> scenarios = new ArrayList<>();
        for (String scenarioAsString : scenarioStrings) {
            if (scenarioAsString.trim().isEmpty()) {
                continue;
            }
            TestScenario scenario = parseScenarioString(scenarioAsString);
            scenarios.add(scenario);
        }
        return scenarios;
    }

    public static void main(String [] args) throws Exception {
        System.out.println("START TESTING");
        List<String> scenarioStrings = new ArrayList<>();
        if(args.length > 0) {
            if(args.length > 2) {
                throw new Exception("This command does not except more than two arguments. Usage: {-l **csv structured test parameters**}\nOr: {**filename**}");
            }
            if(args[0].equals("-l")) {
                scenarioStrings.add(args[1]);
            } else if (args.length == 1){
                TEST_FILE = "src/test/resources/" + args[0];
                scenarioStrings = Files.readAllLines(Paths.get(TEST_FILE));
            } else {
                throw new Exception("These are invalid arguments.  Usage: {-l **csv structured test parameters**}\n" + //
                                        "Or: {**filename**}");
            }
        } else {
            scenarioStrings = Files.readAllLines(Paths.get(TEST_FILE));
        }
        // Note: toArray converts from a List to an array
        testScenarios = parseScenarioStrings(scenarioStrings);
        runJunitTests();
        System.out.println("DONE");
    }
}

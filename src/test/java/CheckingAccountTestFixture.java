import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CheckingAccountTestFixture {
    public static Logger logger = LogManager.getLogger(CheckingAccountTestFixture.class);
    // We could read the file from classpath instead of hardcoding the pathname too
    static String TEST_FILE = "src/test/resources/CheckingAccountTest.csv";

    record TestScenario(double initBalance,
                        List<Double> checks,
                        List<Double> withdrawals,
                        List<Double> deposits,
                        boolean runMonthEnd,
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

            // set up account with specified starting balance
            CheckingAccount ca = new CheckingAccount(
                    "test "+testNum, -1, scenario.initBalance, 0, -1);
            ca.setMinimumBalance(100d);
            ca.setBelowMinimumFee(10d);

            // now process checks, withdrawals, deposits
            for (double checkAmount : scenario.checks) {
                ca.writeCheck("CHECK", checkAmount, new Date());
            }
            for (double withdrawalAmount : scenario.withdrawals) {
                ca.withdraw(withdrawalAmount);
            }
            for (double depositAmount : scenario.deposits) {
                ca.deposit(depositAmount);
            }

            // run month-end if desired and output register
            if (scenario.runMonthEnd) {
                ca.monthEnd();
                for (RegisterEntry entry : ca.getRegisterEntries()) {
                    logger.info("Register Entry {} -- {}: {}", entry.id(), entry.entryName(), entry.amount());

                }
            }

            // make sure the balance is correct
            assertThat("Test #" + testNum + ":" + scenario, ca.getBalance(), is(scenario.endBalance));
        }
    }

    private static void runJunitTests() {
        JUnitCore jc = new JUnitCore();
        jc.addListener(new TextListener(System.out));
        Result r = jc.run(CheckingAccountTestFixture.class);
        System.out.printf("Tests run: %d Passed: %d Failed: %d\n",
                r.getRunCount(), r.getRunCount() - r.getFailureCount(), r.getFailureCount());
        System.out.println("Failures:");
        for (Failure f : r.getFailures()) {
            System.out.println("\t"+f);
        }
    }

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

    private static TestScenario parseScenarioString(String scenarioAsString) {
        String [] scenarioValues = scenarioAsString.split(",");
        // should probably validate length here
        double initialBalance = Double.parseDouble(scenarioValues[0]);
        List<Double> checks = parseListOfAmounts(scenarioValues[1]);
        List<Double> wds = parseListOfAmounts(scenarioValues[2]);
        List<Double> deps = parseListOfAmounts(scenarioValues[3]);
        boolean runMonthEndFee = Boolean.parseBoolean(scenarioValues[4].trim());
        double finalBalance = Double.parseDouble(scenarioValues[5]);
        TestScenario scenario = new TestScenario(
                initialBalance, checks, wds, deps, runMonthEndFee, finalBalance
        );
        return scenario;
    }

    private static List<TestScenario> parseScenarioStrings(List<String> scenarioStrings) {
        logger.info("Running test scenarios...");
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

    public static void main(String [] args) throws IOException {
        System.out.println("START");

        // We can:
        // ... manually populate the list of scenarios we want to test...
        System.out.println("\n\n****** FROM OBJECTS ******\n");
        testScenarios = List.of(
                new TestScenario(100, List.of(), List.of(), List.of(), false, 100),
                new TestScenario(100, List.of(10d), List.of(), List.of(), false, 90),
                new TestScenario(100, List.of(10.,20.), List.of(), List.of(10.), true, 80)
                );
        runJunitTests();

        // ...or create scenarios from a collection of strings...
        // Format for each line: BALANCE,check_amt|check_amt|...,withdraw_amt|...,deposit_amt|...,end_balance
        // note we left out runMonthEnd from our file format

        // Same scenarios as above plus one more to verify it's running these string scenarios
        List<TestScenario> parsedScenarios = parseScenarioStrings(scenarioStrings);
        testScenarios = parsedScenarios;
        runJunitTests();

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
        testScenarios = parseScenarioStrings(scenarioStrings);
        runJunitTests();

        // ...or, we could also specify a single scenario on the command line,
        // for example "-t '10, 20|20, , 40|10, 0'"
        // Note the single-quotes because of the embedded spaces and the pipe symbol
        System.out.println("Command-line arguments passed in: " + java.util.Arrays.asList(args));
        
        System.out.println("DONE");
    }
}

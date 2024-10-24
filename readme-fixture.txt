Checking Account Test CSV Column Order:
Initial Balance, Checks, Withdrawals, Deposits, runMonthEnd?, Final Balance

Checking Account Test CSV Column Order:
Initial Balance, Interest Rate, Withdrawals, Deposits, runMonthEndTimes, Final Balance

To run the checking fixture type "./gradelw runCheckingFixture"

To run the savings fixture type "./gradlew runSavingsFixture"

Alternatively you may pass in an argument for a filename:

"./gradelw runCheckingFixture --args='**filename**'"

"./gradlew runSavingsFixture --args='**filename**'"

Finally you may pass in a flag for a csv line and an argument that corresponds:

"./gradelw runCheckingFixture --args='-l **csv parameters**'"

"./gradlew runSavingsFixture --args='-l **csv parameters**'"

I have edited the gradle wrapper properties file so that it uses version 8.4 in order to correspond with my version of Java. You may need to change this. 

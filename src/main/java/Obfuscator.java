import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Obfuscator {
    List<CheckingAccount> checkingAccounts = new LinkedList<>();
    List<CheckingAccount> owners = new LinkedList<>();
    List<CheckingAccount> registers = new LinkedList<>();
    List<CheckingAccount> savings = new LinkedList<>();
    public void loadData() throws IOException{
        FileReader fr = new FileReader("../resources/production_data/checking.csv");
        BufferedReader br = new BufferedReader(fr);
        br.readLine();
        while(br.ready()) {
            String line = br.readLine();
            CheckingAccount ca = CheckingAccount.fromCSV(line);
            checkingAccounts.add(ca);
        }
        br.close();
        fr = new FileReader("../resources/production_data/owners.csv");
        br = new BufferedReader(fr);
        br.readLine();
        while(br.ready()) {
            String line = br.readLine();
            Owner o = Owner.fromCSV(line);
            owners.add(o);
        }
        br.close();
        fr = new FileReader("../resources/production_data/register.csv");
        br = new BufferedReader(fr);
        br.readLine();
        while(br.ready()) {
            String line = br.readLine();
            Register r = Register.fromCSV(line);
            registers.add(r);
        }
        br.close();
        fr = new FileReader("../resources/production_data/savings.csv");
        br = new BufferedReader(fr);
        br.readLine();
        while(br.ready()) {
            String line = br.readLine();
            SavingsAccount sa = SavingsAccount.fromCSV(line);
            checkingAccounts.add(sa);
        }
        br.close();
    }

    public void obfuscate() throws IOException{
        FileWriter fw = new FileWriter("../../test/resources/checking_integ.csv");
        BufferedWriter bw = new BufferedWriter(fw);
        for(CheckingAccount ca : checkingAccounts) {
            bw.newLine();
            bw.write(ca.toCSV());
            bw.close();
        } 
        fw = new FileWriter("../../test/resources/owners_integ.csv");
        bw = new BufferedWriter(fw);
        for(Owner o : owners) {
            bw.newLine();
            bw.write(o.toCSV());
            bw.close();
        } 
        fw = new FileWriter("../../test/resources/register_integ.csv");
        bw = new BufferedWriter(fw);
        for(Register r : registers) {
            bw.newLine();
            bw.write(r.toCSV());
            bw.close();
        }
        fw = new FileWriter("../../test/resources/savings_integ.csv");
        bw = new BufferedWriter(fw);
        for(SavingsAccount sa : savings) {
            bw.newLine();
            bw.write(sa.toCSV());
            bw.close();
        } 
    }
}
